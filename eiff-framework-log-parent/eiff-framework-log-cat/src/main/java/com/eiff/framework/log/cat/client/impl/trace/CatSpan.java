package com.eiff.framework.log.cat.client.impl.trace;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Transaction;
import com.eiff.framework.log.api.HdLogger;
import com.eiff.framework.log.api.trace.Span;
import com.eiff.framework.log.api.trace.Tracer;

public class CatSpan implements Span {
	HdLogger logger = HdLogger.getLogger(Tracer.class);

	private Transaction catTransaction;

	protected CatSpan(String type, String name) {
		try {
			catTransaction = Cat.newTransaction(type, name);
		} catch (Throwable throwable) {
			logger.error("", throwable);
		}
	}
	protected CatSpan() {
	}
	@Override
	public void addEvent(String type, String name) {
		try {
			Cat.logEvent(type, name, Event.SUCCESS, "");
		} catch (Throwable throwable) {
			logger.error("", throwable);
		}
	}

	@Override
	public void addEvent(String type, String name, String status) {
		try {
			Cat.logEvent(type, name, status, "");
		} catch (Throwable throwable) {
			logger.error("", throwable);
		}
	}

	@Override
	public void addData(String key, String value) {
		if (catTransaction != null) {
			catTransaction.addData(key, value);
		}
	}

	@Override
	public void addMetric(String name, int count) {
		try {
			Cat.logMetricForCount(name, count);
		} catch (Throwable throwable) {
			logger.error("", throwable);
		}
	}

	@Override
	public void success() {
		if (catTransaction == null) {
			return;
		}
		try {
			catTransaction.setStatus(Transaction.SUCCESS);
		} catch (Throwable throwable) {
			logger.error("", throwable);
		}
	}

	@Override
	public void close() {
		if (catTransaction == null) {
			return;
		}
		try {
			catTransaction.complete();
		} catch (Throwable throwable) {
			logger.error("", throwable);
		}
	}

	@Override
	public void failed(String reason) {
		if (catTransaction == null) {
			return;
		}
		try {
			catTransaction.setStatus(reason);
		} catch (Throwable throwable) {
			logger.error("", throwable);
		}
	}
	
	
	@Override
	public void failed(Throwable throwable) {
		if (catTransaction == null) {
			return;
		}
		try {
			Cat.logError(throwable);
		} catch (Throwable t) {
			logger.error("", t);
		}
	}
	
	@Override
	public long getDurationInMillis() {
		if (catTransaction == null) {
			return 0;
		}
		return catTransaction.getDurationInMillis();
	}
}
