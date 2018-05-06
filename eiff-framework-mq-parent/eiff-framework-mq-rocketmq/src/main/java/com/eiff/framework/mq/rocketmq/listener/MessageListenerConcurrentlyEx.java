package com.eiff.framework.mq.rocketmq.listener;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;

import com.eiff.framework.log.api.HdLogger;
import com.eiff.framework.log.api.trace.Span;
import com.eiff.framework.log.api.trace.Tracer;
import com.eiff.framework.mq.rocketmq.common.MQConstants;
import com.eiff.framework.mq.rocketmq.common.MessageConverterUtils;
import com.eiff.framework.mq.rocketmq.exception.MessageListenerConcurrentlyException;
import com.eiff.framework.mq.rocketmq.listener.support.JedisHelper;

public abstract class MessageListenerConcurrentlyEx extends JedisHelper implements MessageListenerConcurrently {

	private static HdLogger LOGGER = HdLogger.getLogger(MessageListenerConcurrentlyEx.class);

	private int retryTimes = MQConstants.CONSUME_RETRY_TIMES;

	@Override
	public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
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

		try {
			if (messageExtWrappers.size() > 0) {
				try {
					for (MessageExt messageExt : messageExtWrappers) {
						span.addEvent(TRANS_TYPE_MQ_CONSUMER_FROM, messageExt.getBornHostString());
					}
				} catch (Throwable e) {
				}
				consume(messageExtWrappers, context);
			}

			LOGGER.info(LOG_MQ_CONSUMER_SUCCESS_MSG, topic, broker, queueId);
			span.addEvent(TRANS_TYPE_MQ_CONSUMER, ConsumeConcurrentlyStatus.CONSUME_SUCCESS.name());
			span.success();
			return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
		} catch (MessageListenerConcurrentlyException e) {
			LOGGER.error(LOG_MQ_CONSUMER_EX_MSG, topic, broker, queueId, e);
			throw e;
		} catch (Throwable e) {
			LOGGER.error(LOG_MQ_CONSUMER_EX_MSG, topic, broker, queueId, e);
			LOGGER.info(LOG_MQ_CONSUMER_RECONSUME_MSG, topic, broker, queueId);
			span.failed(e);
			span.addEvent(TRANS_TYPE_MQ_CONSUMER, ConsumeConcurrentlyStatus.RECONSUME_LATER.name());

			if (CollectionUtils.isNotEmpty(msgs)) {
				if (msgs.get(0).getDelayTimeLevel() >= 2 + retryTimes) {
					context.setDelayLevelWhenNextConsume(-1);
				}
			}

			return ConsumeConcurrentlyStatus.RECONSUME_LATER;
		} finally {
			LOGGER.info(LOG_MQ_CONSUMER_OUT_MSG, topic, broker, queueId);
			span.close();
			LOGGER.cleanTraceInfoInMDC();
		}
	}

	public abstract void consume(List<MessageExt> msgs, ConsumeConcurrentlyContext context);

	public void setRetryTimes(int retryTimes) {
		this.retryTimes = retryTimes;
	}

}
