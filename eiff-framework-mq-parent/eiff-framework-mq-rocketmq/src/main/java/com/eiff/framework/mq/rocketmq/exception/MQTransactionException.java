package com.eiff.framework.mq.rocketmq.exception;

public class MQTransactionException extends RuntimeException {

	private static final long serialVersionUID = 7804271651659850182L;

	public MQTransactionException(String msg) {
		super(msg);
	}

	public MQTransactionException(String msg, Throwable cause) {
		super(msg, cause);
	}

	public MQTransactionException() {
		super("transaction exception");
	}
}
