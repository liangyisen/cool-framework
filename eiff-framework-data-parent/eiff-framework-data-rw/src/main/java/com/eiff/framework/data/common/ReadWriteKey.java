package com.eiff.framework.data.common;

import com.eiff.framework.data.loadbalance.LoadBalance;

public class ReadWriteKey {

	private LoadBalance<String> loadBalance;
	private String writeKey;

	public void setWriteKey() {
		ShardingUtil.setReadWriteKey(writeKey);
	}

	public void setReadKey() {
		if (loadBalance == null) {
			ShardingUtil.setReadWriteKey(writeKey);
		} else {
			ShardingUtil.setReadWriteKey(loadBalance.elect());
		}
	}

	public String getKey() {
		if (ShardingUtil.getReadWriteKey() == null) {
			return writeKey;
		}

		return ShardingUtil.getReadWriteKey();
	}

	// getter and setter

	public LoadBalance<String> getLoadBalance() {
		return loadBalance;
	}

	public void setLoadBalance(LoadBalance<String> loadBalance) {
		this.loadBalance = loadBalance;
	}

	public String getWriteKey() {
		return writeKey;
	}

	public void setWriteKey(String writeKey) {
		this.writeKey = writeKey;
	}
}
