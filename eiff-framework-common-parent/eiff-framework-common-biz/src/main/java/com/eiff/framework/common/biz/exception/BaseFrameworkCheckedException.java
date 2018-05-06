package com.eiff.framework.common.biz.exception;

import com.eiff.framework.common.biz.exception.baseface.BaseFrameworkException;

/**
 * 基础中间件checked异常类
 * 
 * @author lusong
 * 
 */
public class BaseFrameworkCheckedException extends Exception implements BaseFrameworkException{

	private static final long serialVersionUID = 1L;

	private String msg;

	private String code;

	private Throwable innerException;

	private String jsonContent;

	protected boolean monitored = false;
	protected boolean loggable = true;

	public BaseFrameworkCheckedException(String msg, String code, Throwable ex,
			String jsonContent) {
		super(msg);
		this.msg = msg;
		this.code = code;
		this.innerException = ex;
		this.jsonContent = jsonContent;
	}

	public BaseFrameworkCheckedException(String msg, String code,
			String jsonContent) {
		super(msg);
		this.msg = msg;
		this.code = code;
		this.jsonContent = jsonContent;
	}

	public BaseFrameworkCheckedException(String msg, String code) {
		super(msg);
		this.msg = msg;
		this.code = code;
	}

	public String getMsg() {
		return msg;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public Throwable getInnerException() {
		return innerException;
	}

	public void setInnerException(Throwable innerException) {
		this.innerException = innerException;
	}

	public String getJsonContent() {
		return jsonContent;
	}

	public void setJsonContent(String jsonContent) {
		this.jsonContent = jsonContent;
	}

	public boolean isMonitored() {
		return monitored;
	}

	public void setMonitored(boolean monitored) {
		this.monitored = monitored;
	}

	public boolean isLoggable() {
		return loggable;
	}

	public void setLoggable(boolean loggable) {
		this.loggable = loggable;
	}

}
