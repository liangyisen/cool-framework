package com.eiff.framework.data.common;

import java.util.Stack;

import org.springframework.util.CollectionUtils;

public class ShardingUtil {

	private static final ThreadLocal<Stack<ShardingHolder>> REPOSITORY_HOLDER_STACK = new ThreadLocal<Stack<ShardingHolder>>();

	public static void removeCurrent() {
		Stack<ShardingHolder> stack = REPOSITORY_HOLDER_STACK.get();
		if (CollectionUtils.isEmpty(stack)) {
			REPOSITORY_HOLDER_STACK.remove();
			return;
		}
		stack.pop();
	}

	public static void setReadWriteKey(String key) {
		Stack<ShardingHolder> stack = REPOSITORY_HOLDER_STACK.get();
		if (stack == null) {
			stack = new Stack<ShardingHolder>();
			REPOSITORY_HOLDER_STACK.set(stack);
		}
		ShardingHolder holder = new ShardingHolder();
		holder.setReadWriteKey(key);
		stack.push(holder);
	}

	public static String getReadWriteKey() {
		Stack<ShardingHolder> stack = REPOSITORY_HOLDER_STACK.get();
		if (CollectionUtils.isEmpty(stack)) {
			return null;
		}
		return stack.peek().getReadWriteKey();
	}
}
