package com.eiff.framework.mq.rocketmq.common;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.rocketmq.common.message.MessageConst;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.BeanUtils;

public class MessageExtWrapper extends MessageExt {

	private static final long serialVersionUID = -6116217910023007895L;

	private Object messageObject;

	public MessageExtWrapper(MessageExt messageExt) {
		super();
		BeanUtils.copyProperties(messageExt, this);

		Map<String, String> properties = messageExt.getProperties();
		for (Entry<String, String> entry : properties.entrySet()) {
			if (!MessageConst.STRING_HASH_SET.contains(entry.getKey())) {
				this.putUserProperty(entry.getKey(), entry.getValue());
			}
		}
	}

	public Object getMessageObject() {
		return messageObject;
	}

	public void setMessageObject(Object messageObject) {
		this.messageObject = messageObject;
	}

	@Override
	public String toString() {
		String innerObjectToString = null;
		if (messageObject != null) {
			if (messageObject instanceof String) {
				innerObjectToString = "'" + messageObject + "'";
			} else {
				innerObjectToString = ToStringBuilder.reflectionToString(messageObject);
			}
		}

		return "MessageExtWrapper [messageObject=" + innerObjectToString + ", toString()=" + super.toString() + "]";
	}
}
