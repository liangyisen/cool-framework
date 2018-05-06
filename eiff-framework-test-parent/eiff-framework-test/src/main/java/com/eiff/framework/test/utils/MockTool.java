package com.eiff.framework.test.utils;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.FactoryBean;

public class MockTool {
	public static <T> T mockFactoryBean(final T t) {
		if (t instanceof FactoryBean) {
			FactoryBean<?> factory = (FactoryBean<?>) t;
			try {
				Mockito.doAnswer(new Answer<Object>() {
					@Override
					public Object answer(InvocationOnMock invocation) throws Throwable {
						return t;
					}
				}).when(factory).getObject();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return t;
	}
}
