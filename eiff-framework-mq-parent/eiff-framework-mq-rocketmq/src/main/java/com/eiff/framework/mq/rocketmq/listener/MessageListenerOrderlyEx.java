package com.eiff.framework.mq.rocketmq.listener;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerOrderly;
import org.apache.rocketmq.common.message.MessageExt;

import com.eiff.framework.log.api.HdLogger;
import com.eiff.framework.log.api.trace.Span;
import com.eiff.framework.log.api.trace.Tracer;
import com.eiff.framework.mq.rocketmq.common.MessageConverterUtils;
import com.eiff.framework.mq.rocketmq.exception.MQConsumerReturnNullException;
import com.eiff.framework.mq.rocketmq.listener.support.JedisHelper;

public abstract class MessageListenerOrderlyEx extends JedisHelper implements MessageListenerOrderly {

	private static HdLogger LOGGER = HdLogger.getLogger(MessageListenerOrderlyEx.class);

	@Override
	public ConsumeOrderlyStatus consumeMessage(List<MessageExt> msgs, ConsumeOrderlyContext context) {
		String broker = context.getMessageQueue().getBrokerName();
		int queueId = context.getMessageQueue().getQueueId();
		String topic = context.getMessageQueue().getTopic();
		Tracer buildTracer = LOGGER.buildTracer();
		if (CollectionUtils.isNotEmpty(msgs)) {
			String messageId = msgs.get(0).getProperty(TRACE_CHILD + 1);
			String parentId = msgs.get(0).getProperty(TRACE_PARENT + 1);
			String rootId = msgs.get(0).getProperty(TRACE_ROOT);
			if (StringUtils.isNotBlank(messageId) && StringUtils.isNotBlank(parentId) && StringUtils.isNotBlank(rootId)) {
				buildTracer.buildContext(messageId, parentId, rootId);
			}
		}

		Span span = buildTracer.createSpan(TRANS_TYPE_MQ_CONSUMER, topic + ":" + broker + ":" + queueId);
		LOGGER.logTraceInfoTMDC();
		LOGGER.info(LOG_MQ_CONSUMER_IN_MSG, topic, broker, queueId);

		List<MessageExt> messageExtWrappers = new LinkedList<MessageExt>();
		if (CollectionUtils.isNotEmpty(msgs)) {
			for (MessageExt messageExt : msgs) {
				LOGGER.info(LOG_MQ_CONSUMER_MSG_MSG, topic, messageExt);

				if (messageExt != null) {
					if (!isRepeatMessage(messageExt)) {
						messageExtWrappers.add(MessageConverterUtils.conver2MessageExtWrapper(messageExt));
					} else {
						LOGGER.warn("MQ_CON_REPEAT_MSG {} REPEAT MSG WILL NOT BE CONSUMED {}", topic, messageExt);
					}
				}
			}
		}

		ConsumeOrderlyStatus status = null;
		try {
			if (messageExtWrappers.size() > 0) {
				try {
					for (MessageExt messageExt : messageExtWrappers) {
						span.addEvent(TRANS_TYPE_MQ_CONSUMER_FROM, messageExt.getBornHostString());
					}
				} catch (Throwable e) {
				}
			}
			status = consume(messageExtWrappers, context);
			if (status != null) {
				if (status.equals(ConsumeOrderlyStatus.SUCCESS)) {
					LOGGER.info(LOG_MQ_CONSUMER_SUCCESS_MSG, topic, broker, queueId);
					span.addEvent(TRANS_TYPE_MQ_CONSUMER, ConsumeOrderlyStatus.SUCCESS.name());
				} else {
					LOGGER.info(LOG_MQ_CONSUMER_SUSPEND_MSG, topic, broker, queueId);
					span.addEvent(TRANS_TYPE_MQ_CONSUMER, ConsumeOrderlyStatus.SUSPEND_CURRENT_QUEUE_A_MOMENT.name());
				}
			} else {
				LOGGER.info(LOG_MQ_CONSUMER_RETURNNULL_MSG, topic, broker, queueId);
				span.failed(new MQConsumerReturnNullException());
			}

			span.success();
			return status;
		} catch (Throwable e) {
			LOGGER.error(LOG_MQ_CONSUMER_EX_MSG, topic, broker, queueId, e);
			throw e;
		} finally {
			LOGGER.info(LOG_MQ_CONSUMER_OUT_MSG, topic, broker, queueId);
			span.close();
			LOGGER.cleanTraceInfoInMDC();
		}
	}

	public abstract ConsumeOrderlyStatus consume(List<MessageExt> msgs, ConsumeOrderlyContext context);

}
