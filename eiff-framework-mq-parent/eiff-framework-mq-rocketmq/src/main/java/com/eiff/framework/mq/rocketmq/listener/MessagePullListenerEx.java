package com.eiff.framework.mq.rocketmq.listener;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.message.MessageQueue;

import com.eiff.framework.log.api.HdLogger;
import com.eiff.framework.log.api.trace.Span;
import com.eiff.framework.log.api.trace.Tracer;
import com.eiff.framework.mq.rocketmq.callback.MessagePullListener;
import com.eiff.framework.mq.rocketmq.common.MQConstants;
import com.eiff.framework.mq.rocketmq.common.MessageConverterUtils;
import com.eiff.framework.mq.rocketmq.common.MessageExtWrapper;
import com.eiff.framework.mq.rocketmq.listener.support.JedisHelper;

public abstract class MessagePullListenerEx extends JedisHelper implements MessagePullListener {

	private static HdLogger LOGGER = HdLogger.getLogger(MessagePullListenerEx.class);

	private final ConcurrentHashMap<String/* msgId */, AtomicInteger/* consume times */> msgConsumeTimesMap = new ConcurrentHashMap<String, AtomicInteger>(
			256);

	private int retryTimes = MQConstants.CONSUME_RETRY_TIMES;

	public ConsumePullStatus consumeMessage(final MessageQueue messageQueue, final List<MessageExt> msgs) {
		final String topic = messageQueue.getTopic();
		final String broker = messageQueue.getBrokerName();
		final int queueId = messageQueue.getQueueId();
		Tracer buildTracer = LOGGER.buildTracer();
		if (CollectionUtils.isNotEmpty(msgs)) {
			String messageId = msgs.get(0).getProperty(TRACE_CHILD + 1);
			String parentId = msgs.get(0).getProperty(TRACE_PARENT + 1);
			String rootId = msgs.get(0).getProperty(TRACE_ROOT);
			if (StringUtils.isNotBlank(messageId) && StringUtils.isNotBlank(parentId) && StringUtils.isNotBlank(rootId)) {
				buildTracer.buildContext(messageId, parentId, rootId);
			}
		}

		Span span = buildTracer.createSpan(TRANS_TYPE_MQ_PULL_CONSUMER, topic + ":" + broker + ":" + queueId);
		LOGGER.logTraceInfoTMDC();
		LOGGER.info(LOG_MQ_CONSUMER_IN_MSG, topic, broker, queueId);

		final List<MessageExtWrapper> messageExtWrappers = new LinkedList<MessageExtWrapper>();
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
			ConsumePullStatus consumePullStatus = ConsumePullStatus.CONSUME_SUCCESS;
			if (messageExtWrappers.size() > 0) {
				try {
					for (MessageExt messageExt : messageExtWrappers) {
						span.addEvent(TRANS_TYPE_MQ_CONSUMER_FROM, messageExt.getBornHostString());
					}
				} catch (Throwable e) {
				}
				consumePullStatus = consume(messageExtWrappers);
			}

			span.addEvent(TRANS_TYPE_MQ_PULL_CONSUMER, consumePullStatus.name());
			span.success();
			return consumePullStatus;
		} catch (Throwable e) {
			LOGGER.error(LOG_MQ_CONSUMER_EX_MSG, topic, broker, queueId, e);

			cleanRepeatMessageFlag(messageExtWrappers);

			final ConsumePullStatus consumePullStatus = getConsumeStatusExeHappen(messageExtWrappers);
			span.addEvent(TRANS_TYPE_MQ_PULL_CONSUMER, consumePullStatus.name());
			return consumePullStatus;
		} finally {
			LOGGER.info(LOG_MQ_CONSUMER_OUT_MSG, topic, broker, queueId);
			span.close();
			LOGGER.cleanTraceInfoInMDC();
		}
	}

	/**
	 * 消息消费出现异常时，最多可以重试消费3次，超过3次返回CONSUME_FAILED，就结束重复消费
	 * 
	 * @param messageExtWrappers
	 * @return
	 */
	private ConsumePullStatus getConsumeStatusExeHappen(final List<MessageExtWrapper> messageExtWrappers) {
		ConsumePullStatus consumePullStatus = ConsumePullStatus.CONSUME_EXCEPTION;
		if (messageExtWrappers.size() > 0) {
			for (final MessageExtWrapper messageExtWrapper : messageExtWrappers) {
				final AtomicInteger counter = msgConsumeTimesMap.putIfAbsent(messageExtWrapper.getMsgId(),
						new AtomicInteger(1));
				if (counter != null) {
					if (counter.incrementAndGet() > retryTimes) {
						consumePullStatus = ConsumePullStatus.CONSUME_FAILED;
						msgConsumeTimesMap.remove(messageExtWrapper.getMsgId());
					}
				}
			}
		}
		return consumePullStatus;
	}

	/**
	 * 清除重复消息的标志，消费出现异常时，使消息可以重复消费而不被过滤掉
	 * 
	 * @param messageExtWrappers
	 */
	private void cleanRepeatMessageFlag(final List<MessageExtWrapper> messageExtWrappers) {
		if (messageExtWrappers.size() > 0) {
			for (final MessageExtWrapper messageExtWrapper : messageExtWrappers) {
				cleanRepeatMessageFlag(messageExtWrapper);
			}
		}
	}

	public abstract ConsumePullStatus consume(final List<MessageExtWrapper> msgs);

	public void setRetryTimes(int retryTimes) {
		this.retryTimes = retryTimes;
	}

}
