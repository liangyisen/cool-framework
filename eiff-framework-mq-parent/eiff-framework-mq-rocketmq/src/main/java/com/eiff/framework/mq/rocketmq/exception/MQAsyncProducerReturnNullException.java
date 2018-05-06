package com.eiff.framework.mq.rocketmq.exception;

public class MQAsyncProducerReturnNullException extends RuntimeException {

	private static final long serialVersionUID = 7804271651659850182L;

	public MQAsyncProducerReturnNullException(String msg) {
		super(msg);
	}

	public MQAsyncProducerReturnNullException() {
		super("return null");
	}
}
