package com.eiff.framework.mq.rocketmq.exception;

public class MQPullConsumerException extends RuntimeException {

	private static final long serialVersionUID = 7804271651659850182L;

	public MQPullConsumerException(String msg) {
		super(msg);
	}

	public MQPullConsumerException() {
		super("pull failed");
	}
}
