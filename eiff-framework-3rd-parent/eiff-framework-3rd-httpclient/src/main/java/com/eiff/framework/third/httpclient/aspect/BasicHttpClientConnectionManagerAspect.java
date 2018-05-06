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
public class BasicHttpClientConnectionManagerAspect {
	HdLogger LOGGER = HdLogger.getLogger(HttpClient.class);
	@Around("methodsToBeProfiled()")
	public Object invokeExecute(ProceedingJoinPoint pjp) throws Throwable {
		String methodName = pjp.getSignature().getName();
		Tracer buildTracer = LOGGER.buildTracer();
		Span span = buildTracer.createSpan("BasicHttpClientConnectionManager", methodName);
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

	@Pointcut("execution(public * org.apache.http.impl.conn.BasicHttpClientConnectionManager.connect(org.apache.http.HttpClientConnection,org.apache.http.conn.routing.HttpRoute,int,org.apache.http.protocol.HttpContext))")
	public void methodsToBeProfiled() {
	}
}
