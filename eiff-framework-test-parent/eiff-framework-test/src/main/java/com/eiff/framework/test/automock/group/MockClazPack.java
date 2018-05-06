package com.eiff.framework.test.automock.group;

import java.util.HashSet;
import java.util.Set;

import com.eiff.framework.test.automock.vo.MockBeanDefinition;

public class MockClazPack {
	private Set<MockBeanDefinition> include = new HashSet<>();
	private Set<MockBeanDefinition> includeParent = new HashSet<>();
	private Set<MockBeanDefinition> exclude = new HashSet<>();
	private Set<MockBeanDefinition> excludeParent = new HashSet<>();
	public Set<MockBeanDefinition> getInclude() {
		return include;
	}
	public Set<MockBeanDefinition> getIncludeParent() {
		return includeParent;
	}
	public Set<MockBeanDefinition> getExclude() {
		return exclude;
	}
	public Set<MockBeanDefinition> getExcludeParent() {
		return excludeParent;
	}
}
