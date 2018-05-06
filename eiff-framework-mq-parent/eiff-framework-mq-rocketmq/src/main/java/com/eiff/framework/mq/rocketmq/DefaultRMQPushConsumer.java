package com.eiff.framework.mq.rocketmq;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.consumer.listener.MessageListenerOrderly;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import com.eiff.framework.mq.rocketmq.common.MQConstants;

public class DefaultRMQPushConsumer extends DefaultMQPushConsumer implements FactoryBean<DefaultMQPushConsumer>,
		InitializingBean, DisposableBean {

	private DefaultMQPushConsumer consumer = new DefaultMQPushConsumer();
	private Map<String, String> topics = new HashMap<String, String>();

	@Override
	public DefaultMQPushConsumer getObject() throws Exception {
		return this;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		final String group = System.getenv(MQConstants.GROUP);
		boolean grayFlag = false;
		if (StringUtils.isNotBlank(group)) {
			grayFlag = true;
		}

		consumer.setNamesrvAddr(getNamesrvAddr());

		if (grayFlag) {
			consumer.setConsumerGroup(getConsumerGroup() + MQConstants.UNDER_LINE + group);
		} else {
			consumer.setConsumerGroup(getConsumerGroup());
		}

		Assert.hasText(getNamesrvAddr(),
				"[Assertion failed] - namesrvAddr must have text; it must not be null, empty, or blank");
		Assert.hasText(getConsumerGroup(),
				"[Assertion failed] - consumerGroup must have text; it must not be null, empty, or blank");
		Assert.notEmpty(topics, "[Assertion failed] - topics must have text; it must not be null, empty, or blank");
		Assert.notNull(getMessageListener(),
				"[Assertion failed] - messageListener must have text; it must not be null, empty, or blank");

		consumer.setAdjustThreadPoolNumsThreshold(getAdjustThreadPoolNumsThreshold());
		consumer.setAllocateMessageQueueStrategy(getAllocateMessageQueueStrategy());
		consumer.setClientCallbackExecutorThreads(getClientCallbackExecutorThreads());
		consumer.setClientIP(getClientIP());
		consumer.setConsumeConcurrentlyMaxSpan(getConsumeConcurrentlyMaxSpan());
		consumer.setConsumeFromWhere(getConsumeFromWhere());
		consumer.setConsumeMessageBatchMaxSize(getConsumeMessageBatchMaxSize());
		consumer.setConsumeThreadMax(getConsumeThreadMax());
		consumer.setConsumeThreadMin(getConsumeThreadMin());
		consumer.setConsumeTimestamp(getConsumeTimestamp());
		consumer.setHeartbeatBrokerInterval(getHeartbeatBrokerInterval());
		consumer.setInstanceName(getInstanceName());
		consumer.setMessageListener(getMessageListener());
		consumer.setMessageModel(getMessageModel());
		consumer.setOffsetStore(getOffsetStore());
		consumer.setPersistConsumerOffsetInterval(getPersistConsumerOffsetInterval());
		consumer.setPollNameServerInterval(getPollNameServerInterval());
		consumer.setPostSubscriptionWhenPull(isPostSubscriptionWhenPull());
		consumer.setPullBatchSize(getPullBatchSize());
		consumer.setPullInterval(getPullInterval());
		consumer.setPullThresholdForQueue(getPullThresholdForQueue());
		consumer.setSubscription(getSubscription());
		consumer.setUnitMode(isUnitMode());

		for (String topic : topics.keySet()) {
			if (grayFlag) {
				consumer.subscribe(topic + MQConstants.UNDER_LINE + group, topics.get(topic));
			} else {
				consumer.subscribe(topic, topics.get(topic));
			}
		}

		if (getMessageListener() instanceof MessageListenerConcurrently) {
			consumer.registerMessageListener((MessageListenerConcurrently) getMessageListener());
		} else {
			consumer.registerMessageListener((MessageListenerOrderly) getMessageListener());
		}

		consumer.start();
	}

	@Override
	public void destroy() throws Exception {
		consumer.shutdown();
	}

	@Override
	public Class<?> getObjectType() {
		return DefaultRMQPushConsumer.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	public DefaultMQPushConsumer getConsumer() {
		return consumer;
	}

	public Map<String, String> getTopics() {
		return topics;
	}

	public void setTopics(Map<String, String> topics) {
		this.topics = topics;
	}
}
