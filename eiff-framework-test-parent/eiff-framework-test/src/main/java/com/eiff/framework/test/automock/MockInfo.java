package com.eiff.framework.test.automock;

import java.util.Set;

import com.eiff.framework.test.automock.group.MockClazPack;
import com.eiff.framework.test.automock.vo.MockBeanDefinition;

public abstract class MockInfo {

	public abstract Set<MockBeanDefinition> getIncludeList();

	public abstract Set<MockBeanDefinition> getIncludeParentList();

	public abstract Set<MockBeanDefinition> getExcludeList();

	public abstract Set<MockBeanDefinition> getExcludeParentList();

	public MockInfo(MockClazPack mockClazPack) {
		mergeBeanDefine(mockClazPack.getInclude(), this.getIncludeList());
		mergeBeanDefine(mockClazPack.getIncludeParent(), this.getIncludeParentList());
		mergeBeanDefine(mockClazPack.getExclude(), this.getExcludeList());
		mergeBeanDefine(mockClazPack.getExcludeParent(), this.getExcludeParentList());
	}

	private void mergeBeanDefine(Set<MockBeanDefinition> dest, Set<MockBeanDefinition> origin) {
		if (origin != null) {
			for (MockBeanDefinition define : origin) {
				try {
					Class.forName(define.getClassName());
					dest.add(define);
				} catch (Exception e) {
				}
			}
		}
	}
}
