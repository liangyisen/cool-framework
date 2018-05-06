package com.eiff.framework.test.automock;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.eiff.framework.idgen.api.IIdGenerator;
import com.eiff.framework.idgen.zk.impl.ZkId17Generator;
import com.eiff.framework.idgen.zk.impl.ZkId20Generator;
import com.eiff.framework.test.automock.group.MockClazPack;
import com.eiff.framework.test.automock.vo.DefaultMockAction;
import com.eiff.framework.test.automock.vo.MockBeanDefinition;

public class IdgenClaz extends MockInfo {

	public IdgenClaz(MockClazPack mockClazPack) {
		super(mockClazPack);
	}

	@Override
	public Set<MockBeanDefinition> getIncludeList() {
		return null;
	}

	public static MockBeanDefinition buildIdGen(String beanName) {
		return new MockBeanDefinition("com.eiff.framework.idgen.api.IIdGenerator", beanName, new DefaultMockAction() {

			@Override
			public void wrapup(Object mockObj) {
				try {
					if (mockObj instanceof ZkId20Generator) {
						ZkId20Generator zk20 = (ZkId20Generator) mockObj;
						try {
							Mockito.doAnswer(new Answer<String>() {
								@Override
								public String answer(InvocationOnMock invocation) throws Throwable {
									String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 20);
									return uuid;
								}
							}).when(zk20).genId();
						} catch (Exception e) {
							e.printStackTrace();
						}
					} else if (mockObj instanceof ZkId17Generator) {
						ZkId17Generator zk20 = (ZkId17Generator) mockObj;
						try {
							Mockito.doAnswer(new Answer<String>() {
								@Override
								public String answer(InvocationOnMock invocation) throws Throwable {
									String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 17);
									return uuid;
								}
							}).when(zk20).genId();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				} catch (Throwable e) {
				}

				try {
					Mockito.doAnswer(new Answer<String>() {
						@Override
						public String answer(InvocationOnMock invocation) throws Throwable {
							return System.currentTimeMillis() / 100000 + "";
						}
					}).when((IIdGenerator) mockObj).genId();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	@Override
	public Set<MockBeanDefinition> getIncludeParentList() {
		Set<MockBeanDefinition> includeParentSet = new HashSet<>();
		includeParentSet.add(IdgenClaz.buildIdGen("iIdGenerator"));
		return includeParentSet;
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
