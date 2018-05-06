package com.eiff.framework.concurrent.redis.common;

import org.apache.commons.lang3.ArrayUtils;

/**
 * @author tangzhaowei
 */
public class AssertUtils {
	public static void lessThan1(int arg) {
		if (arg < 1) {
			throw new IllegalArgumentException("[Assertion failed] - this argument must be greater than 1 or equal to 1");
		}
	}

	public static void lessThan0(int arg) {
		if (arg < 0) {
			throw new IllegalArgumentException("[Assertion failed] - this argument must be greater than 0 or equal to 0");
		}
	}

	public static void notEmpty(byte[] array, String message) {
		if (ArrayUtils.isEmpty(array)) {
			throw new IllegalArgumentException(message);
		}
	}

	public static void notEmpty(byte[] array) {
		notEmpty(array, "[Assertion failed] - this array must not be empty: it must contain at least 1 element");
	}
}
