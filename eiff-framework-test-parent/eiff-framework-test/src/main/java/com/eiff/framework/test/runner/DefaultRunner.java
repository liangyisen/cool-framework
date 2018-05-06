package com.eiff.framework.test.runner;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@SuppressWarnings({ "unchecked" })
public class DefaultRunner extends SpringJUnit4ClassRunner {

	public static MockLevelEnum DEFAULT_MOCK_LEVEL;

	public DefaultRunner(Class<?> clazz) throws InitializationError {
		super(clazz);
	}

	@Override
	public void run(RunNotifier notifier) {
		AutoMockLevel findAnnotationValueByClass = findAnnotationValueByClass(AutoMockLevel.class);
		if (findAnnotationValueByClass != null) {
			DEFAULT_MOCK_LEVEL = findAnnotationValueByClass.value();
		} else {
			DEFAULT_MOCK_LEVEL = MockLevelEnum.REAL;
		}

		super.run(notifier);

	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	public static @interface AutoMockLevel {
		public MockLevelEnum value() default MockLevelEnum.SPY;
	}

	public static enum MockLevelEnum {
		REAL, SPY, Mock
	}

	private <T> T findAnnotationValueByClass(Class<T> annotationClass) {
		for (Annotation annotation : getTestClass().getAnnotations()) {
			if (annotation.annotationType().equals(annotationClass)) {
				return (T) annotation;
			}
		}
		throw new IllegalStateException(
				String.format("Can't find %s on test class: %s", annotationClass, getTestClass()));
	}
}
