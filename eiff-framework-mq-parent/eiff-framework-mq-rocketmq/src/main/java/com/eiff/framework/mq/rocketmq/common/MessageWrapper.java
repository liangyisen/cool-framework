package com.eiff.framework.mq.rocketmq.common;

import java.io.UnsupportedEncodingException;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.rocketmq.common.message.Message;

import com.alibaba.fastjson.JSON;
import com.eiff.framework.mq.rocketmq.exception.MQMessageConversionException;

public class MessageWrapper extends Message {

	private static final long serialVersionUID = 1537858456103437515L;

	public MessageWrapper(String topic, Object messageObject) {
		super(topic, null);
		this.setBody(serialize(messageObject));
	}

	public MessageWrapper(String topic, String tags, Object messageObject) {
		super(topic, tags, null);
		this.setBody(serialize(messageObject));
	}

	public MessageWrapper(String topic, String tags, String keys, Object messageObject) {
		super(topic, tags, keys, null);
		this.setBody(serialize(messageObject));
	}

	public MessageWrapper(String topic, String tags, String keys, int flag, Object messageObject, boolean waitStoreMsgOK) {
		super(topic, tags, keys, flag, null, waitStoreMsgOK);
		this.setBody(serialize(messageObject));
	}

	private byte[] serialize(Object messageObject) {
		if (null == messageObject) {
			throw new MQMessageConversionException("Your object can not be null.");
		}

		byte[] bytes = null;
		if (messageObject instanceof byte[]) {
			bytes = (byte[]) messageObject;
		} else if (messageObject instanceof String) {
			try {
				bytes = ((String) messageObject).getBytes(MQConstants.DEFAULT_CHARSET);
				this.putUserProperty(MQConstants.MESSAGE_SERIALIZE_CLASS, String.class.getName());
			} catch (UnsupportedEncodingException e) {
				throw new MQMessageConversionException("failed to convert to Message content", e);
			}
		} else {
			try {
				bytes = JSON.toJSONString(messageObject).getBytes(MQConstants.DEFAULT_CHARSET);
				this.putUserProperty(MQConstants.MESSAGE_SERIALIZE_CLASS, messageObject.getClass().getName());
			} catch (Exception e) {
				throw new MQMessageConversionException("failed to convert to serialized Message content", e);
			}
		}

		if (null == bytes) {
			throw new MQMessageConversionException("Your body bytes can not be null.");
		}

		return bytes;
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

}
