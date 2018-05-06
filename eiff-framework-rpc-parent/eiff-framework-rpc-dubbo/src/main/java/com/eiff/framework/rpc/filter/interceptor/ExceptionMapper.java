package com.eiff.framework.rpc.filter.interceptor;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import com.alibaba.dubbo.rpc.RpcException;
import com.eiff.framework.common.biz.exception.BaseBusinessException;
import com.eiff.framework.common.biz.exception.BaseFrameworkCheckedException;
import com.eiff.framework.common.biz.exception.BaseFrameworkRuntimeException;

public class ExceptionMapper implements MethodInterceptor {

	public Object invoke(MethodInvocation invocation) throws Throwable {

		try {
			return invocation.proceed();
		} catch (BaseBusinessException be) {
			throw new RpcException(be);
		} catch (BaseFrameworkRuntimeException runEx) {
			throw new RpcException(runEx);
		} catch (BaseFrameworkCheckedException checkEx) {
			throw new RpcException(checkEx);
		}
	}

}
