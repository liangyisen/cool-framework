package com.eiff.framework.rpc.trace;

import com.alibaba.dubbo.rpc.Invocation;
import com.eiff.framework.log.api.trace.TraceLinker;

public class DubboTraceLinker implements TraceLinker {

	private Invocation invocation;

	public DubboTraceLinker(Invocation invocation) {
		this.invocation = invocation;
	}

	@Override
	public void put(String key, String value) {
		this.invocation.getAttachments().put(key, value);
	}

	@Override
	public String get(String key) {
		return this.invocation.getAttachments().get(key);
	}

}
