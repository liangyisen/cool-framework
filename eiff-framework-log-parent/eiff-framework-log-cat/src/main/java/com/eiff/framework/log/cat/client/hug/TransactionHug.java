package com.eiff.framework.log.cat.client.hug;

import java.util.Map;

import com.eiff.framework.log.api.HdLogger;
import com.eiff.framework.log.api.trace.Span;
import com.eiff.framework.log.api.trace.Tracer;

public class TransactionHug<V> {

	private static HdLogger LOGGER = HdLogger.getLogger(TransactionHug.class);

	private String type;
	private String name;
	Map<String, String> catContext;
	Tracer tracer;

	public TransactionHug(String type, String name) {
		this.type = type;
		this.name = name;
		this.tracer = LOGGER.buildTracer();
		catContext = this.tracer.getContext();
	}

	public TransactionHug() {
		this.tracer = LOGGER.buildTracer();
		catContext = this.tracer.getContext();
	}

	public V action(TransactionHugAction<V> action) throws Throwable {
		this.tracer.buildContext(catContext, false);
		Span span = this.tracer.createSpan(this.type, this.name);
		LOGGER.logTraceInfoTMDC();
		try {
			V returnValue = action.work();
			span.success();
			return returnValue;
		} catch (Throwable ex) {
			span.failed(ex.getClass().getName());
			throw ex;
		} finally {
			span.close();
			LOGGER.cleanTraceInfoInMDC();
		}
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
