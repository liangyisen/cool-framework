package com.eiff.framework.log.cat.client.aspect.aj.bean;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public class ServiceBeanAspect {

	@Around("methodsToBeProfiled()")
	public Object invokeExecute(ProceedingJoinPoint pjp) throws Throwable {
		return System.getenv("GROUP");
	}

	@Pointcut("execution(public * com.alibaba.dubbo.config.AbstractServiceConfig.getGroup())")
	public void methodsToBeProfiled() {
	}

	@Around("referenceMethodsToBeProfiled()")
	public Object invokeExecute1(ProceedingJoinPoint pjp) throws Throwable {
		return System.getenv("GROUP");
	}

	@Pointcut("execution(public * com.alibaba.dubbo.config.AbstractReferenceConfig.getGroup())")
	public void referenceMethodsToBeProfiled() {
	}
}
