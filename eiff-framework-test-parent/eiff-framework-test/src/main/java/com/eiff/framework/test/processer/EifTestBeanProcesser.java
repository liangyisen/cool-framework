package com.eiff.framework.test.processer;

import java.util.ArrayList;
import java.util.List;

import org.mockito.Mockito;
import org.mockito.exceptions.base.MockitoException;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.BeanPostProcessor;

import com.eiff.framework.test.annotation.NoAutoProcess;
import com.eiff.framework.test.runner.DefaultRunner;

public class EifTestBeanProcesser implements BeanPostProcessor {

	@SuppressWarnings("unused")
	private List<String> noAutoProcesserList = new ArrayList<>();

	public EifTestBeanProcesser() {

	}

	public void setNoAutoProcesserList(List<String> noAutoProcesserList) {
		this.noAutoProcesserList = noAutoProcesserList;
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		if (bean != null
				&& (bean.getClass().getName().equals("PageHelper")
						|| bean.getClass().getName().equals("com.eiff.framework.data.datasource.ReadWriteDataSource"))
				|| beanName.equals("webMvcConfig") || beanName.startsWith("com.eiff.framework.data.mybatis.interceptor.log")) {
			return bean;
		}
		if (bean != null && bean instanceof FactoryBean) {
			return bean;
		}
		if (bean.getClass().getName().startsWith("com.eiff") && !(bean instanceof NoAutoProcess)
				&& !(bean.toString().startsWith("Mock"))) {
			switch (DefaultRunner.DEFAULT_MOCK_LEVEL) {
			case REAL:
				return bean;
			case Mock:
				Object mock = null;
				try {
					mock = Mockito.mock(bean.getClass());
				} catch (MockitoException e) {
					return bean;
				}
				return mock;
			case SPY:
				Object spy = null;
				try {
					spy = Mockito.spy(bean);
				} catch (MockitoException e) {
					return bean;
				}
				return spy;
			default:
				return bean;
			}

		} else {
			return bean;
		}
	}
}
