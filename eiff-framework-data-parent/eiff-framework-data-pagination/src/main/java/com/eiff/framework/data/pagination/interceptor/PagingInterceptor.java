package com.eiff.framework.data.pagination.interceptor;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.data.domain.Pageable;

import com.eiff.framework.data.pagination.common.PagingArrayList;

public class PagingInterceptor implements MethodInterceptor {

	private final static String COUNT_METHOD_SUFFIX = "Count";

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		try {
			Object[] args = invocation.getArguments();
			if(args != null && args.length > 0 && args[0] instanceof Pageable){
				String methodName = invocation.getMethod().getName();
				long total = 0; 
				try {
					Method method = invocation.getThis().getClass().getMethod(methodName + COUNT_METHOD_SUFFIX,
							invocation.getMethod().getParameterTypes());
					total = (long)method.invoke(invocation.getThis(), args);
				} catch (Exception e) {
				}
				
				Object result = invocation.proceed();
				if (result instanceof List) {
					if(0 == total){
						return new PagingArrayList((ArrayList<?>) result, (Pageable) args[0], (long)((ArrayList<?>) result).size());
					}else{
						return new PagingArrayList((ArrayList<?>) result, (Pageable) args[0], total);
					}
				}
				return result;
			}else {
				Object result = invocation.proceed();
				return result;
			}
		} catch (Throwable e) {
			return invocation.proceed();
		}
	}
}
