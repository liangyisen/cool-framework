package com.eiff.framework.mq.rocketmq;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.MessageQueueSelector;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import com.eiff.framework.kv.KVDb;
import com.eiff.framework.kv.KVManager;
import com.eiff.framework.log.api.Constants;
import com.eiff.framework.log.api.HdLogger;
import com.eiff.framework.log.api.trace.Span;
import com.eiff.framework.log.api.trace.Tracer;
import com.eiff.framework.mq.rocketmq.callback.SendCallbackEx;
import com.eiff.framework.mq.rocketmq.common.FailedSendMessage;
import com.eiff.framework.mq.rocketmq.common.MQConstants;
import com.eiff.framework.mq.rocketmq.common.MessageConverterUtils;
import com.eiff.framework.mq.rocketmq.common.MessageHashCodeUtils;
import com.eiff.framework.mq.rocketmq.common.MessageWrapper;
import com.eiff.framework.mq.rocketmq.exception.MQAsyncProducerReturnNullException;

public class DefaultRMQProducer extends DefaultMQProducer implements FactoryBean<DefaultMQProducer>, InitializingBean,
		DisposableBean, Constants {

	private static HdLogger LOGGER = HdLogger.getLogger(DefaultRMQProducer.class);

	private static final String FAILED_SEND_MESSAGE_TOPIC = "FAILED_SEND_MESSAGE";

	private static final int MAX_SEND_TIMES = 3;

	private DefaultMQProducer producer = new DefaultMQProducer();

	private KVManager kvManager;

	private String localDBPath;

	private KVDb kvdb;

	private LinkedBlockingQueue<FailedSendMessage> failedMsgQueue = new LinkedBlockingQueue<FailedSendMessage>(10000);

	@Override
	public DefaultMQProducer getObject() throws Exception {
		return this;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		producer.setNamesrvAddr(getNamesrvAddr());
		producer.setProducerGroup(getProducerGroup());

		Assert.hasText(getNamesrvAddr(),
				"[Assertion failed] - namesrvAddr must have text; it must not be null, empty, or blank");
		Assert.hasText(getProducerGroup(),
				"[Assertion failed] - producerGroup must have text; it must not be null, empty, or blank");

		producer.setClientCallbackExecutorThreads(getClientCallbackExecutorThreads());
		producer.setClientIP(getClientIP());
		producer.setCompressMsgBodyOverHowmuch(getCompressMsgBodyOverHowmuch());
		producer.setCreateTopicKey(getCreateTopicKey());
		producer.setDefaultTopicQueueNums(getDefaultTopicQueueNums());
		producer.setHeartbeatBrokerInterval(getHeartbeatBrokerInterval());
		producer.setInstanceName(getInstanceName());
		producer.setMaxMessageSize(getMaxMessageSize());
		producer.setPersistConsumerOffsetInterval(getPersistConsumerOffsetInterval());
		producer.setPollNameServerInterval(getPollNameServerInterval());
		producer.setRetryAnotherBrokerWhenNotStoreOK(isRetryAnotherBrokerWhenNotStoreOK());
		producer.setRetryTimesWhenSendFailed(getRetryTimesWhenSendFailed());
		producer.setSendMsgTimeout(getSendMsgTimeout());
		producer.setUnitMode(isUnitMode());

		producer.start();

		if (StringUtils.isNotBlank(localDBPath) && kvManager != null) {
			kvdb = kvManager.getKVDB(localDBPath, "RMQProducerDB");
			if (null != kvdb) {
				saveFailedMsgToLocal();

				scheduleSendFailedMsg();
			}
		}
	}

	/**
	 * 定时从localdb里面拿出发送失败的消息发送到MQ去
	 */
	private void scheduleSendFailedMsg() {
		final ScheduledExecutorService scheduledExecutorService = Executors
				.newSingleThreadScheduledExecutor(new ThreadFactory() {

					@Override
					public Thread newThread(Runnable r) {
						return new Thread(r, "SendFailedMessageScheduleThread");
					}
				});

		scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				try {
					final Set<Object> keySet = kvdb.keySet();
					if (keySet != null && keySet.size() > 0) {
						LOGGER.info("failed message keys size:" + keySet.size());
						for (final Object key : keySet) {
							try {
								final FailedSendMessage failedSendMessage = (FailedSendMessage) kvdb.get(key);
								LOGGER.info("Will resend failed message:" + failedSendMessage);

								kvdb.remove(key);
								kvdb.commit();

								DefaultRMQProducer.this.send(new MessageWrapper(FAILED_SEND_MESSAGE_TOPIC,
										failedSendMessage));
							} catch (Exception e) {
								LOGGER.warn("send failed message to MQ[FAILED_SEND_MESSAGE].", e);
							}

						}
					}
				} catch (Throwable e) {
					LOGGER.error("SendFailedMessageScheduleThread error.", e);
				}
			}
		}, 2, 5, TimeUnit.MINUTES);
	}

	/**
	 * 发送失败的消息保存到localdb里面
	 */
	private void saveFailedMsgToLocal() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					while (true) {
						try {
							final FailedSendMessage failedSendMessage = DefaultRMQProducer.this.failedMsgQueue.poll(
									3000, TimeUnit.MILLISECONDS);
							if (failedSendMessage != null
									&& !FAILED_SEND_MESSAGE_TOPIC.equals(failedSendMessage.getMsg().getTopic())) {
								LOGGER.info("Start to persist failed msg :" + failedSendMessage);
								kvdb.put(failedSendMessage.getMsg().getProperty(TRACE_CHILD + 1), failedSendMessage);
								kvdb.commit();
							}
						} catch (Exception e) {
							LOGGER.warn("save failed message has exception. ", e);
						}
					}
				} catch (Throwable e) {
					LOGGER.error("SaveFailedMsgToMapDBThread error.", e);
				}
			}
		}, "SaveFailedMsgToMapDBThread").start();
	}

	@Override
	public void destroy() throws Exception {
		producer.shutdown();
	}

	@Override
	public Class<?> getObjectType() {
		return DefaultRMQProducer.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	public DefaultMQProducer getProducer() {
		return producer;
	}

	private SendResult syncSend(Message msg, MessageQueue mq, MessageQueueSelector selector, Object arg, Long timeout)
			throws MQClientException, RemotingException, MQBrokerException, InterruptedException {
		Tracer buildTracer = LOGGER.buildTracer();
		Span span = buildTracer.createSpan(TRANS_TYPE_MQ_SYNC_PRODUCER, msg.getTopic());
		Map<String, String> map = buildTracer.getContext();
		if (MapUtils.isNotEmpty(map)) {
			for (String key : map.keySet())
				msg.putUserProperty(key, map.get(key));
		}

		final String group = System.getenv(MQConstants.GROUP);
		if (StringUtils.isNotBlank(group) && !msg.getTopic().endsWith(MQConstants.UNDER_LINE + group)) {
			msg.setTopic(msg.getTopic() + MQConstants.UNDER_LINE + group);
		}

		int hashcode = MessageHashCodeUtils.generateMsgHashCode(msg);
		if (hashcode != 0) {
			// for remove repeated message
			msg.putUserProperty(MQConstants.HASH_CODE, hashcode + "");
		}

		LOGGER.info(LOG_MQ_PRODUCER_SYNC_IN_MSG, msg);

		SendResult sendResult = null;
		try {
			if (mq != null) {
				if (timeout != null) {
					sendResult = producer.send(msg, mq, timeout);
				} else {
					sendResult = producer.send(msg, mq);
				}
			} else if (selector != null) {
				if (timeout != null) {
					sendResult = producer.send(msg, selector, arg, timeout);
				} else {
					sendResult = producer.send(msg, selector, arg);
				}
			} else {
				if (timeout != null) {
					sendResult = producer.send(msg, timeout);
				} else {
					sendResult = producer.send(msg);
				}
			}

			if (sendResult != null && sendResult.getSendStatus() != null) {
				SendStatus sendStatus = sendResult.getSendStatus();
				if (sendStatus.equals(SendStatus.SEND_OK)) {
					LOGGER.info(LOG_MQ_PRODUCER_SYNC_OK_MSG, msg, sendResult);
				} else if (sendStatus.equals(SendStatus.SLAVE_NOT_AVAILABLE)) {
					LOGGER.info(LOG_MQ_PRODUCER_SYNC_SNA_MSG, msg, sendResult);
				} else if (sendStatus.equals(SendStatus.FLUSH_DISK_TIMEOUT)) {
					LOGGER.info(LOG_MQ_PRODUCER_SYNC_FDT_MSG, msg, sendResult);
				} else {
					LOGGER.info(LOG_MQ_PRODUCER_SYNC_FST_MSG, msg, sendResult);
				}

				span.addEvent(TRANS_TYPE_MQ_SYNC_PRODUCER, "" + sendResult.getSendStatus());
			} else {
				LOGGER.error(LOG_MQ_PRODUCER_SYNC_RETURNNULL_MSG, msg);
				span.failed(new MQAsyncProducerReturnNullException());
				return sendResult;
			}

			span.success();
			return sendResult;
		} catch (Exception e) {
			LOGGER.error(LOG_MQ_PRODUCER_SYNC_EX_MSG, msg, e);

			if (null != kvdb) {
				final Message cloneMsg = (Message) MessageHashCodeUtils.deepClone(msg);
				cloneMsg.setBody(null);
				failedMsgQueue.offer(new FailedSendMessage(cloneMsg, MessageConverterUtils.getMessageObject(msg),
						MessageHashCodeUtils.currentStackTrace(e), e.getMessage()));
			}

			span.failed(e);
			throw e;
		} finally {
			LOGGER.info(LOG_MQ_PRODUCER_SYNC_OUT_MSG, msg);
			span.close();
		}
	}

	private void asyncSend(Message msg, MessageQueue mq, MessageQueueSelector selector, Object arg,
			SendCallback sendCallback, Long timeout) throws MQClientException, RemotingException, InterruptedException {
		Tracer buildTracer = LOGGER.buildTracer();
		Span span = buildTracer.createSpan(TRANS_TYPE_MQ_ASYNC_PRODUCER, msg.getTopic());
		Map<String, String> map = buildTracer.getContext4Async();
		if (MapUtils.isNotEmpty(map)) {
			for (String key : map.keySet())
				msg.putUserProperty(key, map.get(key));
		}

		final String group = System.getenv(MQConstants.GROUP);
		if (StringUtils.isNotBlank(group) && !msg.getTopic().endsWith(MQConstants.UNDER_LINE + group)) {
			msg.setTopic(msg.getTopic() + MQConstants.UNDER_LINE + group);
		}

		LOGGER.info(LOG_MQ_PRODUCER_ASYNC_IN_MSG, msg);

		if (sendCallback instanceof SendCallbackEx) {
			SendCallbackEx sendCallbackEx = (SendCallbackEx) sendCallback;
			if (sendCallbackEx.getMessage() == null) {
				sendCallbackEx.setMessage(msg);
			}
		}

		try {
			if (mq != null) {
				if (timeout != null) {
					producer.send(msg, mq, sendCallback, timeout);
				} else {
					producer.send(msg, mq, sendCallback);
				}
			} else if (selector != null) {
				if (timeout != null) {
					producer.send(msg, selector, arg, sendCallback, timeout);
				} else {
					producer.send(msg, selector, arg, sendCallback);
				}
			} else {
				if (timeout != null) {
					producer.send(msg, sendCallback, timeout);
				} else {
					producer.send(msg, sendCallback);
				}
			}
			span.success();
		} catch (Exception e) {
			LOGGER.error(LOG_MQ_PRODUCER_ASYNC_EX_MSG, msg, e);
			throw e;
		} finally {
			LOGGER.info(LOG_MQ_PRODUCER_ASYNC_OUT_MSG, msg);
			span.close();
		}
	}

	private void onewaySend(Message msg, MessageQueue mq, MessageQueueSelector selector, Object arg)
			throws MQClientException, RemotingException, InterruptedException {
		Tracer buildTracer = LOGGER.buildTracer();
		Span span = buildTracer.createSpan(TRANS_TYPE_MQ_OW_PRODUCER, msg.getTopic());

		final String group = System.getenv(MQConstants.GROUP);
		if (StringUtils.isNotBlank(group) && !msg.getTopic().endsWith(MQConstants.UNDER_LINE + group)) {
			msg.setTopic(msg.getTopic() + MQConstants.UNDER_LINE + group);
		}

		LOGGER.info(LOG_MQ_PRODUCER_OW_IN_MSG, msg);

		try {
			if (mq != null) {
				producer.sendOneway(msg, mq);
			} else if (selector != null) {
				producer.sendOneway(msg, selector, arg);
			} else {
				producer.sendOneway(msg);
			}
			span.success();
		} catch (Exception e) {
			LOGGER.error(LOG_MQ_PRODUCER_OW_EX_MSG, msg, e);
			throw e;
		} finally {
			LOGGER.info(LOG_MQ_PRODUCER_OW_OUT_MSG, msg);
			span.close();
		}
	}

	@Override
	public SendResult send(Message msg) throws MQClientException, RemotingException, MQBrokerException,
			InterruptedException {
		return syncSend(msg, null, null, null, null);
	}

	public SendResult sendWithRetry(Message msg) {
		int times = 0;
		while (times < MAX_SEND_TIMES) {
			try {
				return this.send(msg);
			} catch (InterruptedException | RemotingException | MQClientException | MQBrokerException e) {
				LOGGER.warn("Send message error, e={} , try times:" + times, e);
				times++;
			}
		}
		LOGGER.buildTracer().createEmpty().addEvent(EVENT_TYPE_MQ_SYNC_SEND, msg.getTopic() + ":SENDFAILED");
		LOGGER.error(LOG_MQ_PRODUCER_SYNC_FINAL_SEND_FAILED_MSG, msg);
		return null;
	}

	@Override
	public SendResult send(Message msg, long timeout) throws MQClientException, RemotingException, MQBrokerException,
			InterruptedException {
		return syncSend(msg, null, null, null, timeout);
	}

	public SendResult sendWithRetry(Message msg, long timeout) {
		int times = 0;
		while (times < MAX_SEND_TIMES) {
			try {
				return this.send(msg, timeout);
			} catch (InterruptedException | RemotingException | MQClientException | MQBrokerException e) {
				LOGGER.warn("Send message error, e={} , try times:" + times, e);
				times++;
			}
		}

		LOGGER.buildTracer().createEmpty().addEvent(EVENT_TYPE_MQ_SYNC_SEND, msg.getTopic() + ":SENDFAILED");
		LOGGER.error(LOG_MQ_PRODUCER_SYNC_FINAL_SEND_FAILED_MSG, msg);
		return null;
	}

	@Override
	public void send(Message msg, SendCallback sendCallback) throws MQClientException, RemotingException,
			InterruptedException {
		asyncSend(msg, null, null, null, sendCallback, null);
	}

	@Override
	public void send(Message msg, SendCallback sendCallback, long timeout) throws MQClientException, RemotingException,
			InterruptedException {
		asyncSend(msg, null, null, null, sendCallback, timeout);
	}

	@Override
	public SendResult send(Message msg, MessageQueue mq) throws MQClientException, RemotingException,
			MQBrokerException, InterruptedException {
		return syncSend(msg, mq, null, null, null);
	}

	public SendResult sendWithRetry(Message msg, MessageQueue mq) {
		int times = 0;
		while (times < MAX_SEND_TIMES) {
			try {
				return this.send(msg, mq);
			} catch (InterruptedException | RemotingException | MQClientException | MQBrokerException e) {
				LOGGER.warn("Send message error, e={} , try times:" + times, e);
				times++;
			}
		}

		LOGGER.buildTracer().createEmpty().addEvent(EVENT_TYPE_MQ_SYNC_SEND, msg.getTopic() + ":SENDFAILED");
		LOGGER.error(LOG_MQ_PRODUCER_SYNC_FINAL_SEND_FAILED_MSG, msg);
		return null;
	}

	@Override
	public SendResult send(Message msg, MessageQueue mq, long timeout) throws MQClientException, RemotingException,
			MQBrokerException, InterruptedException {
		return syncSend(msg, mq, null, null, timeout);
	}

	public SendResult sendWithRetry(Message msg, MessageQueue mq, long timeout) {
		int times = 0;
		while (times < MAX_SEND_TIMES) {
			try {
				return this.send(msg, mq, timeout);
			} catch (InterruptedException | RemotingException | MQClientException | MQBrokerException e) {
				LOGGER.warn("Send message error, e={} , try times:" + times, e);
				times++;
			}
		}

		LOGGER.buildTracer().createEmpty().addEvent(EVENT_TYPE_MQ_SYNC_SEND, msg.getTopic() + ":SENDFAILED");
		LOGGER.error(LOG_MQ_PRODUCER_SYNC_FINAL_SEND_FAILED_MSG, msg);
		return null;
	}

	@Override
	public void send(Message msg, MessageQueue mq, SendCallback sendCallback) throws MQClientException,
			RemotingException, InterruptedException {
		asyncSend(msg, mq, null, null, sendCallback, null);
	}

	@Override
	public void send(Message msg, MessageQueue mq, SendCallback sendCallback, long timeout) throws MQClientException,
			RemotingException, InterruptedException {
		asyncSend(msg, mq, null, null, sendCallback, timeout);
	}

	@Override
	public SendResult send(Message msg, MessageQueueSelector selector, Object arg) throws MQClientException,
			RemotingException, MQBrokerException, InterruptedException {
		return syncSend(msg, null, selector, arg, null);
	}

	public SendResult sendWithRetry(Message msg, MessageQueueSelector selector, Object arg) {
		int times = 0;
		while (times < MAX_SEND_TIMES) {
			try {
				return this.send(msg, selector, arg);
			} catch (InterruptedException | RemotingException | MQClientException | MQBrokerException e) {
				LOGGER.warn("Send message error, e={} , try times:" + times, e);
				times++;
			}
		}

		LOGGER.buildTracer().createEmpty().addEvent(EVENT_TYPE_MQ_SYNC_SEND, msg.getTopic() + ":SENDFAILED");
		LOGGER.error(LOG_MQ_PRODUCER_SYNC_FINAL_SEND_FAILED_MSG, msg);
		return null;
	}

	@Override
	public SendResult send(Message msg, MessageQueueSelector selector, Object arg, long timeout)
			throws MQClientException, RemotingException, MQBrokerException, InterruptedException {
		return syncSend(msg, null, selector, arg, timeout);
	}

	public SendResult sendWithRetry(Message msg, MessageQueueSelector selector, Object arg, long timeout) {
		int times = 0;
		while (times < MAX_SEND_TIMES) {
			try {
				return this.send(msg, selector, arg, timeout);
			} catch (InterruptedException | RemotingException | MQClientException | MQBrokerException e) {
				LOGGER.warn("Send message error, e={} , try times:" + times, e);
				times++;
			}
		}

		LOGGER.buildTracer().createEmpty().addEvent(EVENT_TYPE_MQ_SYNC_SEND, msg.getTopic() + ":SENDFAILED");
		LOGGER.error(LOG_MQ_PRODUCER_SYNC_FINAL_SEND_FAILED_MSG, msg);
		return null;
	}

	@Override
	public void send(Message msg, MessageQueueSelector selector, Object arg, SendCallback sendCallback)
			throws MQClientException, RemotingException, InterruptedException {
		asyncSend(msg, null, selector, arg, sendCallback, null);
	}

	@Override
	public void send(Message msg, MessageQueueSelector selector, Object arg, SendCallback sendCallback, long timeout)
			throws MQClientException, RemotingException, InterruptedException {
		asyncSend(msg, null, selector, arg, sendCallback, timeout);
	}

	@Override
	public void sendOneway(Message msg) throws MQClientException, RemotingException, InterruptedException {
		onewaySend(msg, null, null, null);
	}

	@Override
	public void sendOneway(Message msg, MessageQueue mq) throws MQClientException, RemotingException,
			InterruptedException {
		onewaySend(msg, mq, null, null);
	}

	@Override
	public void sendOneway(Message msg, MessageQueueSelector selector, Object arg) throws MQClientException,
			RemotingException, InterruptedException {
		onewaySend(msg, null, selector, arg);
	}

	public String getLocalDBPath() {
		return localDBPath;
	}

	public void setLocalDBPath(String localDBPath) {
		this.localDBPath = localDBPath;
	}

	public KVManager getKvManager() {
		return kvManager;
	}

	public void setKvManager(KVManager kvManager) {
		this.kvManager = kvManager;
	}

}
