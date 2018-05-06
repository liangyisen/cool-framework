package com.eiff.framework.springmvc.log.common;

import java.lang.reflect.Field;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.AdvisedSupport;
import org.springframework.aop.framework.AopProxy;

import com.eiff.framework.log.api.Constants;

public class Utils implements Constants {

	private static Logger LOGGER = LoggerFactory.getLogger(Utils.class);

	public static String getJdkDynamicProxyTargetClassName(Object proxy) throws Exception {
		try {
			Field h = proxy.getClass().getSuperclass().getDeclaredField("h");
			h.setAccessible(true);
			AopProxy aopProxy = (AopProxy) h.get(proxy);

			Field advised = aopProxy.getClass().getDeclaredField("advised");
			advised.setAccessible(true);
			AdvisedSupport advisedSupport = (AdvisedSupport) advised.get(aopProxy);

			Class<?>[] classes = advisedSupport.getProxiedInterfaces();
			if (ArrayUtils.isNotEmpty(classes)) {
				return classes[0].getName();
			}
		} catch (Exception e) {
			LOGGER.error(LOG_FAILED_TO_GET_CLASS_NAME, e);
		}

		return null;
	}
}
