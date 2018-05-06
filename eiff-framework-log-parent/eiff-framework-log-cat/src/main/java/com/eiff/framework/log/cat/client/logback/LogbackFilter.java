package com.eiff.framework.log.cat.client.logback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

@Deprecated
public class LogbackFilter extends Filter<ILoggingEvent> {

	@Override
	public FilterReply decide(ILoggingEvent event) {

		if (event.getFormattedMessage().contains("topic")) {
			return FilterReply.DENY;
		} else {
			return FilterReply.ACCEPT;
		}
	}

}
