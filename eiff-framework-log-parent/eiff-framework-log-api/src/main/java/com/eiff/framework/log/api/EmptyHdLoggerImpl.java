package com.eiff.framework.log.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import com.eiff.framework.log.api.trace.Tracer;

class EmptyHdLoggerImpl extends HdLogger {

	private Logger logger;

	@Override
	protected HdLogger createNewLogger(String loggerName) {
		EmptyHdLoggerImpl newLogger = new EmptyHdLoggerImpl();
		newLogger.logger = LoggerFactory.getLogger(loggerName);
		return newLogger;
	}

	@Override
	public String getName() {
		return this.logger.getName();
	}

	@Override
	public boolean isTraceEnabled() {
		return this.logger.isTraceEnabled();
	}

	@Override
	public void trace(String msg) {
		this.logger.trace(msg);
	}

	@Override
	public void trace(String format, Object arg) {
		this.logger.trace(format, arg);
	}

	@Override
	public void trace(String format, Object arg1, Object arg2) {
		this.logger.trace(format, arg1, arg2);
	}

	@Override
	public void trace(String format, Object... arguments) {
		this.logger.trace(format, arguments);
	}

	@Override
	public void trace(String msg, Throwable t) {
		this.logger.trace(msg, t);
	}

	@Override
	public boolean isTraceEnabled(Marker marker) {
		return this.logger.isTraceEnabled();
	}

	@Override
	public void trace(Marker marker, String msg) {
		this.logger.trace(marker, msg);
	}

	@Override
	public void trace(Marker marker, String format, Object arg) {
		this.logger.trace(marker, format, arg);
	}

	@Override
	public void trace(Marker marker, String format, Object arg1, Object arg2) {
		this.logger.trace(marker, format, arg1, arg2);
	}

	@Override
	public void trace(Marker marker, String format, Object... argArray) {
		this.logger.trace(marker, format, argArray);
	}

	@Override
	public void trace(Marker marker, String msg, Throwable t) {
		this.logger.trace(marker, msg, t);
	}

	@Override
	public boolean isDebugEnabled() {
		return this.logger.isDebugEnabled();
	}

	@Override
	public void debug(String msg) {
		this.logger.debug(msg);
	}

	@Override
	public void debug(String format, Object arg) {
		this.logger.debug(format, arg);
	}

	@Override
	public void debug(String format, Object arg1, Object arg2) {
		this.logger.debug(format, arg1, arg2);
	}

	@Override
	public void debug(String format, Object... arguments) {
		this.logger.debug(format, arguments);
	}

	@Override
	public void debug(String msg, Throwable t) {
		this.logger.debug(msg, t);
	}

	@Override
	public boolean isDebugEnabled(Marker marker) {
		return this.logger.isDebugEnabled(marker);
	}

	@Override
	public void debug(Marker marker, String msg) {
		this.logger.debug(marker, msg);
	}

	@Override
	public void debug(Marker marker, String format, Object arg) {
		this.logger.debug(marker, format, arg);
	}

	@Override
	public void debug(Marker marker, String format, Object arg1, Object arg2) {
		this.logger.debug(marker, format, arg1, arg2);
	}

	@Override
	public void debug(Marker marker, String format, Object... arguments) {
		this.logger.debug(marker, format, arguments);
	}

	@Override
	public void debug(Marker marker, String msg, Throwable t) {
		this.logger.debug(marker, msg, t);
	}

	@Override
	public boolean isInfoEnabled() {
		return this.logger.isInfoEnabled();
	}

	@Override
	public void info(String msg) {
		this.logger.info(msg);
	}

	@Override
	public void info(String format, Object arg) {
		this.logger.info(format, arg);
	}

	@Override
	public void info(String format, Object arg1, Object arg2) {
		this.logger.info(format, arg1, arg2);
	}

	@Override
	public void info(String format, Object... arguments) {
		this.logger.info(format, arguments);
	}

	@Override
	public void info(String msg, Throwable t) {
		this.logger.info(msg, t);
	}

	@Override
	public boolean isInfoEnabled(Marker marker) {
		return this.logger.isInfoEnabled(marker);
	}

	@Override
	public void info(Marker marker, String msg) {
		this.logger.info(marker, msg);
	}

	@Override
	public void info(Marker marker, String format, Object arg) {
		this.logger.info(marker, format, arg);
	}

	@Override
	public void info(Marker marker, String format, Object arg1, Object arg2) {
		this.logger.info(marker, format, arg1, arg2);
	}

	@Override
	public void info(Marker marker, String format, Object... arguments) {
		this.logger.info(marker, format, arguments);
	}

	@Override
	public void info(Marker marker, String msg, Throwable t) {
		this.logger.info(marker, msg, t);
	}

	@Override
	public boolean isWarnEnabled() {
		return this.logger.isWarnEnabled();
	}

	@Override
	public void warn(String msg) {
		this.logger.warn(msg);
	}

	@Override
	public void warn(String format, Object arg) {
		this.logger.warn(format, arg);
	}

	@Override
	public void warn(String format, Object... arguments) {
		this.logger.warn(format, arguments);
	}

	@Override
	public void warn(String format, Object arg1, Object arg2) {
		this.logger.warn(format, arg1, arg2);
	}

	@Override
	public void warn(String msg, Throwable t) {
		this.logger.warn(msg, t);
	}

	@Override
	public boolean isWarnEnabled(Marker marker) {
		return this.logger.isWarnEnabled(marker);
	}

	@Override
	public void warn(Marker marker, String msg) {
		this.logger.warn(marker, msg);
	}

	@Override
	public void warn(Marker marker, String format, Object arg) {
		this.logger.warn(marker, format, arg);
	}

	@Override
	public void warn(Marker marker, String format, Object arg1, Object arg2) {
		this.logger.warn(marker, format, arg1, arg2);
	}

	@Override
	public void warn(Marker marker, String format, Object... arguments) {
		this.logger.warn(marker, format, arguments);
	}

	@Override
	public void warn(Marker marker, String msg, Throwable t) {
		this.logger.warn(marker, msg, t);
	}

	@Override
	public boolean isErrorEnabled() {
		return this.logger.isErrorEnabled();
	}

	@Override
	public void error(String msg) {
		this.logger.error(msg);
	}

	@Override
	public void error(String format, Object arg) {
		this.logger.error(format, arg);
	}

	@Override
	public void error(String format, Object arg1, Object arg2) {
		this.logger.error(format, arg1, arg2);
	}

	@Override
	public void error(String format, Object... arguments) {
		this.logger.error(format, arguments);
	}

	@Override
	public void error(String msg, Throwable t) {
		logger.error(msg, t);
	}

	@Override
	public boolean isErrorEnabled(Marker marker) {
		return logger.isErrorEnabled(marker);
	}

	@Override
	public void error(Marker marker, String msg) {
		logger.error(marker, msg);
	}

	@Override
	public void error(Marker marker, String format, Object arg) {
		logger.error(marker, format, arg);
	}

	@Override
	public void error(Marker marker, String format, Object arg1, Object arg2) {
		logger.error(marker, format, arg1, arg2);
	}

	@Override
	public void error(Marker marker, String format, Object... arguments) {
		logger.error(marker, format, arguments);
	}

	@Override
	public void error(Marker marker, String msg, Throwable t) {
		logger.error(marker, msg, t);
	}

	@Override
	public void logTraceInfoTMDC() {
	}

	@Override
	public void cleanTraceInfoInMDC() {
	}

	@Override
	public void extInfo(String message, EventPair value) {
		logger.info(message + "::" + value);
	}

	@Override
	public Tracer buildTracer() {
		return new EmptyTracer();
	}

	@Override
	public void launchOnlineChange() {
	}
}
