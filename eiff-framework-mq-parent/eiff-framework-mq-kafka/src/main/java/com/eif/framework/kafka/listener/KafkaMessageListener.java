package com.eif.framework.kafka.listener;

import org.apache.kafka.clients.consumer.ConsumerRecords;

public interface KafkaMessageListener {

	public void consume(final ConsumerRecords<Object, Object> consumerRecords);

}
