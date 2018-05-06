package com.eiff.framework.mq.rocketmq.callback;

import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.common.message.Message;
import org.slf4j.MDC;

import com.eiff.framework.log.api.Constants;
import com.eiff.framework.log.api.HdLogger;
import com.eiff.framework.log.api.trace.Span;
import com.eiff.framework.log.api.trace.Tracer;
import com.eiff.framework.mq.rocketmq.exception.MQAsyncProducerReturnNullException;

public abstract class SendCallbackEx implements SendCallback, Constants {

	private static HdLogger LOGGER = HdLogger.getLogger(SendCallbackEx.class);

	private Message message;

	public SendCallbackEx() {
	}

	public SendCallbackEx(Message message) {
		this.message = message;
	}

	public void setMessage(Message message) {
		this.message = message;
	}

	public Message getMessage() {
		return message;
	}

	@Override
	public void onSuccess(SendResult sendResult) {
		Tracer buildTracer = LOGGER.buildTracer();
		Span span = buildTracer.createEmpty();
		if (message != null) {
			String messageId = message.getProperty(TRACE_CHILD);
			String parentId = message.getProperty(TRACE_PARENT);
			String rootId = message.getProperty(TRACE_ROOT);
			if (StringUtils.isNotBlank(messageId) && StringUtils.isNotBlank(parentId) && StringUtils.isNotBlank(rootId)) {
				buildTracer.buildContext(messageId, parentId, rootId);
			}

			span = buildTracer.createSpan(TRANS_TYPE_MQ_ASYNC_PRODUCER_CALLBACK, TRANS_TYPE_MQ_ASYNC_PRODUCER_CALLBACK);
			MDC.put(TRACE_ID_PREFIX, buildTracer.getTraceId());
			LOGGER.info(LOG_MQ_PRODUCER_ASYNC_CALLBACK_IN_MSG, message);
		} else {
			span = buildTracer.createSpan(TRANS_TYPE_MQ_ASYNC_PRODUCER_CALLBACK, StringUtils.EMPTY);
			MDC.put(TRACE_ID_PREFIX, buildTracer.getTraceId());
			LOGGER.info(LOG_MQ_PRODUCER_ASYNC_CALLBACK_IN_MSG, "NULL");
		}

		if (sendResult != null && sendResult.getSendStatus() != null) {
			SendStatus sendStatus = sendResult.getSendStatus();
			if (sendStatus.equals(SendStatus.SEND_OK)) {
				LOGGER.info(LOG_MQ_PRODUCER_ASYNC_CALLBACK_OK_MSG, sendResult);
				span.addEvent(TRANS_TYPE_MQ_ASYNC_PRODUCER_CALLBACK, SendStatus.SEND_OK.name());
			} else if (sendStatus.equals(SendStatus.SLAVE_NOT_AVAILABLE)) {
				LOGGER.info(LOG_MQ_PRODUCER_ASYNC_CALLBACK_SNA_MSG, sendResult);
				span.addEvent(TRANS_TYPE_MQ_ASYNC_PRODUCER_CALLBACK, SendStatus.SLAVE_NOT_AVAILABLE.name());
			} else if (sendStatus.equals(SendStatus.FLUSH_DISK_TIMEOUT)) {
				LOGGER.info(LOG_MQ_PRODUCER_ASYNC_CALLBACK_FDT_MSG, sendResult);
				span.addEvent(TRANS_TYPE_MQ_ASYNC_PRODUCER_CALLBACK, SendStatus.FLUSH_DISK_TIMEOUT.name());
			} else if (sendStatus.equals(SendStatus.FLUSH_SLAVE_TIMEOUT)) {
				LOGGER.info(LOG_MQ_PRODUCER_ASYNC_CALLBACK_FST_MSG, sendResult);
				span.addEvent(TRANS_TYPE_MQ_ASYNC_PRODUCER_CALLBACK, SendStatus.FLUSH_SLAVE_TIMEOUT.name());
			}
		} else {
			if (message != null) {
				LOGGER.info(LOG_MQ_PRODUCER_ASYNC_CALLBACK_RETURNNULL_MSG, message);
			}
			span.failed(new MQAsyncProducerReturnNullException());
		}

		try {
			success(sendResult);
			span.success();
		} catch (Throwable e) {
			if (message != null) {
				LOGGER.error(LOG_MQ_PRODUCER_ASYNC_CALLBACK_EX_MSG, message, e);
			} else {
				LOGGER.error(LOG_MQ_PRODUCER_ASYNC_CALLBACK_EX_MSG, "NULL", e);
			}
			throw e;
		} finally {
			if (message != null) {
				LOGGER.info(LOG_MQ_PRODUCER_ASYNC_CALLBACK_OUT_MSG, message);
			}
			span.close();
			MDC.remove(TRACE_ID_PREFIX);
		}
	}

	public abstract void success(SendResult sendResult);

	@Override
	public void onException(Throwable e) {
		Tracer buildTracer = LOGGER.buildTracer();
		Span span = buildTracer.createEmpty();
		if (message != null) {
			String messageId = message.getProperty(TRACE_CHILD);
			String parentId = message.getProperty(TRACE_PARENT);
			String rootId = message.getProperty(TRACE_ROOT);
			if (StringUtils.isNotBlank(messageId) && StringUtils.isNotBlank(parentId) && StringUtils.isNotBlank(rootId)) {
				buildTracer.buildContext(messageId, parentId, rootId);
			}

			span = buildTracer.createSpan(TRANS_TYPE_MQ_ASYNC_PRODUCER_CALLBACK, TRANS_TYPE_MQ_ASYNC_PRODUCER_CALLBACK);
			MDC.put(TRACE_ID_PREFIX, buildTracer.getTraceId());
			LOGGER.error(LOG_MQ_PRODUCER_ASYNC_CALLBACK_EX_MSG, message, e);
		} else {
			span = buildTracer.createSpan(TRANS_TYPE_MQ_ASYNC_PRODUCER_CALLBACK, StringUtils.EMPTY);
			MDC.put(TRACE_ID_PREFIX, buildTracer.getTraceId());
			LOGGER.error(LOG_MQ_PRODUCER_ASYNC_CALLBACK_EX_MSG, "NULL", e);
		}
		span.failed(e);
		span.close();
		try {
			exception(e);
		} finally {
			MDC.remove(TRACE_ID_PREFIX);
		}
	}

	public abstract void exception(Throwable e);

}
