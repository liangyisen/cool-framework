package com.eiff.framework.third.httpclient.aspect;

import org.apache.http.client.HttpClient;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import com.eiff.framework.log.api.HdLogger;
import com.eiff.framework.log.api.trace.Span;
import com.eiff.framework.log.api.trace.Tracer;

@Aspect
public class DefaultHttpRequestRetryHandlerAspect {
	HdLogger LOGGER = HdLogger.getLogger(HttpClient.class);
	@Around("methodsToBeProfiled()")
	public Object invokeExecute(ProceedingJoinPoint pjp) throws Throwable {
		Tracer buildTracer = LOGGER.buildTracer();
		String methodName = pjp.getSignature().getName();
		Span span = buildTracer.createSpan("DefaultHttpRequestRetryHandler", methodName);
		try {
			Object returnValue = pjp.proceed();
			span.success();
			return returnValue;
		} catch (Throwable e) {
			span.failed(e.getClass().getName());
			throw e;
		} finally {
			span.close();
		}
	}

	@Pointcut("execution(public * org.apache.http.impl.client.DefaultHttpRequestRetryHandler.retryRequest(java.io.IOException,int,org.apache.http.protocol.HttpContext))")
	public void methodsToBeProfiled() {
	}
}
