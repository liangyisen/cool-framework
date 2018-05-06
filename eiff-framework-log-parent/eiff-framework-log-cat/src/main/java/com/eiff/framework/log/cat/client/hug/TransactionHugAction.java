package com.eiff.framework.log.cat.client.hug;

public abstract class TransactionHugAction<V> {
	public abstract V work() throws Throwable;
}
