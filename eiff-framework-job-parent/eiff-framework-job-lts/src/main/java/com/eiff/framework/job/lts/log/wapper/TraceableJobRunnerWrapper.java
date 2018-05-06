package com.eiff.framework.job.lts.log.wapper;

import com.eiff.framework.job.lts.log.InTraceJobRunner;
import com.github.ltsopensource.tasktracker.Result;
import com.github.ltsopensource.tasktracker.runner.JobContext;
import com.github.ltsopensource.tasktracker.runner.JobRunner;

public class TraceableJobRunnerWrapper {

	public static InTraceJobRunner wrap(final JobRunner jobRunner){
		return new InTraceJobRunner() {
			@Override
			public Result traceAbleRun(JobContext jobContext) throws Throwable {
				return jobRunner.run(jobContext);
			}
		};
	}
}
