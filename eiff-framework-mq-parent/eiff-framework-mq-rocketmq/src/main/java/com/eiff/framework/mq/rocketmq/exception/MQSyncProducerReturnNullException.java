package com.eiff.framework.mq.rocketmq.exception;

public class MQSyncProducerReturnNullException extends RuntimeException {

	private static final long serialVersionUID = 7804271651659850182L;

	public MQSyncProducerReturnNullException(String msg) {
		super(msg);
	}

	public MQSyncProducerReturnNullException() {
		super("return null");
	}
}
