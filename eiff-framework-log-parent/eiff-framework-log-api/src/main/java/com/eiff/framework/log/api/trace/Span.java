package com.eiff.framework.log.api.trace;

public interface Span {
	public void addEvent(String type, String name);

	public void addEvent(String type, String name, String status);

	public void addData(String key, String value);

	public void addMetric(String name, int count);

	public void success();

	public void failed(String reason);
	
	public void failed(Throwable throwable);
	
	public long getDurationInMillis();
	
	public void close();
}
