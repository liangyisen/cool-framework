package com.eiff.framework.test.processer;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.Set;

import org.mockito.Mockito;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;
import org.springframework.util.CollectionUtils;

import com.eiff.framework.test.automock.group.MockClazPack;
import com.eiff.framework.test.automock.vo.MockBeanDefinition;
import com.eiff.framework.test.factory.TestProxyFactory;
import com.eiff.framework.test.listener.MockExecutionListener;
import com.eiff.framework.test.utils.MockTool;

@SuppressWarnings({ "rawtypes" })
public class EifInstantiationAwareBeanPostProcessor extends InstantiationAwareBeanPostProcessorAdapter {

	public EifInstantiationAwareBeanPostProcessor() {
		super();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object postProcessBeforeInstantiation(Class beanClass, String beanName) throws BeansException {

		MockClazPack mockClazPack = MockExecutionListener.MOCKPACK.get();
		Set<MockBeanDefinition> includes = mockClazPack.getInclude();
		if (!CollectionUtils.isEmpty(includes)) {
			for (MockBeanDefinition include : includes) {
				try {
					if (include.getClassName().equals(beanClass.getName())) {
						Object mock = Mockito.mock(beanClass);
						include.getDefaultMockAction().wrapup(mock);
						mock = MockTool.mockFactoryBean(mock);
						System.out.println(beanClass.getName() + ":::" + include.getBeanName());
						return mock;
					}
				} catch (Exception e) {
					// TODO
					e.printStackTrace();
				}
			}
		}
		Set<MockBeanDefinition> includeParents = mockClazPack.getIncludeParent();
		if (!CollectionUtils.isEmpty(includeParents)) {
			for (MockBeanDefinition includeParent : includeParents) {
				try {
					if (Class.forName(includeParent.getClassName()).isAssignableFrom(beanClass)) {
						Object mock = Mockito.mock(beanClass);
						includeParent.getDefaultMockAction().wrapup(mock);
						mock = MockTool.mockFactoryBean(mock);
						return mock;
					}
				} catch (Exception e) {
					// TODO
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		try {
			if (bean.getClass().getName().equals("com.alibaba.dubbo.config.spring.ReferenceBean")) {
				Field declaredField = bean.getClass().getSuperclass().getDeclaredField("interfaceName");
				declaredField.setAccessible(true);
				String interfaceName = "" + declaredField.get(bean);
				return new TestProxyFactory(interfaceName);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bean;
	}

	@Override
	public PropertyValues postProcessPropertyValues(PropertyValues pvs, PropertyDescriptor[] pds, Object bean,
			String beanName) throws BeansException {
		return pvs;
	}
}