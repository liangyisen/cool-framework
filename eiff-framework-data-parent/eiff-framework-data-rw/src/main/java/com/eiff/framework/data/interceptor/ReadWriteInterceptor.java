package com.eiff.framework.data.interceptor;

import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.transaction.annotation.Transactional;

import com.eiff.framework.data.common.ReadWriteKey;
import com.eiff.framework.data.common.ShardingUtil;
import com.eiff.framework.log.api.Constants;

public class ReadWriteInterceptor implements MethodInterceptor, Constants {

	// private static final Logger LOGGER =
	// LoggerFactory.getLogger(ReadWriteInterceptor.class);

	private ReadWriteKey readWriteKey;

	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		// String className = invocation.getThis().getClass().getName();
		String methodName = invocation.getMethod().getName();
		// String fullName = className + "." + methodName;

		// LOGGER.info(Constants.LOG_RW_IN_MSG, fullName);

		Method method = invocation.getThis().getClass().getMethod(methodName, invocation.getMethod().getParameterTypes());

		Transactional tx = method.getAnnotation(Transactional.class);
		if (tx == null) {
			tx = invocation.getThis().getClass().getAnnotation(Transactional.class);
		}

		if (tx != null && tx.readOnly()) {
			readWriteKey.setReadKey();
			// LOGGER.info(Constants.LOG_RW_INFO_MSG, fullName, "read");
		} else {
			readWriteKey.setWriteKey();
			// LOGGER.info(Constants.LOG_RW_INFO_MSG, fullName, "write");
		}

		try {
			return invocation.proceed();
		} catch (Throwable e) {
			// LOGGER.error(LOG_RW_EX_MSG, fullName, e);
			throw e;
		} finally {
			ShardingUtil.removeCurrent();
			// LOGGER.info(LOG_RW_OUT_MSG, fullName);
		}
	}

	// getter and setter

	public ReadWriteKey getReadWriteKey() {
		return readWriteKey;
	}

	public void setReadWriteKey(ReadWriteKey readWriteKey) {
		this.readWriteKey = readWriteKey;
	}
}
