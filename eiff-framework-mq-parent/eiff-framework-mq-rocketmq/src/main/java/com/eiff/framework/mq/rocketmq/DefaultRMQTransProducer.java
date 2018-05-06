package com.eiff.framework.mq.rocketmq;

import java.util.Map;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.Validators;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.LocalTransactionExecuter;
import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.MessageQueueSelector;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.apache.rocketmq.client.producer.TransactionSendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageAccessor;
import org.apache.rocketmq.common.message.MessageConst;
import org.apache.rocketmq.common.message.MessageDecoder;
import org.apache.rocketmq.common.message.MessageId;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.common.protocol.header.EndTransactionRequestHeader;
import org.apache.rocketmq.common.sysflag.MessageSysFlag;
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
import com.eiff.framework.mq.rocketmq.exception.MQTransactionException;

public class DefaultRMQTransProducer extends TransactionMQProducer implements FactoryBean<TransactionMQProducer>,
		InitializingBean, DisposableBean, Constants {

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

		producer.setCheckRequestHoldMax(getCheckRequestHoldMax());
		producer.setCheckThreadPoolMaxSize(getCheckThreadPoolMaxSize());
		producer.setCheckThreadPoolMinSize(getCheckThreadPoolMinSize());
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
		producer.setTransactionCheckListener(getTransactionCheckListener());
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
			span.failed(e);
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

	@Override
	public SendResult send(Message msg, long timeout) throws MQClientException, RemotingException, MQBrokerException,
			InterruptedException {
		return syncSend(msg, null, null, null, timeout);
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

	@Override
	public SendResult send(Message msg, MessageQueue mq, long timeout) throws MQClientException, RemotingException,
			MQBrokerException, InterruptedException {
		return syncSend(msg, mq, null, null, timeout);
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

	@Override
	public TransactionSendResult sendMessageInTransaction(Message msg, LocalTransactionExecuter tranExecuter, Object arg)
			throws MQClientException {
		return producer.sendMessageInTransaction(msg, tranExecuter, arg);
	}

	/**
	 * 预发消息，预发消息失败抛出MQTransactionException异常
	 * 
	 * @param msg
	 * @param tranExecuter
	 * @param arg
	 * @return
	 * @throws MQTransactionException
	 */
	public SendResult prepareTransaction(Message msg) throws MQTransactionException {
		if (null == producer.getTransactionCheckListener()) {
			throw new MQTransactionException("localTransactionBranchCheckListener is null");
		}

		return sendPrepareMessageInTransaction(msg);
	}

	/**
	 * oneway方式，根据本地事务执行结果确定事务消息正常发出
	 * 
	 * @param transactionPrepareResult
	 * @return
	 * @throws MQTransactionException
	 */
	public TransactionSendResult commitTransaction(final SendResult sendResult) throws MQTransactionException {
		if (null == sendResult) {
			throw new MQTransactionException("sendResult is null");
		}

		return confirmTransaction(sendResult, LocalTransactionState.COMMIT_MESSAGE, null);
	}

	/**
	 * 
	 * oneway方式，根据本地事务执行结果回滚事务消息
	 * 
	 * @param sendResult
	 * @param localExecuteException
	 * @return
	 * @throws MQTransactionException
	 */
	public TransactionSendResult rollbackTransaction(final SendResult sendResult, final Throwable localExecuteException)
			throws MQTransactionException {
		if (null == sendResult) {
			throw new MQTransactionException("sendResult is null");
		}

		return confirmTransaction(sendResult, LocalTransactionState.ROLLBACK_MESSAGE, localExecuteException);
	}

	/**
	 * oneway方式，不知道本地事务执行结果，等待了一段时间没有结果，可以调用这个方法，等待mq broker回查producer
	 * 
	 * @param sendResult
	 * @return
	 * @throws MQTransactionException
	 */
	public TransactionSendResult unknowTransaction(final SendResult sendResult) throws MQTransactionException {
		if (null == sendResult) {
			throw new MQTransactionException("sendResult is null");
		}

		return confirmTransaction(sendResult, LocalTransactionState.UNKNOW, null);
	}

	private TransactionSendResult confirmTransaction(SendResult sendResult,
			LocalTransactionState localTransactionState, Throwable localException) {
		try {
			final MessageId id = MessageDecoder.decodeMessageId(sendResult.getMsgId());
			final String transactionId = sendResult.getTransactionId();
			final String brokerAddr = this.producer.getDefaultMQProducerImpl().getmQClientFactory()
					.findBrokerAddressInPublish(sendResult.getMessageQueue().getBrokerName());

			EndTransactionRequestHeader requestHeader = new EndTransactionRequestHeader();
			requestHeader.setTransactionId(transactionId);
			requestHeader.setCommitLogOffset(id.getOffset());
			switch (localTransactionState) {
			case COMMIT_MESSAGE:
				requestHeader.setCommitOrRollback(MessageSysFlag.TRANSACTION_COMMIT_TYPE);
				break;
			case ROLLBACK_MESSAGE:
				requestHeader.setCommitOrRollback(MessageSysFlag.TRANSACTION_ROLLBACK_TYPE);
				break;
			case UNKNOW:
				requestHeader.setCommitOrRollback(MessageSysFlag.TRANSACTION_NOT_TYPE);
				break;
			default:
				break;
			}

			requestHeader.setProducerGroup(this.getProducerGroup());

			// 预发消息的queueOffset，其实是0，因为预发消息不进入ConsumeQueue
			requestHeader.setTranStateTableOffset(sendResult.getQueueOffset());
			requestHeader.setMsgId(sendResult.getMsgId());
			String remark = localException != null ? ("executeLocalTransactionBranch exception: " + localException
					.toString()) : null;
			this.producer.getDefaultMQProducerImpl().getmQClientFactory().getMQClientAPIImpl()
					.endTransactionOneway(brokerAddr, requestHeader, remark, this.getSendMsgTimeout());
		} catch (Throwable e) {
			LOGGER.warn("confirmTransaction, local transaction execute " + localTransactionState
					+ ", but end broker transaction failed", e);
		}

		TransactionSendResult transactionSendResult = new TransactionSendResult();
		transactionSendResult.setSendStatus(sendResult.getSendStatus());
		transactionSendResult.setMessageQueue(sendResult.getMessageQueue());
		transactionSendResult.setMsgId(sendResult.getMsgId());
		transactionSendResult.setQueueOffset(sendResult.getQueueOffset());
		transactionSendResult.setTransactionId(sendResult.getTransactionId());
		transactionSendResult.setLocalTransactionState(localTransactionState);
		return transactionSendResult;
	}

	private SendResult sendPrepareMessageInTransaction(final Message msg) throws MQTransactionException {
		try {
			Validators.checkMessage(msg, this.producer);
		} catch (MQClientException e) {
			throw new MQTransactionException("send transaction prepare message checkMessage Exception", e);
		}

		SendResult sendResult = null;
		MessageAccessor.putProperty(msg, MessageConst.PROPERTY_TRANSACTION_PREPARED, "true");
		MessageAccessor.putProperty(msg, MessageConst.PROPERTY_PRODUCER_GROUP, this.producer.getProducerGroup());
		try {
			sendResult = this.send(msg);
		} catch (Exception e) {
			throw new MQTransactionException("send transaction prepare message Exception", e);
		}

		switch (sendResult.getSendStatus()) {
		case SEND_OK:
			break;
		case FLUSH_DISK_TIMEOUT:
		case FLUSH_SLAVE_TIMEOUT:
		case SLAVE_NOT_AVAILABLE:
			throw new MQTransactionException("send transaction prepare message Exception " + sendResult.getSendStatus());
			// 如果预发消息因为刷盘超时、同步到slave超时、slave当前不可用导致发送消息失败，直接回滚预发消息
		default:
			throw new MQTransactionException("send transaction prepare message Exception " + sendResult.getSendStatus());
		}

		return sendResult;
	}

}
