package com.eif.framework.kafka;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import com.eif.framework.kafka.listener.KafkaMessageListener;

public class DefaultKafkaConsumer extends KafkaConsumer<Object, Object> implements
		FactoryBean<KafkaConsumer<Object, Object>>, InitializingBean, DisposableBean {

	private KafkaMessageListener messageListener;

	private List<String> topics = new ArrayList<String>();

	public DefaultKafkaConsumer(Properties properties) {
		super(properties);
	}

	@Override
	public KafkaConsumer<Object, Object> getObject() throws Exception {
		return this;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(messageListener);
		Assert.notEmpty(topics);

		this.subscribe(topics);

		while (true) {
			final ConsumerRecords<Object, Object> consumerRecords = this.poll(200);
			if (consumerRecords.isEmpty()) {
				continue;
			}

			messageListener.consume(consumerRecords);
		}
	}

	@Override
	public void destroy() throws Exception {
		this.close();
	}

	@Override
	public Class<?> getObjectType() {
		return KafkaConsumer.class;
	}

	@Override
	public boolean isSingleton() {
		return false;
	}

	public List<String> getTopics() {
		return topics;
	}

	public void setTopics(List<String> topics) {
		this.topics = topics;
	}

	public void setMessageListener(KafkaMessageListener messageListener) {
		this.messageListener = messageListener;
	}

}
