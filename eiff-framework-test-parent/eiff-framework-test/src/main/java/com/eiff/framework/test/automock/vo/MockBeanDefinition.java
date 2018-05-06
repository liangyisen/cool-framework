package com.eiff.framework.test.automock.vo;

public class MockBeanDefinition {
	private String className;
	private String beanName;
	private DefaultMockAction defaultMockAction = new DefaultMockAction() {
		
		@Override
		public void wrapup(Object mockObj) {
		}
	};
	
	public MockBeanDefinition(String className, String beanName) {
		super();
		this.className = className;
		this.beanName = beanName;
	}
	
	public MockBeanDefinition(String className, String beanName, DefaultMockAction defaultMockAction) {
		super();
		this.className = className;
		this.beanName = beanName;
		this.defaultMockAction = defaultMockAction;
	}
	public String getClassName() {
		return className;
	}
	public void setClassName(String className) {
		this.className = className;
	}
	public String getBeanName() {
		return beanName;
	}
	public void setBeanName(String beanName) {
		this.beanName = beanName;
	}
	public DefaultMockAction getDefaultMockAction() {
		return defaultMockAction;
	}
	public void setDefaultMockAction(DefaultMockAction defaultMockAction) {
		this.defaultMockAction = defaultMockAction;
	}
}
