package com.eiff.framework.log.cat.client.concurrent;	

import com.eiff.framework.log.cat.client.hug.TransactionHug;
import com.eiff.framework.log.cat.client.hug.TransactionHugAction;

public abstract class InTraceRunnable implements Runnable {
	private TransactionHug<Object> hug;

	public InTraceRunnable() {
		this.hug = new TransactionHug<Object>("runner.call", this.getClass().getSimpleName());
	}

	public InTraceRunnable(String runnerName) {
		this.hug = new TransactionHug<Object>("runner.call", runnerName);
	}

	public abstract void traceAbleRun();

	@Override
	public void run() {
		try {
			this.hug.action(new TransactionHugAction<Object>() {
				@Override
				public Object work() throws Throwable {
					traceAbleRun();
					return null;
				}
			});
		} catch (Throwable e) {
			if (e instanceof RuntimeException) {
				throw (RuntimeException) e;
			}
			throw new RuntimeException(e);
		}
	}
}
