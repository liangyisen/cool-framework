package com.eiff.framework.mq.rocketmq.callback;

import java.util.List;

import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.message.MessageQueue;

public interface MessagePullListener {

	public enum ConsumePullStatus {
		/**
		 * Success consumption
		 */
		CONSUME_SUCCESS,
		/**
		 * Failure consumption, NOT try to consume
		 */
		CONSUME_FAILED,

		/**
		 * EXECEPTION consumption, later try to consume
		 */
		CONSUME_EXCEPTION;
	}

	/**
	 * 返回三种消费状态，CONSUME_SUCCESS和CONSUME_FAILED不会再重复消费，CONSUME_EXCEPTION会重复消费
	 * 
	 * @param messageQueue
	 * @param msgs
	 * @return
	 */
	public ConsumePullStatus consumeMessage(final MessageQueue messageQueue, final List<MessageExt> msgs);

}
