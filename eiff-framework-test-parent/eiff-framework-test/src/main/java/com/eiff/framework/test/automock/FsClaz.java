package com.eiff.framework.test.automock;

import java.util.HashSet;
import java.util.Set;

import com.eiff.framework.test.automock.group.MockClazPack;
import com.eiff.framework.test.automock.vo.MockBeanDefinition;

public class FsClaz extends MockInfo{

	public FsClaz(MockClazPack mockClazPack) {
		super(mockClazPack);
	}

	@Override
	public Set<MockBeanDefinition> getIncludeList() {
		Set<MockBeanDefinition> includeSet = new HashSet<>();
		includeSet.add(new MockBeanDefinition("com.eiff.framework.fs.fastdfs.client.HfsClient", "hfsClient"));
		return includeSet;
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
