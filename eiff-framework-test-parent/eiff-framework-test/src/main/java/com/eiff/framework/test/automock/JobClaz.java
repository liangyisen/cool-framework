package com.eiff.framework.test.automock;

import java.util.Set;

import com.eiff.framework.test.automock.group.MockClazPack;
import com.eiff.framework.test.automock.vo.MockBeanDefinition;

//TODO
public class JobClaz extends MockInfo{

	public JobClaz(MockClazPack mockClazPack) {
		super(mockClazPack);
	}

	@Override
	public Set<MockBeanDefinition> getIncludeList() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<MockBeanDefinition> getIncludeParentList() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<MockBeanDefinition> getExcludeList() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<MockBeanDefinition> getExcludeParentList() {
		// TODO Auto-generated method stub
		return null;
	}
}
