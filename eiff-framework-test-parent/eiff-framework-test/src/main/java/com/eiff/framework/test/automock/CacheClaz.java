package com.eiff.framework.test.automock;

import java.util.HashSet;
import java.util.Set;

import com.eiff.framework.test.automock.group.MockClazPack;
import com.eiff.framework.test.automock.vo.MockBeanDefinition;

public class CacheClaz extends MockInfo{

	public CacheClaz(MockClazPack mockClazPack) {
		super(mockClazPack);
	}

	@Override
	public Set<MockBeanDefinition> getIncludeList() {
		Set<MockBeanDefinition> includeSet = new HashSet<>();
		includeSet.add(new MockBeanDefinition("com.eiff.framework.cache.redis.client.HadesClient", "hadesClient"));
		includeSet.add(new MockBeanDefinition("org.redisson.spring.cache.RedissonSpringCacheManager", "redissonSpringCacheManager"));
		includeSet.add(new MockBeanDefinition("com.eiff.framework.common.biz.code.CommonRspCode.RedisConcurrentLock", "redisConcurrentLock"));
		
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
