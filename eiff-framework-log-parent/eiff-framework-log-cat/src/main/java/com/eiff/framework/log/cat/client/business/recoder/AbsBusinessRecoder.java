package com.eiff.framework.log.cat.client.business.recoder;

import java.util.ArrayList;
import java.util.List;

import com.dianping.cat.message.Event;
import com.eiff.framework.log.api.EventPair;
import com.eiff.framework.log.api.HdLogger;
import com.eiff.framework.log.api.biz.BusinessRecoder;
import com.eiff.framework.log.api.trace.Span;
import com.eiff.framework.log.api.trace.Tracer;

public abstract class AbsBusinessRecoder implements BusinessRecoder {

	HdLogger LOGGER = HdLogger.getLogger(AbsBusinessRecoder.class);

	protected String rootType;
	protected String rootName;
	protected List<EventPair> eventList = new ArrayList<>();

	public String getRootType() {
		return rootType;
	}

	public String getRootName() {
		return rootName;
	}

	public BusinessRecoder logSuccess(EventPair eventPair) {
		try {
			eventPair = eventPair.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		eventPair.setSuccessful(true);
		eventList.add(eventPair);
		return this;
	}

	public BusinessRecoder logFailed(EventPair eventPair) {
		try {
			eventPair = eventPair.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		eventPair.setSuccessful(false);
		eventList.add(eventPair);
		return this;
	}

	public void submit() {
		Tracer tracer = LOGGER.buildTracer();
		Span span = tracer.createSpan(this.rootType, this.rootName);
		for (EventPair eventPair : eventList) {
			String statusCode = Event.SUCCESS;
			if (!eventPair.isSuccessful()) {
				statusCode = "failed";
			}
			span.addEvent(eventPair.getType() + "." + this.rootName, eventPair.getName(), statusCode);
		}
		if (eventList.size() > 0) {
			span.addMetric(this.rootName, eventList.size());
		}
		span.success();
		span.close();
		eventList.clear();
	}
}
