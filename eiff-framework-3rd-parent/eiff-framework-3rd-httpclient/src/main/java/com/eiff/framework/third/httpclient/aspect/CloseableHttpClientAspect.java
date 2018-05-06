package com.eiff.framework.third.httpclient.aspect;

import org.apache.http.HttpRequest;
import org.apache.http.RequestLine;
import org.apache.http.client.HttpClient;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import com.eiff.framework.log.api.HdLogger;
import com.eiff.framework.log.api.trace.Span;
import com.eiff.framework.log.api.trace.Tracer;

@Aspect
public class CloseableHttpClientAspect {
	HdLogger LOGGER = HdLogger.getLogger(HttpClient.class);
	@Around("methodsToBeProfiled()")
	public Object invokeExecute(ProceedingJoinPoint pjp) throws Throwable {
		Tracer buildTracer = LOGGER.buildTracer();
		String methodName = pjp.getSignature().getName();
		Object[] args = pjp.getArgs();
		String requestUri = "";
		if(args != null){
			for (Object arg : args) {
				if(arg instanceof HttpRequest){
					 HttpRequest requewt = (HttpRequest)arg;
					 RequestLine requestLine = requewt.getRequestLine();
					 if(requestLine != null){
						 requestUri = requestLine.getUri();
					 }
				}
			}
		}
		
		Span span = buildTracer.createSpan("CloseableHttpClient", methodName);
		try {
			span.addData("curi", requestUri);
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

	@Pointcut("execution(public * org.apache.http.impl.client.CloseableHttpClient.execute(..))")
	public void methodsToBeProfiled() {
	}
}
