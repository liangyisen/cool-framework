package com.eiff.framework.log.api;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Callable;

import com.eiff.framework.log.api.trace.Span;
import com.eiff.framework.log.api.trace.TraceHelper;
import com.eiff.framework.log.api.trace.TraceLinker;
import com.eiff.framework.log.api.trace.Tracer;

class EmptyTracer implements Tracer {

	@Override
	public Span createSpan(String type, String name) {
		return new EmptySpan();
	}

	@Override
	public Span createEmpty() {
		return new EmptySpan();
	}

	@Override
	public Map<String, String> getContext() {
		return Collections.emptyMap();
	}

	@Override
	public Map<String, String> getContext4Async() {
		return Collections.emptyMap();
	}

	@Override
	public void buildContext(Map<String, String> context, boolean reJoin) {
	}

	@Override
	public <V> Callable<V> wrap(Callable<V> callable) {
		return callable;
	}

	@Override
	public Runnable wrap(Runnable runnable) {
		return runnable;
	}

	@Override
	public String getTraceId() {
		return Constants.EMPTY_TRACE_ID;
	}

	@Override
	public TraceHelper create(TraceLinker linker) {
		return new TraceHelper() {
			@Override
			public void send() {
			}

			@Override
			public void receive() {
			}
		};
	}

	@Override
	public String getDomainName() {
		return "NA";
	}

	public void buildContext(String messageId, String parentId, String rootId) {
	}
}
