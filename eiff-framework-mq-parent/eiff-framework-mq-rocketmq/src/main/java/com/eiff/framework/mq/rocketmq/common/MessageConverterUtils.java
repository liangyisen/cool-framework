package com.eiff.framework.mq.rocketmq.common;

import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

public class MessageConverterUtils {

	private static Logger LOGGER = LoggerFactory.getLogger(MessageConverterUtils.class);

	public static MessageExtWrapper conver2MessageExtWrapper(MessageExt messageExt) {
		Object messageObject = null;
		String serializeClassName = messageExt.getUserProperty(MQConstants.MESSAGE_SERIALIZE_CLASS);
		if (StringUtils.isNotBlank(serializeClassName)) {
			String bodyContentStr = null;
			try {
				bodyContentStr = new String(messageExt.getBody(), MQConstants.DEFAULT_CHARSET);
			} catch (Exception e) {
				LOGGER.error("failed to convert text-based Message content:{}", messageExt, e);
			}

			if (String.class.getName().equals(serializeClassName)) {
				messageObject = bodyContentStr;
			} else {
				if (StringUtils.isNotBlank(bodyContentStr)) {
					try {
						messageObject = JSON.parseObject(bodyContentStr, Class.forName(serializeClassName));
					} catch (Exception e) {
						LOGGER.info("failed to convert json-based Message content:{}" + messageExt.toString() + ","
								+ e.getMessage());
						messageObject = JSON.parseObject(bodyContentStr);
					}
				}
			}
		} else {
			// try to deserializable bytes (old message)
			try {
				messageObject = new String(messageExt.getBody(), MQConstants.DEFAULT_CHARSET);
			} catch (Throwable e) {
				LOGGER.info("failed to deserializable Message content:{}", messageExt, e);
			}
		}

		MessageExtWrapper messageExtWrapper = new MessageExtWrapper(messageExt);
		messageExtWrapper.setMessageObject(messageObject);

		final String group = System.getenv(MQConstants.GROUP);
		if (StringUtils.isNotBlank(group)) {
			if (messageExt.getTopic().endsWith(MQConstants.UNDER_LINE + group)) {
				final String realTopic = messageExt.getTopic().substring(0,
						messageExt.getTopic().indexOf(MQConstants.UNDER_LINE + group));
				messageExtWrapper.setTopic(realTopic);
			}
		}

		return messageExtWrapper;
	}

	/**
	 * 根据message的body获取到对应的原始messageObject
	 * 
	 * @param mesage
	 * @return
	 */
	public static Object getMessageObject(Message mesage) {
		Object messageObject = null;
		String serializeClassName = mesage.getUserProperty(MQConstants.MESSAGE_SERIALIZE_CLASS);
		if (StringUtils.isNotBlank(serializeClassName)) {
			String bodyContentStr = null;
			try {
				bodyContentStr = new String(mesage.getBody(), MQConstants.DEFAULT_CHARSET);
			} catch (Exception e) {
				LOGGER.error("failed to convert text-based Message content:{}", mesage, e);
			}

			if (String.class.getName().equals(serializeClassName)) {
				messageObject = bodyContentStr;
			} else {
				if (StringUtils.isNotBlank(bodyContentStr)) {
					try {
						messageObject = JSON.parseObject(bodyContentStr, Class.forName(serializeClassName));
					} catch (Exception e) {
						LOGGER.info("failed to convert json-based Message content:{}" + mesage.toString() + ","
								+ e.getMessage());
						messageObject = JSON.parseObject(bodyContentStr);
					}
				}
			}
		} else {
			// try to deserializable bytes (old message)
			try {
				messageObject = new String(mesage.getBody(), MQConstants.DEFAULT_CHARSET);
			} catch (Throwable e) {
				LOGGER.info("failed to deserializable Message content:{}", mesage, e);
			}
		}

		return messageObject;
	}
}
