package com.eiff.framework.mq.rocketmq.exception;

public class MQConsumerReturnNullException extends RuntimeException {

	private static final long serialVersionUID = 7804271651659850182L;

	public MQConsumerReturnNullException(String msg) {
		super(msg);
	}

	public MQConsumerReturnNullException() {
		super("return null");
	}
}
