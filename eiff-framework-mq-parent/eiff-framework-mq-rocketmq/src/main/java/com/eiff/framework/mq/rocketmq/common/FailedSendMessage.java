package com.eiff.framework.mq.rocketmq.common;

import java.io.Serializable;
import java.util.Calendar;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.rocketmq.common.message.Message;

public class FailedSendMessage implements Serializable {

	private static final long serialVersionUID = 7313053656091677085L;

	private Message msg;

	private Object messageObject;

	private String errStackTrace;

	private String errMsg;

	private Calendar happenTime;

	public FailedSendMessage(Message msg, Object messageObject, String errStackTrace, String errMsg) {
		super();
		this.msg = msg;
		this.messageObject = messageObject;
		this.errStackTrace = errStackTrace;
		this.errMsg = errMsg;
		this.happenTime = Calendar.getInstance();
	}

	public Message getMsg() {
		return msg;
	}

	public void setMsg(Message msg) {
		this.msg = msg;
	}

	public String getErrStackTrace() {
		return errStackTrace;
	}

	public void setErrStackTrace(String errStackTrace) {
		this.errStackTrace = errStackTrace;
	}

	public String getErrMsg() {
		return errMsg;
	}

	public void setErrMsg(String errMsg) {
		this.errMsg = errMsg;
	}

	public Calendar getHappenTime() {
		return happenTime;
	}

	public void setHappenTime(Calendar happenTime) {
		this.happenTime = happenTime;
	}

	public Object getMessageObject() {
		return messageObject;
	}

	public void setMessageObject(Object messageObject) {
		this.messageObject = messageObject;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

}
