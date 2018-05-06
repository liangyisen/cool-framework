package com.eiff.framework.log.api.trace;

import java.util.Map;
import java.util.concurrent.Callable;

public interface Tracer {
	public Span createSpan(String type, String name);
	public Span createEmpty();
	
	public Map<String, String> getContext();

	public Map<String, String> getContext4Async();

	public void buildContext(Map<String, String> context, boolean reJoin);
	public void buildContext(String messageId, String parentId, String rootId) ;
	public String getTraceId();
	
	public TraceHelper create(TraceLinker linker);
	
	public String getDomainName();
	
	<V> Callable<V> wrap(Callable<V> callable);

	Runnable wrap(Runnable runnable);
}