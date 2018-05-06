package com.eiff.framework.log.cat.client.trace;

import com.dianping.cat.Cat;
import com.eiff.framework.log.api.HdLogger;
import com.eiff.framework.log.api.trace.TraceHelper;
import com.eiff.framework.log.api.trace.TraceLinker;
import com.eiff.framework.log.api.trace.Tracer;

public class CatTraceHelper implements TraceHelper {
	HdLogger logger = HdLogger.getLogger(Tracer.class);
	private TraceLinker linker;

	public CatTraceHelper(TraceLinker linker) {
		this.linker = linker;
	}

	@Override
	public void send() {

		try {
			Cat.logRemoteCallClient(new Cat.Context() {
				@Override
				public void addProperty(String key, String value) {
					CatTraceHelper.this.linker.put(key, value);
				}
				@Override
				public String getProperty(String key) {
					return CatTraceHelper.this.linker.get(key);
				}
			});
		} catch (Throwable throwable) {
			logger.error("", throwable);
		}
	}

	@Override
	public void receive() {
		try {
			Cat.logRemoteCallServer(new Cat.Context() {
				@Override
				public void addProperty(String key, String value) {
					 CatTraceHelper.this.linker.put(key, value);
				}
				@Override
				public String getProperty(String key) {
					return  CatTraceHelper.this.linker.get(key);
				}
			});
		} catch (Throwable throwable) {
			throwable.printStackTrace();
		}
	}

}
