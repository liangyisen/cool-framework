package com.eiff.framework.idgen.zk.exception;

public class CannotIncreaseException extends RuntimeException {

	private static final long serialVersionUID = 7804271651659850182L;

	public CannotIncreaseException(String msg) {
		super(msg);
	}

	public CannotIncreaseException() {
		super();
	}

	public CannotIncreaseException(Throwable e) {
		super(e);
	}
}
