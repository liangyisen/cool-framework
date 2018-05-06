package com.eiff.framework.springmvc.log;

import java.util.HashSet;
import java.util.Set;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.aop.framework.ReflectiveMethodInvocation;
import org.springframework.aop.support.AopUtils;

import com.eiff.framework.common.biz.exception.BaseBusinessException;
import com.eiff.framework.common.biz.mobile.FrontRequest;
import com.eiff.framework.common.biz.mobile.FrontResponse;
import com.eiff.framework.log.api.Constants;
import com.eiff.framework.log.api.HdLogger;
import com.eiff.framework.log.api.trace.Span;
import com.eiff.framework.log.api.trace.Tracer;
import com.eiff.framework.springmvc.log.common.Utils;
import com.netflix.hystrix.exception.HystrixRuntimeException;

public class MvcInterceptor implements MethodInterceptor, Constants {

	final static HdLogger LOGGER = HdLogger.getLogger(MvcInterceptor.class);

	private Set<String> excludes = new HashSet<String>();

	public MvcInterceptor() {
		excludes.add("equals");
		excludes.add("getClass");
		excludes.add("hashCode");
		excludes.add("notify");
		excludes.add("notifyAll");
		excludes.add("toString");
		excludes.add("wait");
	}

	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		String methodName = invocation.getMethod().getName();
		Tracer buildTracer = LOGGER.buildTracer();
		// TODO Anders 支持通配符
		if (excludes.contains(methodName)) {
			return invocation.proceed();
		}

		Span span = buildTracer.createEmpty();
		Object object = null;
		String fullMethodName = null;
		try {
			if (invocation instanceof ReflectiveMethodInvocation) {
				ReflectiveMethodInvocation reflectiveMethodInvocation = (ReflectiveMethodInvocation) invocation;
				if (AopUtils.isJdkDynamicProxy(reflectiveMethodInvocation.getProxy())) {
					fullMethodName = Utils.getJdkDynamicProxyTargetClassName(reflectiveMethodInvocation.getProxy()) + "." + methodName;
				}
			}
			if (fullMethodName == null) {
				fullMethodName = invocation.getThis().getClass().getName() + "." + methodName;
			}

			span = buildTracer.createSpan(TRANS_TYPE_MVC, fullMethodName);
			
			LOGGER.info(LOG_MVC_IN_MSG, fullMethodName);

			Object[] args = invocation.getArguments();
			if (ArrayUtils.isNotEmpty(args)) {
				StringBuilder sb = new StringBuilder();
				for (Object arg : args) {
					if (arg instanceof FrontRequest) {
						//span.addEvent(TRANS_TYPE_MVC + ".args" + i++, ((FrontRequest) arg).toString());
						sb.append(((FrontRequest) arg).toString() + ",");
					} else if (arg instanceof String || arg instanceof Integer || arg instanceof Long || arg instanceof Double || arg instanceof Float
							|| arg instanceof Character || arg instanceof Boolean) {
						//span.addEvent(TRANS_TYPE_MVC + ".args" + i++, String.valueOf(arg));
						sb.append(String.valueOf(arg) + ",");
					}
				}
				if (sb.length() > 0) {
					LOGGER.info(LOG_MVC_REQUEST_MSG, fullMethodName, StringUtils.removeEnd(sb.toString(), ","));
				}
			}
		} catch (Throwable e) {
			LOGGER.error(LOG_FAILED_TO_CREATE_TRANS, e);
		}

		try {
			object = invocation.proceed();
			if (object != null && object instanceof FrontResponse) {
				FrontResponse frontResponse = (FrontResponse) object;
				frontResponse.setTraceNo(buildTracer.getTraceId());
				MDC.put(EVENT_FRONTRESPONSE_RPCRETURN, frontResponse.getResultCode());
				LOGGER.info(LOG_MVC_RESPONSE_MSG, fullMethodName, frontResponse.toString());
			}
			span.success();
			return object;
		} catch (Throwable e) {
			boolean monitored = true;
			
			if(e instanceof BaseBusinessException){
				BaseBusinessException baseBusinessException = (BaseBusinessException)e;
				monitored = baseBusinessException.isMonitored();
				span.addEvent(EVENT_TYPE_EXCEPTION, baseBusinessException.getCode());
				if(!monitored) {
					MDC.put(EVENT_FRONTRESPONSE_RPCRETURN_SUCCESS, Boolean.TRUE.toString());
				}else{
					MDC.put(EVENT_FRONTRESPONSE_RPCRETURN_SUCCESS, Boolean.FALSE.toString());
				}
			}else{
				if(e instanceof HystrixRuntimeException && 
						HystrixRuntimeException.FailureType.COMMAND_EXCEPTION.equals(((HystrixRuntimeException)e).getFailureType())){
					monitored = false;
				}
			}
			LOGGER.error(LOG_MVC_EX_MSG, fullMethodName, e);

			if(monitored){
				span.failed(e);
			}else{
				span.success();
			}
			
			throw e;
		} finally {
			LOGGER.info(LOG_MVC_OUT_MSG, fullMethodName);
			span.close();
		}
	}

	public Set<String> getExcludes() {
		return excludes;
	}

	public void setExcludes(Set<String> excludes) {
		this.excludes.addAll(excludes);
	}

}
