package com.eiff.framework.mq.rocketmq;

import java.util.Set;

import org.apache.rocketmq.client.consumer.DefaultMQPullConsumer;
import org.apache.rocketmq.client.consumer.PullResult;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.MessageQueue;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import com.eiff.framework.log.api.Constants;
import com.eiff.framework.log.api.HdLogger;
import com.eiff.framework.log.api.trace.Span;
import com.eiff.framework.log.api.trace.Tracer;
import com.eiff.framework.mq.rocketmq.callback.MessagePullListener;
import com.eiff.framework.mq.rocketmq.callback.MessagePullListener.ConsumePullStatus;

public class DefaultRMQPullConsumer extends DefaultMQPullConsumer implements FactoryBean<DefaultMQPullConsumer>,
		InitializingBean, DisposableBean, Constants {

	private static HdLogger LOGGER = HdLogger.getLogger(DefaultRMQPullConsumer.class);

	private DefaultMQPullConsumer consumer = new DefaultMQPullConsumer();

	private MessagePullListener messagePullListener;

	private int msgMaxNums = 1;

	@Override
	public DefaultMQPullConsumer getObject() throws Exception {
		return this;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.hasText(getNamesrvAddr(),
				"[Assertion failed] - namesrvAddr must have text; it must not be null, empty, or blank");
		Assert.hasText(getConsumerGroup(),
				"[Assertion failed] - consumerGroup must have text; it must not be null, empty, or blank");
		Assert.notNull(getMessagePullListener(),
				"[Assertion failed] - messagePullListener must have text; it must not be null, empty, or blank");

		consumer.setNamesrvAddr(getNamesrvAddr());
		consumer.setConsumerGroup(getConsumerGroup());
		consumer.setAllocateMessageQueueStrategy(getAllocateMessageQueueStrategy());
		consumer.setClientCallbackExecutorThreads(getClientCallbackExecutorThreads());
		consumer.setClientIP(getClientIP());
		consumer.setHeartbeatBrokerInterval(getHeartbeatBrokerInterval());
		consumer.setInstanceName(getInstanceName());
		consumer.setMessageModel(getMessageModel());
		consumer.setOffsetStore(getOffsetStore());
		consumer.setPersistConsumerOffsetInterval(getPersistConsumerOffsetInterval());
		consumer.setPollNameServerInterval(getPollNameServerInterval());
		consumer.setUnitMode(isUnitMode());

		consumer.start();
	}

	/**
	 * 遍历所有messagequeue，直到查找到消息
	 * 
	 * @param topic
	 * @throws MQClientException
	 */
	public void pullMsgs(String topic) throws MQClientException {
		Tracer buildTracer = LOGGER.buildTracer();
		Span span = buildTracer.createSpan(TRANS_TYPE_MQ_PULL_CONSUMER, topic);

		final Set<MessageQueue> messageQueues = consumer.fetchSubscribeMessageQueues(topic);
		if (messageQueues != null && messageQueues.size() > 0) {
			for (final MessageQueue messageQueue : messageQueues) {
				SINGLE_MQ: while (true) {
					try {
						long nextMessageQueueOffset = consumer.fetchConsumeOffset(messageQueue, false);
						if (nextMessageQueueOffset < 0) {
							nextMessageQueueOffset = 0;
						}

						final PullResult pullResult = consumer.pull(messageQueue, null, nextMessageQueueOffset,
								msgMaxNums);
						switch (pullResult.getPullStatus()) {
						case FOUND:
							MessagePullListener.ConsumePullStatus consumePullStatus = messagePullListener
									.consumeMessage(messageQueue, pullResult.getMsgFoundList());
							if (consumePullStatus != ConsumePullStatus.CONSUME_EXCEPTION) {
								consumer.updateConsumeOffset(messageQueue, pullResult.getNextBeginOffset());
							}

							span.addEvent(EVENT_TYPE_MQ_UPDATE_OFFSET, messageQueue.getBrokerName() + ":"
									+ messageQueue.getQueueId() + ":" + pullResult.getNextBeginOffset());
							span.close();
							return;
						case NO_MATCHED_MSG:
							consumer.updateConsumeOffset(messageQueue, pullResult.getNextBeginOffset());
							break;
						case NO_NEW_MSG:
							break SINGLE_MQ;
						case OFFSET_ILLEGAL:
							consumer.updateConsumeOffset(messageQueue, pullResult.getNextBeginOffset());
							break;
						default:
							break;
						}
					} catch (Exception e) {
						LOGGER.error(LOG_MQ_CONSUMER_PULL_EX_MSG, topic, e);
						span.close();
						return;
					}
				}
			}
		}

		span.addEvent(EVENT_TYPE_MQ_NO_MSG, topic);
		span.close();
	}

	@Override
	public void destroy() throws Exception {
		consumer.shutdown();
	}

	@Override
	public Class<?> getObjectType() {
		return DefaultMQPullConsumer.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	public DefaultMQPullConsumer getConsumer() {
		return consumer;
	}

	public int getMsgMaxNums() {
		return msgMaxNums;
	}

	public void setMsgMaxNums(int msgMaxNums) {
		this.msgMaxNums = msgMaxNums;
	}

	public MessagePullListener getMessagePullListener() {
		return messagePullListener;
	}

	public void setMessagePullListener(MessagePullListener messagePullListener) {
		this.messagePullListener = messagePullListener;
	}

}
