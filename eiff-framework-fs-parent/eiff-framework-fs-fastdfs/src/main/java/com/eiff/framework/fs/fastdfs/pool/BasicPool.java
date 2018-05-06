package com.eiff.framework.fs.fastdfs.pool;

import org.apache.commons.pool2.KeyedPooledObjectFactory;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eiff.framework.fs.fastdfs.pool.conf.BasePoolConfig;

public abstract class BasicPool<K, T> extends GenericKeyedObjectPool<K, T> {
	private BasePoolConfig poolConfig;

	public BasicPool(KeyedPooledObjectFactory<K, T> factory, BasePoolConfig poolConfig) {
		super(factory, poolConfig);
		this.poolConfig = poolConfig;
	}

	private static Logger LOGGER = LoggerFactory.getLogger(BasicPool.class);

	@Override
	public T borrowObject(K key) throws Exception {
		long maxBorrowWaitTimeMillis = this.getMaxBorrowWaitTimeMillis();
		T borrowObject = super.borrowObject(key);
		if (this.getMaxBorrowWaitTimeMillis() > maxBorrowWaitTimeMillis) {
			LOGGER.info("borrow wait more");
		}
		return borrowObject;
	}

	public int getMaxConcurrentCount() {
		return this.getNumWaiters() + this.getNumActive();
	}

	public int getMaxConcurrentCount(K key) {
		Integer waitByKey = this.getNumWaitersByKey().get(key);
		if (waitByKey == null)
			return this.getNumActive(key);
		return this.getNumActive(key) + waitByKey;
	}

	public int getInitPoolSize() {
		return poolConfig.getInitSize();
	}
}
