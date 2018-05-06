package com.eiff.framework.test.bean;

import org.mockito.Mockito;
import org.mockito.internal.util.MockUtil;

import com.eiff.framework.test.processer.EIfBeanFactoryPostProcessor;

public class CleanupUtils {

	public static void cleanUpAllSpyBean() {
		String[] beanNames = EIfBeanFactoryPostProcessor.SPRING_APPLICATIONCONTEXT.getBeanDefinitionNames();
		for (int i = 0; i < beanNames.length; i++) {
			Object bean = EIfBeanFactoryPostProcessor.SPRING_APPLICATIONCONTEXT.getBean(beanNames[i]);
			MockUtil mockUtil = new MockUtil();
			if (mockUtil.isSpy(bean)) {
				Mockito.reset(bean);
			}
		}
	}

	public static void cleanUpAllMockBean() {
		String[] beanNames = EIfBeanFactoryPostProcessor.SPRING_APPLICATIONCONTEXT.getBeanDefinitionNames();
		for (int i = 0; i < beanNames.length; i++) {
			Object bean = EIfBeanFactoryPostProcessor.SPRING_APPLICATIONCONTEXT.getBean(beanNames[i]);
			MockUtil mockUtil = new MockUtil();
			if (mockUtil.isMock(bean)) {
				Mockito.reset(bean);
			}
		}
	}
}
