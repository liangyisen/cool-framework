package com.eiff.framework.concurrent.redis.interceptor;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import com.eiff.framework.log.api.HdLogger;
import com.eiff.framework.log.api.trace.Span;
import com.eiff.framework.log.api.trace.Tracer;

public class ConcurrentLockInterceptor implements MethodInterceptor {

	private static HdLogger LOGGER = HdLogger.getLogger(ConcurrentLockInterceptor.class);

	private static final String CONCURRENT_LOCK_TYPE = "Lock.redis";

	@Override
	public Object invoke(MethodInvocation methodInvocation) throws Throwable {
		Tracer tracer = LOGGER.buildTracer();
		String hadesMethodName = methodInvocation.getMethod().getName();

		Span span = tracer.createSpan(CONCURRENT_LOCK_TYPE, hadesMethodName);
		try {
			Object returnValue = methodInvocation.proceed();
			span.success();
			return returnValue;
		} catch (Throwable ex) {
			span.failed(ex);
			throw ex;
		} finally {
			span.close();
		}
	}
}
