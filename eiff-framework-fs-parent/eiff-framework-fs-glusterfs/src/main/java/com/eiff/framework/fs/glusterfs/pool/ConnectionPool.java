package com.eiff.framework.fs.glusterfs.pool;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eiff.framework.fs.glusterfs.exception.CannotGetPoolException;

public class ConnectionPool {

	private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionPool.class);

	private long timeout;
	@SuppressWarnings("unused")
	private int maxConnection;
	private Semaphore readWriteLock;

	public ConnectionPool(long timeout, int maxConnection) {
		this.timeout = timeout;
		this.maxConnection = maxConnection;
		readWriteLock = new Semaphore(maxConnection);
	}

	public void getConnection() throws CannotGetPoolException {
		try {
			if(!this.readWriteLock.tryAcquire(this.timeout, TimeUnit.MILLISECONDS)){
				throw new CannotGetPoolException();
			}
		} catch (InterruptedException e) {
			LOGGER.error("", e);
			throw new CannotGetPoolException();
		}
	}

	public void releaseConnection() {
		this.readWriteLock.release();
	}
}
