package com.eiff.framework.mq.rocketmq.exception;

public class MQMessageConversionException extends RuntimeException {

	private static final long serialVersionUID = 2425375119454827990L;

	public MQMessageConversionException(String msg) {
		super(msg);
	}

	public MQMessageConversionException(String msg, Throwable e) {
		super(msg, e);
	}

	public MQMessageConversionException() {
		super();
	}

	public MQMessageConversionException(Throwable e) {
		super(e);
	}
}
