package com.eiff.framework.concurrent.redis.exception;

/**
 * @author tangzhaowei
 */
public class LockAcquireException extends BaseFrameworkRuntimeException {

	private static final long serialVersionUID = 7643585190397628968L;

	public LockAcquireException(String msg, String code, Exception ex, String jsonContent) {
		super(msg, code, ex, jsonContent);
	}

	public LockAcquireException(String msg, String code, String jsonContent) {
		super(msg, code, jsonContent);
	}

	public LockAcquireException(String msg, String code) {
		super(msg, code);
	}

}
