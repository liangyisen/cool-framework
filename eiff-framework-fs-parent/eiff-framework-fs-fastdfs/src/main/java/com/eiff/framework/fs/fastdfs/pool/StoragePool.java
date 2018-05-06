package com.eiff.framework.fs.fastdfs.pool;

import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eiff.framework.fs.fastdfs.StorageServer;
import com.eiff.framework.fs.fastdfs.common.MyException;
import com.eiff.framework.fs.fastdfs.pool.conf.BasePoolConfig;
import com.eiff.framework.fs.fastdfs.pool.exception.KeyLost;
import com.eiff.framework.fs.fastdfs.pool.factory.StorageFactory;
import com.eiff.framework.fs.fastdfs.pool.manager.PoolStatManager;

public class StoragePool extends BasicPool<String, StorageServer> {

	private static Logger LOGGER = LoggerFactory.getLogger(StoragePool.class);

	private StorageFactory stFactory;

	public StoragePool(StorageFactory factory, BasePoolConfig poolConfig) {
		super(factory, poolConfig);
		PoolStatManager.getStoragePools().put(this, poolConfig.getPoolName());
		this.stFactory = factory;
		for (int i = 0; i < poolConfig.getInitSize(); i++) {
			try {
				this.borrowObject();
			} catch (Exception e) {
				LOGGER.info("init pool failed", e);
			}
		}
	}

	public StorageServer borrowObject() throws Exception {
		try {
			return this.borrowObject(this.stFactory.getNextKey());
		} catch (KeyLost e) {
			String next = this.stFactory.getNextAvailableKey();
			return this.borrowObject(next);
		}
	}

	@Override
	public StorageServer borrowObject(String key) throws Exception {
		try {
			PoolStatManager.getStoragekeys().add(key);
			StorageServer borrowObject = super.borrowObject(key);
			borrowObject.setDataSource(this);
			return borrowObject;
		} catch (NoSuchElementException nse) {
			LOGGER.error("NoSuchElementException for " + key, nse);
			throw new MyException(nse);
		}
	}

	@Override
	public StorageServer borrowObject(String key, long borrowMaxWaitMillis) throws Exception {
		try {
			StorageServer borrowObject = super.borrowObject(key, borrowMaxWaitMillis);
			borrowObject.setDataSource(this);
			return borrowObject;
		} catch (NoSuchElementException nse) {
			LOGGER.error("NoSuchElementException for " + key, nse);
			throw new MyException(nse);
		}
	}
}
