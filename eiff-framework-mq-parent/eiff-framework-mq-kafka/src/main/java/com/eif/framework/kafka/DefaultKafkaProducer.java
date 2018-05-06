package com.eif.framework.kafka;

import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;

public class DefaultKafkaProducer extends KafkaProducer<Object, Object> implements
		FactoryBean<KafkaProducer<Object, Object>>, DisposableBean {

	private static Logger LOGGER = LoggerFactory.getLogger(DefaultKafkaProducer.class);

	public DefaultKafkaProducer(Properties properties) {
		super(properties);
	}

	@Override
	public KafkaProducer<Object, Object> getObject() throws Exception {
		return this;
	}

	@Override
	public void destroy() throws Exception {
		this.close();
	}

	@Override
	public Class<?> getObjectType() {
		return KafkaProducer.class;
	}

	@Override
	public boolean isSingleton() {
		return false;
	}

	public RecordMetadata send(String topic, Integer partition, Object key, Object value) throws InterruptedException,
			ExecutionException {
		final ProducerRecord<Object, Object> record = new ProducerRecord<Object, Object>(topic, partition, key, value);
		return syncSendRecord(record);
	}

	public RecordMetadata send(String topic, Object key, Object value) throws InterruptedException, ExecutionException {
		final ProducerRecord<Object, Object> record = new ProducerRecord<Object, Object>(topic, key, value);
		return syncSendRecord(record);
	}

	public RecordMetadata send(String topic, Object value) throws InterruptedException, ExecutionException {
		final ProducerRecord<Object, Object> record = new ProducerRecord<Object, Object>(topic, value);
		return syncSendRecord(record);
	}

	private RecordMetadata syncSendRecord(final ProducerRecord<Object, Object> record) throws InterruptedException,
			ExecutionException {
		LOGGER.info("KAFKA_PRO_SYN_IN {}", record);

		try {
			Future<RecordMetadata> future = this.send(record);
			this.flush();

			final RecordMetadata recordMetadata = future.get();
			LOGGER.info("KAFKA_PRO_SYN_OUT {}", recordMetadata);
			return recordMetadata;
		} catch (Exception e) {
			LOGGER.error("KAFKA_PRO_SYN_EX {}", record, e);
			throw e;
		} finally {
			LOGGER.info("KAFKA_PRO_SYN_OUT {}", record);
		}
	}

	private void asyncSendRecord(final ProducerRecord<Object, Object> record, Callback callback)
			throws InterruptedException, ExecutionException {
		LOGGER.info("KAFKA_PRO_ASYN_IN {}", record);

		try {
			this.send(record, callback);
		} catch (Exception e) {
			LOGGER.error("KAFKA_PRO_ASYN_EX {}", record, e);
			throw e;
		} finally {
			LOGGER.info("KAFKA_PRO_ASYN_OUT {}", record);
		}
	}

	public void asyncSend(String topic, Integer partition, Object key, Object value, Callback callback)
			throws InterruptedException, ExecutionException {
		final ProducerRecord<Object, Object> record = new ProducerRecord<Object, Object>(topic, partition, key, value);
		asyncSendRecord(record, callback);
	}

	public void asyncSend(String topic, Object key, Object value, Callback callback) throws InterruptedException,
			ExecutionException {
		final ProducerRecord<Object, Object> record = new ProducerRecord<Object, Object>(topic, key, value);
		asyncSendRecord(record, callback);
	}

	public void asyncSend(String topic, Object value, Callback callback) throws InterruptedException,
			ExecutionException {
		final ProducerRecord<Object, Object> record = new ProducerRecord<Object, Object>(topic, value);
		asyncSendRecord(record, callback);
	}

}
