package com.eiff.framework.log.cat.client.logback.layout;

import java.util.LinkedHashMap;
import java.util.Map;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.pattern.ThrowableProxyConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.contrib.json.JsonLayoutBase;

/**
 * 重写ch.qos.logback.contrib.json.classic.JsonLayout
 * 
 * @author hengda
 *
 */
public class JsonLayout extends JsonLayoutBase<ILoggingEvent> {

	public static final String TIMESTAMP_ATTR_NAME = "timestamp";
	public static final String LEVEL_ATTR_NAME = "level";
	public static final String THREAD_ATTR_NAME = "thread";
	public static final String MDC_ATTR_NAME = "mdc";
	public static final String LOGGER_ATTR_NAME = "logger";
	public static final String FORMATTED_MESSAGE_ATTR_NAME = "message";
	public static final String MESSAGE_ATTR_NAME = "raw-message";
	public static final String EXCEPTION_ATTR_NAME = "exception";
	public static final String EXCEPTION_ATTR_TYPE = "exceptiontype";
	public static final String CONTEXT_ATTR_NAME = "context";

	protected boolean includeLevel;
	protected boolean includeThreadName;
	protected boolean includeMDC;
	protected boolean includeLoggerName;
	protected boolean includeFormattedMessage;
	protected boolean includeMessage;
	protected boolean includeException;
	protected boolean includeContextName;

	private final ThrowableProxyConverter throwableProxyConverter;

	public JsonLayout() {
		super();
		this.includeLevel = true;
		this.includeThreadName = true;
		this.includeMDC = true;
		this.includeLoggerName = true;
		this.includeFormattedMessage = true;
		this.includeException = true;
		this.includeContextName = true;
		this.throwableProxyConverter = new ThrowableProxyConverter();
	}

	@Override
	public void start() {
		this.throwableProxyConverter.start();
		super.start();
	}

	@Override
	public void stop() {
		super.stop();
		this.throwableProxyConverter.stop();
	}

	@Override
	protected Map<?, ?> toJsonMap(ILoggingEvent event) {

		Map<String, Object> map = new LinkedHashMap<String, Object>();

		if (this.includeTimestamp) {
			long timestamp = event.getTimeStamp();
			String formatted = formatTimestamp(timestamp);
			if (formatted != null) {
				map.put(TIMESTAMP_ATTR_NAME, formatted);
			}
		}

		if (this.includeLevel) {
			Level level = event.getLevel();
			if (level != null) {
				String lvlString = String.valueOf(level);
				map.put(LEVEL_ATTR_NAME, lvlString);
			}
		}

		if (this.includeThreadName) {
			String threadName = event.getThreadName();
			if (threadName != null) {
				map.put(THREAD_ATTR_NAME, threadName);
			}
		}

		if (this.includeMDC) {
			Map<String, String> mdc = event.getMDCPropertyMap();
			if ((mdc != null) && !mdc.isEmpty()) {
				map.put(MDC_ATTR_NAME, mdc);
			}
		}

		if (this.includeLoggerName) {
			String loggerName = event.getLoggerName();
			if (loggerName != null) {
				map.put(LOGGER_ATTR_NAME, loggerName);
			}
		}

		if (this.includeFormattedMessage) {
			String msg = event.getFormattedMessage();
			if (msg != null) {
				map.put(FORMATTED_MESSAGE_ATTR_NAME, msg);
			}
		}

		if (this.includeMessage) {
			String msg = event.getMessage();
			if (msg != null) {
				map.put(MESSAGE_ATTR_NAME, msg);
			}
		}

		if (this.includeContextName) {
			String msg = event.getLoggerContextVO().getName();
			if (msg != null) {
				map.put(CONTEXT_ATTR_NAME, msg);
			}
		}

		if (this.includeException) {
			IThrowableProxy throwableProxy = event.getThrowableProxy();
			if (throwableProxy != null) {
				String ex = throwableProxyConverter.convert(event);
				if (ex != null && !ex.equals("")) {
					map.put(EXCEPTION_ATTR_NAME, ex);
					if (throwableProxy.getCause() != null) {
						IThrowableProxy throwableCause = throwableProxy.getCause();
						if (RuntimeException.class.getName().equals(throwableCause.getClassName())) {
							if (throwableCause.getCause() != null) {
								throwableCause = throwableCause.getCause();
							}
						}
						map.put(EXCEPTION_ATTR_TYPE, throwableCause.getClassName());
					} else {
						map.put(EXCEPTION_ATTR_TYPE, throwableProxy.getClassName());
					}
				}
			}
		}

		return map;
	}

	public boolean isIncludeLevel() {
		return includeLevel;
	}

	public void setIncludeLevel(boolean includeLevel) {
		this.includeLevel = includeLevel;
	}

	public boolean isIncludeLoggerName() {
		return includeLoggerName;
	}

	public void setIncludeLoggerName(boolean includeLoggerName) {
		this.includeLoggerName = includeLoggerName;
	}

	public boolean isIncludeFormattedMessage() {
		return includeFormattedMessage;
	}

	public void setIncludeFormattedMessage(boolean includeFormattedMessage) {
		this.includeFormattedMessage = includeFormattedMessage;
	}

	public boolean isIncludeMessage() {
		return includeMessage;
	}

	public void setIncludeMessage(boolean includeMessage) {
		this.includeMessage = includeMessage;
	}

	public boolean isIncludeMDC() {
		return includeMDC;
	}

	public void setIncludeMDC(boolean includeMDC) {
		this.includeMDC = includeMDC;
	}

	public boolean isIncludeThreadName() {
		return includeThreadName;
	}

	public void setIncludeThreadName(boolean includeThreadName) {
		this.includeThreadName = includeThreadName;
	}

	public boolean isIncludeException() {
		return includeException;
	}

	public void setIncludeException(boolean includeException) {
		this.includeException = includeException;
	}

	public boolean isIncludeContextName() {
		return includeContextName;
	}

	public void setIncludeContextName(boolean includeContextName) {
		this.includeContextName = includeContextName;
	}
}
