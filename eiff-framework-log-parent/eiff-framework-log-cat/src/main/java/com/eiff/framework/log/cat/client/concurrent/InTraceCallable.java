package com.eiff.framework.log.cat.client.concurrent;

import java.util.concurrent.Callable;

import com.eiff.framework.log.cat.client.hug.TransactionHug;
import com.eiff.framework.log.cat.client.hug.TransactionHugAction;

public abstract class InTraceCallable<V> implements Callable<V> {
	private TransactionHug<V> hug;

	public InTraceCallable() {
		this.hug = new TransactionHug<V>("concurrent.call", this.getClass().getSimpleName());
	}

	public abstract V traceAbleCall() throws Exception;

	public V call() throws Exception {
		try {
			return this.hug.action(new TransactionHugAction<V>() {
				@Override
				public V work() throws Throwable {
					return traceAbleCall();
				}
			});
		} catch (Throwable e) {
			if (e instanceof Exception) {
				throw (Exception) e;
			} else {
				throw new Exception(e);
			}
		}
	};
}
