package com.eiff.framework.log.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eiff.framework.log.api.trace.Tracer;

public abstract class HdLogger implements Logger, Constants {

	private static HdLogger LOGGERBUILDER;

	static {
		LOGGERBUILDER = initLogger();
	}

	public static HdLogger getLogger(Class<?> clazz) {
		return getLogger(clazz.getName());
	}

	public static HdLogger getLogger(String loggerName) {
		return LOGGERBUILDER.createNewLogger(loggerName);
	}

	protected abstract HdLogger createNewLogger(String loggerName);

	public abstract void logTraceInfoTMDC();

	public abstract void cleanTraceInfoInMDC();

	public abstract void extInfo(String message, EventPair value);

	public abstract Tracer buildTracer();
	public abstract void launchOnlineChange();

	private static HdLogger initLogger() {
		String DEFAULT_HD_LOGGER_BINDER = "com.eiff.framework.log.cat.client.HdLoggerImpl";
		try {
			return (HdLogger) Class.forName(DEFAULT_HD_LOGGER_BINDER).newInstance();
		} catch (Exception e) {
			EmptyHdLoggerImpl emptyHdLoggerImpl = new EmptyHdLoggerImpl();
			Logger logger = LoggerFactory.getLogger(HdLogger.class);
			logger.error("no advance logger jar");
			return emptyHdLoggerImpl;
		}
	}

}
