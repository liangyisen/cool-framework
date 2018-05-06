package com.eiff.framework.log.api.trace;

public interface TraceLinker {
	public void put(String key, String value);
	public String get(String key);
}