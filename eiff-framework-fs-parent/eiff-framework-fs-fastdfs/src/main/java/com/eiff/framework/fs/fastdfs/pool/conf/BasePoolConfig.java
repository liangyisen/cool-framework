package com.eiff.framework.fs.fastdfs.pool.conf;

import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;

public class BasePoolConfig extends GenericKeyedObjectPoolConfig {

	public BasePoolConfig() {
		this.setTestWhileIdle(true);
		this.setTestOnBorrow(true);
		this.setMaxWaitMillis(2000);
		// 5分钟一次心跳检查，剔除所有存活4分钟的idle链接
		this.setTimeBetweenEvictionRunsMillis(5 * 60 * 1000);
		this.setMinEvictableIdleTimeMillis(4 * 60 * 1000);
	}

	private String poolName = "UNKNOWN";
	private int initSize;

	public String getPoolName() {
		return poolName;
	}

	public void setPoolName(String poolName) {
		this.poolName = poolName;
	}

	public int getInitSize() {
		return initSize;
	}

	public void setInitSize(int initSize) {
		this.initSize = initSize;
	}
}
