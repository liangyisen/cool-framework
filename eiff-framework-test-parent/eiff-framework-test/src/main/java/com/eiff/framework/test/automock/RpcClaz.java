package com.eiff.framework.test.automock;

import java.util.Set;

import com.eiff.framework.test.automock.group.MockClazPack;
import com.eiff.framework.test.automock.vo.MockBeanDefinition;

public class RpcClaz extends MockInfo{

	public RpcClaz(MockClazPack mockClazPack) {
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
