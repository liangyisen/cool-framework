package com.eiff.framework.log.api;

import com.eiff.framework.log.api.trace.Span;

class EmptySpan implements Span {

	@Override
	public void addEvent(String type, String name) {
	}

	@Override
	public void addEvent(String type, String name, String status) {
	}

	@Override
	public void addData(String key, String value) {
	}

	@Override
	public void addMetric(String name, int count) {
	}

	@Override
	public void success() {
	}

	@Override
	public void failed(String reason) {
	}
	@Override
	public void failed(Throwable throwable) {
	}
	@Override
	public void close() {
	}

	@Override
	public long getDurationInMillis() {
		return 0;
	}
}
