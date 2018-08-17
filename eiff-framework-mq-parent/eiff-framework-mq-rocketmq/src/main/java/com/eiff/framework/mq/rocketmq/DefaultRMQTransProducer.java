package com.eiff.framework.mq.rocketmq;

import java.util.Map;
import java.util.concurrent.Executors;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.MessageQueueSelector;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.apache.rocketmq.client.producer.TransactionSendResult;
import org.apache.rocketmq.common.ThreadFactoryImpl;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import com.eiff.framework.log.api.Constants;
import com.eiff.framework.log.api.HdLogger;
import com.eiff.framework.log.api.trace.Span;
import com.eiff.framework.log.api.trace.Tracer;
import com.eiff.framework.mq.rocketmq.callback.SendCallbackEx;
import com.eiff.framework.mq.rocketmq.common.MQConstants;
import com.eiff.framework.mq.rocketmq.common.MessageHashCodeUtils;
import com.eiff.framework.mq.rocketmq.exception.MQAsyncProducerReturnNullException;

public class DefaultRMQTransProducer extends TransactionMQProducer
		implements FactoryBean<TransactionMQProducer>, InitializingBean, DisposableBean, Constants {

	final static HdLogger LOGGER = HdLogger.getLogger(DefaultRMQTransProducer.class);

	private TransactionMQProducer producer = new TransactionMQProducer();

	@Override
	public TransactionMQProducer getObject() throws Exception {
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

		producer.setExecutorService(Executors.newFixedThreadPool(10, new ThreadFactoryImpl("checkProducerThread_")));
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
		producer.setTransactionListener(getTransactionListener());
		producer.setUnitMode(isUnitMode());

		producer.start();
	}

	@Override
	public void destroy() throws Exception {
		producer.shutdown();
	}

	@Override
	public Class<?> getObjectType() {
		return DefaultRMQTransProducer.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	public TransactionMQProducer getProducer() {
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
				} // else if (sendStatus.equals(SendStatus.FLUSH_SLAVE_TIMEOUT))
				else {
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

	@Override
	public SendResult send(Message msg)
			throws MQClientException, RemotingException, MQBrokerException, InterruptedException {
		return syncSend(msg, null, null, null, null);
	}

	@Override
	public SendResult send(Message msg, long timeout)
			throws MQClientException, RemotingException, MQBrokerException, InterruptedException {
		return syncSend(msg, null, null, null, timeout);
	}

	@Override
	public void send(Message msg, SendCallback sendCallback)
			throws MQClientException, RemotingException, InterruptedException {
		asyncSend(msg, null, null, null, sendCallback, null);
	}

	@Override
	public void send(Message msg, SendCallback sendCallback, long timeout)
			throws MQClientException, RemotingException, InterruptedException {
		asyncSend(msg, null, null, null, sendCallback, timeout);
	}

	@Override
	public SendResult send(Message msg, MessageQueue mq)
			throws MQClientException, RemotingException, MQBrokerException, InterruptedException {
		return syncSend(msg, mq, null, null, null);
	}

	@Override
	public SendResult send(Message msg, MessageQueue mq, long timeout)
			throws MQClientException, RemotingException, MQBrokerException, InterruptedException {
		return syncSend(msg, mq, null, null, timeout);
	}

	@Override
	public void send(Message msg, MessageQueue mq, SendCallback sendCallback)
			throws MQClientException, RemotingException, InterruptedException {
		asyncSend(msg, mq, null, null, sendCallback, null);
	}

	@Override
	public void send(Message msg, MessageQueue mq, SendCallback sendCallback, long timeout)
			throws MQClientException, RemotingException, InterruptedException {
		asyncSend(msg, mq, null, null, sendCallback, timeout);
	}

	@Override
	public SendResult send(Message msg, MessageQueueSelector selector, Object arg)
			throws MQClientException, RemotingException, MQBrokerException, InterruptedException {
		return syncSend(msg, null, selector, arg, null);
	}

	@Override
	public SendResult send(Message msg, MessageQueueSelector selector, Object arg, long timeout)
			throws MQClientException, RemotingException, MQBrokerException, InterruptedException {
		return syncSend(msg, null, selector, arg, timeout);
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
	public TransactionSendResult sendMessageInTransaction(final Message msg, final Object arg)
			throws MQClientException {
		return producer.sendMessageInTransaction(msg, arg);
	}

}
