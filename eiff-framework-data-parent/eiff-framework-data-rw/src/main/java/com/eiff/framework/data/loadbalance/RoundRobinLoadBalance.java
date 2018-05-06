package com.eiff.framework.data.loadbalance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

public class RoundRobinLoadBalance implements LoadBalance<String> {

	private static final int MIN_LB_FACTOR = 1;

	private List<String> targets;
	private int currentPos = 0;

	public RoundRobinLoadBalance(Map<String, Integer> lbFactors) {
		Assert.notEmpty(lbFactors, "lbFactors must not null");
		this.targets = initTargets(lbFactors);
	}

	public List<String> initTargets(Map<String, Integer> lbFactors) {
		if (lbFactors == null || lbFactors.size() == 0) {
			return null;
		}

		fixFactor(lbFactors);

		Collection<Integer> factors = lbFactors.values();
		int min = Collections.min(factors);
		if (min > MIN_LB_FACTOR && canModAll(min, factors)) {
			return buildBalanceTargets(lbFactors, min);
		}

		return buildBalanceTargets(lbFactors, MIN_LB_FACTOR);
	}

	private void fixFactor(Map<String, Integer> lbFactors) {
		for (Map.Entry<String, Integer> entry : lbFactors.entrySet()) {
			if (entry.getValue() < MIN_LB_FACTOR) {
				entry.setValue(MIN_LB_FACTOR);
			}
		}
	}

	private boolean canModAll(int baseFactor, Collection<Integer> factors) {
		for (Integer factor : factors) {
			if (factor % baseFactor != 0) {
				return false;
			}
		}
		return true;
	}

	private List<String> buildBalanceTargets(Map<String, Integer> lbFactors, int baseFactor) {
		List<String> targets = new ArrayList<String>();
		for (Map.Entry<String, Integer> entry : lbFactors.entrySet()) {
			int count = entry.getValue() / baseFactor;

			for (int i = 0; i < count; i++) {
				targets.add(entry.getKey());
			}
		}

		return targets;
	}

	@Override
	public synchronized String elect() {
		if (CollectionUtils.isEmpty(targets)) {
			return null;
		}
		if (currentPos >= targets.size()) {
			currentPos = 0;
		}
		return targets.get(currentPos++);
	}
}