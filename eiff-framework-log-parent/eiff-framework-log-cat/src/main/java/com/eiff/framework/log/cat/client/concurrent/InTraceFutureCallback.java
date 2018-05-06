package com.eiff.framework.log.cat.client.concurrent;

import com.eiff.framework.log.cat.client.hug.TransactionHug;
import com.eiff.framework.log.cat.client.hug.TransactionHugAction;
import com.google.common.util.concurrent.FutureCallback;

@SuppressWarnings({ "unchecked", "rawtypes" })
public abstract class InTraceFutureCallback<V> implements FutureCallback<V> {

	private TransactionHug<V> hug;

	public InTraceFutureCallback() {
		this.hug = new TransactionHug<V>();
	}

	public abstract void traceAbleOnSuccess(V result);

	public abstract void traceAbleOnFailure(Throwable t);

	@Override
	public void onSuccess(final V result) {
		this.hug.setType("concurrent.callback.success");
		this.hug.setName(this.getClass().getSimpleName());
		try {
			this.hug.action(new TransactionHugAction() {
				@Override
				public Object work() throws Throwable {
					traceAbleOnSuccess(result);
					return null;
				}
			});
		} catch (Throwable e) {
			if (e instanceof RuntimeException) {
				throw (RuntimeException) e;
			} else {
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	public void onFailure(final Throwable t) {
		try {
			this.hug.setType("concurrent.callback.failed");
			this.hug.setName(this.getClass().getSimpleName());
			this.hug.action(new TransactionHugAction() {
				@Override
				public Object work() throws Throwable {
					traceAbleOnFailure(t);
					return null;
				}
			});
		} catch (Throwable e) {
			if (e instanceof RuntimeException) {
				throw (RuntimeException) e;
			} else {
				throw new RuntimeException(e);
			}
		}
	}
}
