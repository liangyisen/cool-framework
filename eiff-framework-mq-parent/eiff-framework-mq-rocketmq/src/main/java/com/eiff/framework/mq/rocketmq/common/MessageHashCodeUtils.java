package com.eiff.framework.mq.rocketmq.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.rocketmq.common.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eiff.framework.log.api.Constants;

public class MessageHashCodeUtils {

	private static Logger LOGGER = LoggerFactory.getLogger(MessageHashCodeUtils.class);

	/**
	 * 生成message的hashcode
	 * 
	 * @param msg
	 * @return
	 */
	public static int generateMsgHashCode(final Message msg) {
		int hashcode = 0;
		try {
			Message cloneMsg = (Message) deepClone(msg);
			if (cloneMsg != null) {
				// remove old hash code
				cloneMsg.getProperties().remove(MQConstants.HASH_CODE);

				// make sure _catChildMessageId1 is thread id
				cloneMsg.putUserProperty(Constants.TRACE_CHILD + 1, Thread.currentThread().getId() + "");
				hashcode = cloneMsg.hashCode();
			}
		} catch (Throwable e) {
			LOGGER.error("generateMsgHashCode error " + e.getMessage());
		}
		return hashcode;
	}

	public static Object deepClone(Object src) {
		Object o = null;
		try {
			if (src != null) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ObjectOutputStream oos = new ObjectOutputStream(baos);
				oos.writeObject(src);
				oos.close();

				ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
				ObjectInputStream ois = new ObjectInputStream(bais);
				o = ois.readObject();
				ois.close();
			}
		} catch (Throwable e) {
			LOGGER.error("deepClone error " + e.getMessage());
		}
		return o;
	}

	public static String currentStackTrace(Exception e) {
		StringBuilder sb = new StringBuilder();
		StackTraceElement[] stackTrace = e.getStackTrace();
		for (StackTraceElement ste : stackTrace) {
			sb.append("\n\t");
			sb.append(ste.toString());
		}

		if (sb.toString().length() > 1500) {
			return sb.toString().substring(0, 1500);
		}

		return sb.toString();
	}

}
