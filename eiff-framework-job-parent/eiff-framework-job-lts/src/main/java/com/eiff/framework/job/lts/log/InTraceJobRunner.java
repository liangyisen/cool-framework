package com.eiff.framework.job.lts.log;

import com.eiff.framework.log.api.HdLogger;
import com.eiff.framework.log.api.trace.Span;
import com.eiff.framework.log.api.trace.Tracer;
import com.github.ltsopensource.tasktracker.Result;
import com.github.ltsopensource.tasktracker.runner.JobContext;
import com.github.ltsopensource.tasktracker.runner.JobRunner;

public abstract class InTraceJobRunner implements JobRunner {

	final static HdLogger LOGGER = HdLogger.getLogger(InTraceJobRunner.class);

	public abstract Result traceAbleRun(JobContext jobContext) throws Throwable;

	@Override
	public Result run(JobContext jobContext) throws Throwable {
		Tracer buildTracer = LOGGER.buildTracer();
		Span span = buildTracer.createSpan("jobrunner.call", this.getClass().getSimpleName());
		LOGGER.logTraceInfoTMDC();
		try {
			Result traceAbleRun = traceAbleRun(jobContext);
			span.success();
			return traceAbleRun;
		} catch (Throwable ex) {
			span.failed(ex);
			throw ex;
		} finally {
			span.close();
			LOGGER.cleanTraceInfoInMDC();
		}
	}
}
