package com.eiff.framework.fs.fastdfs.pool;

import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eiff.framework.fs.fastdfs.TrackerServer;
import com.eiff.framework.fs.fastdfs.common.MyException;
import com.eiff.framework.fs.fastdfs.pool.conf.BasePoolConfig;
import com.eiff.framework.fs.fastdfs.pool.exception.KeyLost;
import com.eiff.framework.fs.fastdfs.pool.factory.TrackerFactory;
import com.eiff.framework.fs.fastdfs.pool.manager.PoolStatManager;

public class TrackerPool extends BasicPool<String, TrackerServer> {
	private static Logger LOGGER = LoggerFactory.getLogger(TrackerPool.class);

	private TrackerFactory tkFactory;

	public TrackerPool(TrackerFactory factory, BasePoolConfig poolConfig) {
		super(factory, poolConfig);
		PoolStatManager.getTrackerPools().put(this, poolConfig.getPoolName());
		this.tkFactory = factory;
		for (int i = 0; i < poolConfig.getInitSize(); i++) {
			try {
				this.borrowObject();
			} catch (Exception e) {
				LOGGER.info("init pool failed", e);
			}
		}
	}

	public TrackerServer borrowObject() throws Exception {
		try {
			return this.borrowObject(this.tkFactory.getNextKey());
		} catch (KeyLost e) {
			String next = this.tkFactory.getNextAvailableKey();
			return this.borrowObject(next);
		}
	}

	@Override
	public TrackerServer borrowObject(String key) throws Exception {
		try {
			PoolStatManager.getTrackerkeys().add(key);
			TrackerServer borrowObject = super.borrowObject(key);
			borrowObject.setDataSource(this);
			return borrowObject;
		} catch (NoSuchElementException nse) {
			LOGGER.error("NoSuchElementException for " + key, nse);
			throw new MyException(nse);
		}
	}

	@Override
	public TrackerServer borrowObject(String key, long borrowMaxWaitMillis) throws Exception {
		try {
			TrackerServer borrowObject = super.borrowObject(key, borrowMaxWaitMillis);
			borrowObject.setDataSource(this);
			return borrowObject;
		} catch (NoSuchElementException nse) {
			LOGGER.error("NoSuchElementException for " + key, nse);
			throw new MyException(nse);
		}
	}
}
