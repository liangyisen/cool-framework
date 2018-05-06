package com.eiff.framework.data.loadbalance;

import java.util.List;
import java.util.Random;

import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

public class RandomLoadBalance implements LoadBalance<String> {

	private List<String> targets;
	private final Random random = new Random();

	public RandomLoadBalance(List<String> targets) {
		Assert.notEmpty(targets, "targets must not empty");
		this.targets = targets;
	}

	@Override
	public synchronized String elect() {
		if (CollectionUtils.isEmpty(targets)) {
			return null;
		}
		return targets.get(random.nextInt(targets.size()));
	}
}
