package com.eiff.framework.test.listener;

import java.lang.reflect.Constructor;

import org.mockito.MockitoAnnotations;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

import com.eiff.framework.test.annotation.AutoMockComponent;
import com.eiff.framework.test.automock.MockInfo;
import com.eiff.framework.test.automock.group.MockClazPack;

public class MockExecutionListener extends AbstractTestExecutionListener {
	
	public static ThreadLocal<MockClazPack> MOCKPACK = new ThreadLocal<>();
	@Override
	public void beforeTestClass(TestContext testContext) throws Exception {
		
		AutoMockComponent annotation = testContext.getTestClass().getAnnotation(AutoMockComponent.class);
		if (annotation != null) {
			Class<? extends MockInfo>[] values = annotation.values();
			MockClazPack mockClazPack = new MockClazPack();
			for (int i = 0; i < values.length; i++) {
				Constructor<? extends MockInfo> constructor = values[i].getConstructor(mockClazPack.getClass());
				constructor.newInstance(mockClazPack);
			}
			MOCKPACK.set(mockClazPack);
		}
		super.beforeTestClass(testContext);
	}

	@Override
	public void beforeTestMethod(TestContext testContext) throws Exception {
		super.beforeTestMethod(testContext);
		MockitoAnnotations.initMocks(testContext.getTestInstance());
	}
	
	@Override
	public void afterTestClass(TestContext testContext) throws Exception {
		MOCKPACK.remove();
		super.afterTestClass(testContext);
	}
}
