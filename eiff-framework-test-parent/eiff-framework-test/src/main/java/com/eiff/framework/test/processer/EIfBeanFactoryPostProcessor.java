package com.eiff.framework.test.processer;

import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.CollectionUtils;

import com.eiff.framework.test.automock.group.MockClazPack;
import com.eiff.framework.test.automock.vo.MockBeanDefinition;
import com.eiff.framework.test.listener.MockExecutionListener;

public class EIfBeanFactoryPostProcessor implements BeanFactoryPostProcessor, ApplicationContextAware {
	
	private static final Log LOGGER = LogFactory.getLog(BeanFactoryPostProcessor.class);
	public static ApplicationContext SPRING_APPLICATIONCONTEXT;

	public EIfBeanFactoryPostProcessor() {
		super();
	}
	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory arg) throws BeansException {
		LOGGER.info("======: spring context load conf: " + arg.getClass().getName());
		DefaultListableBeanFactory xx = (DefaultListableBeanFactory) arg;
		MockClazPack mockClazPack = MockExecutionListener.MOCKPACK.get();
		Set<MockBeanDefinition> includes = mockClazPack.getInclude();
		if(!CollectionUtils.isEmpty(includes)){
			for (MockBeanDefinition include : includes) {
				GenericBeanDefinition genericBeanDefinition = new GenericBeanDefinition();
				genericBeanDefinition.setBeanClassName(include.getClassName());
				xx.registerBeanDefinition(include.getBeanName(), genericBeanDefinition);
			}
		}
		Set<MockBeanDefinition> includeParents = mockClazPack.getIncludeParent();
		if(!CollectionUtils.isEmpty(includeParents)){
			for (MockBeanDefinition includeParent : includeParents) {
				GenericBeanDefinition genericBeanDefinition = new GenericBeanDefinition();
				genericBeanDefinition.setBeanClassName(includeParent.getClassName());
				xx.registerBeanDefinition(includeParent.getBeanName(), genericBeanDefinition);
			}
		}
		xx.setAllowRawInjectionDespiteWrapping(true);
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		EIfBeanFactoryPostProcessor.SPRING_APPLICATIONCONTEXT = applicationContext;
	}
}