
package com.eiff.framework.fs.fastdfs;

import java.io.IOException;
import java.lang.reflect.Array;

public class ProtoStructDecoder<T extends StructBase> {
	/**
	 * Constructor
	 */
	public ProtoStructDecoder() {
	}

	/**
	 * decode byte buffer
	 */
	@SuppressWarnings("unchecked")
	public T[] decode(byte[] bs, Class<T> clazz, int fieldsTotalSize, String charset) throws Exception {
		if (bs.length % fieldsTotalSize != 0) {
			throw new IOException("byte array length: " + bs.length + " is invalid!");
		}

		int count = bs.length / fieldsTotalSize;
		int offset;
		T[] results = (T[]) Array.newInstance(clazz, count);

		offset = 0;
		for (int i = 0; i < results.length; i++) {
			results[i] = clazz.newInstance();
			results[i].setFields(bs, offset, charset);
			offset += fieldsTotalSize;
		}

		return results;
	}
}
