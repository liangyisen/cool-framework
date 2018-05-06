package com.eiff.framework.mq.rocketmq.exception;

public class MessageListenerConcurrentlyException extends RuntimeException {

	private static final long serialVersionUID = 7804271651659850182L;

	public MessageListenerConcurrentlyException(String msg) {
		super(msg);
	}

	public MessageListenerConcurrentlyException() {
		super();
	}

	public MessageListenerConcurrentlyException(Throwable e) {
		super(e);
	}
}
