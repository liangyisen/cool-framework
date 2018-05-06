package com.eiff.framework.test.factory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.mockito.Mockito;
import org.springframework.beans.factory.FactoryBean;

import com.eiff.framework.test.annotation.NoAutoProcess;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class TestProxyFactory implements FactoryBean<Object>, NoAutoProcess {
	private String className;
	private Map<String, Object> mockMap = new ConcurrentHashMap<String, Object>();

	public void setClassName(String className) {
		this.className = className;
	}

	public TestProxyFactory(String className) {
		this.className = className;
	}

	public TestProxyFactory() {
	}

	public <T> T getBean(String className) {
		if (mockMap.get(className) != null) {
			return (T) mockMap.get(className);
		}
		synchronized (this) {
			this.className = className;

			Class<T> targetClass;
			try {
				targetClass = (Class<T>) Class.forName(className);
			} catch (ClassNotFoundException e) {
				return null;
				// e.printStackTrace();
				// throw new RuntimeException(e);
			}
			T mock = Mockito.mock(targetClass);
			if (mock instanceof FactoryBean) {
				try {
					Mockito.when(((FactoryBean) mock).getObject()).thenReturn(mock);

				} catch (Exception e) {
					FactoryBean newInstance;
					try {
						newInstance = (FactoryBean) targetClass.newInstance();
						mock = (T) Mockito.mock(newInstance.getObjectType());
					} catch (Exception e1) {
						throw new RuntimeException(" cannot creare the factorybean:" + className, e);
					}
				}
				FactoryBean newInstance;
				try {
					newInstance = (FactoryBean) targetClass.newInstance();
					this.className = newInstance.getObjectType().getName();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
			mockDetailsWith(mock);
			mockMap.put(className, mock);
			return mock;
		}
	}

	public <T> T getSpyBean(String className) {
		synchronized (this) {
			this.className = className;

			Class<T> targetClass;
			try {
				targetClass = (Class<T>) Class.forName(className);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
			Object obj = null;
			try {
				obj = targetClass.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new RuntimeException(e);
			}
			T mock = (T) Mockito.spy(obj);

			mockDetailsWith(mock);
			return mock;
		}
	}

	protected void mockDetailsWith(Object instance) {

	}

	public Class<?> getObjectType() {
		synchronized (this) {
			try {
				if (this.className == null)
					return this.getClass();
				Class<?> returnName = Class.forName(this.className);
				return returnName;
			} catch (ClassNotFoundException ex1) {
				return null;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
	}

	public Object getObject() throws Exception {
		synchronized (this) {
			if (this.className != null) {
				return getBean(this.className);
			}
		}
		return this;
	}

	public boolean isSingleton() {
		return false;
	}
}
