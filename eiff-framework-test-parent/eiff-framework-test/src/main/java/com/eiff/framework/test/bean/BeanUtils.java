package com.eiff.framework.test.bean;

import java.lang.reflect.Field;

import org.mockito.internal.util.MockUtil;
import org.mockito.mock.MockCreationSettings;
import org.springframework.aop.framework.AdvisedSupport;
import org.springframework.aop.framework.AopProxy;
import org.springframework.aop.support.AopUtils;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class BeanUtils {

	public static <T> T getField(Object obj, String fieldName, Class<T> fieldClass) {
		Field declaredField;
		try {
			declaredField = obj.getClass().getDeclaredField(fieldName);
			declaredField.setAccessible(true);
			Object object = declaredField.get(obj);
			return (T) object;
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Object getTarget(Object obj) {
		MockUtil mockUtil = new MockUtil();
		if (mockUtil.isSpy(obj)) {
			MockCreationSettings mockSettings = mockUtil.getMockSettings(obj);
			return getTarget(mockSettings.getSpiedInstance());
		}
		if (!AopUtils.isAopProxy(obj)) {
			return obj;
		} else {
			if (AopUtils.isJdkDynamicProxy(obj)) {
				try {
					return getJdkDynamicProxyTargetObject(obj);
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}
			} else {
				try {
					return getCglibProxyTargetObject(obj);
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}
			}
		}
	}

	public static void setField(Object target, String fieldName, Object fieldValue) {
		if (AopUtils.isAopProxy(target)) {
			target = getTarget(target);
		}
		Field declaredField;
		try {
			declaredField = target.getClass().getDeclaredField(fieldName);
			declaredField.setAccessible(true);
			declaredField.set(target, fieldValue);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static Object getCglibProxyTargetObject(Object proxy) throws Exception {
		Field h = proxy.getClass().getDeclaredField("CGLIB$CALLBACK_0");
		h.setAccessible(true);
		Object dynamicAdvisedInterceptor = h.get(proxy);

		Field advised = dynamicAdvisedInterceptor.getClass().getDeclaredField("advised");
		advised.setAccessible(true);

		Object target = ((AdvisedSupport) advised.get(dynamicAdvisedInterceptor)).getTargetSource().getTarget();

		return target;
	}

	private static Object getJdkDynamicProxyTargetObject(Object proxy) throws Exception {
		Field h = proxy.getClass().getSuperclass().getDeclaredField("h");
		h.setAccessible(true);
		AopProxy aopProxy = (AopProxy) h.get(proxy);

		Field advised = aopProxy.getClass().getDeclaredField("advised");
		advised.setAccessible(true);

		Object target = ((AdvisedSupport) advised.get(aopProxy)).getTargetSource().getTarget();

		return target;
	}
}
