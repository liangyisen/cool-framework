package com.eiff.framework.cache.redis.common;

import java.io.*;

/**
 * @author tangzhaowei
 */
public class SerializeUtils {
	public static byte[] serialize(Serializable object) throws IOException {
		ObjectOutputStream oos = null;
		ByteArrayOutputStream baos = null;
		// 序列化
		baos = new ByteArrayOutputStream();
		oos = new ObjectOutputStream(baos);
		oos.writeObject(object);
		byte[] bytes = baos.toByteArray();
		return bytes;
	}

	public static Object unserialize(byte[] bytes) throws IOException, ClassNotFoundException {
		ByteArrayInputStream bais = null;		
		// 反序列化
		bais = new ByteArrayInputStream(bytes);
		ObjectInputStream ois = new ObjectInputStream(bais);
		return ois.readObject();
	}
}
