package com.eiff.framework.fs.fastdfs.pool.factory;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.pool2.KeyedPooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eiff.framework.fs.fastdfs.ProtoCommon;
import com.eiff.framework.fs.fastdfs.TrackerServer;
import com.eiff.framework.fs.fastdfs.common.MyException;
import com.eiff.framework.fs.fastdfs.conf.NonGlobalConfig;
import com.eiff.framework.fs.fastdfs.pool.checker.TrackerDataChecker;

public class TrackerFactory implements KeyedPooledObjectFactory<String, TrackerServer> {

	private static Logger LOGGER = LoggerFactory.getLogger(TrackerFactory.class);
	private CopyOnWriteArrayList<String> trackerAddresss = new CopyOnWriteArrayList<>();
	private NonGlobalConfig config;

	public TrackerFactory(NonGlobalConfig config) {
		this.config = config;
		new TrackerDataChecker(this.config, trackerAddresss).start();
	}

	@Override
	public PooledObject<TrackerServer> makeObject(String key) throws Exception {
		int indexOf = trackerAddresss.indexOf(key);
		LOGGER.info("new tracker connection created");
		return new DefaultPooledObject<TrackerServer>(this.config.getTrackerGroup().getConnection(indexOf));
	}

	@Override
	public void destroyObject(String key, PooledObject<TrackerServer> p) throws Exception {
		p.getObject().close();
	}

	@Override
	public boolean validateObject(String key, PooledObject<TrackerServer> p) {
		try {
			boolean activeTest = ProtoCommon.activeTest(p.getObject().getSocket());
			if (!activeTest) {
				trackerAddresss.remove(key);
			}
			return activeTest;
		} catch (IOException e) {
			LOGGER.error("checkfailed", e);
			trackerAddresss.remove(key);
			return false;
		}
	}

	@Override
	public void activateObject(String key, PooledObject<TrackerServer> p) throws Exception {
	}

	@Override
	public void passivateObject(String key, PooledObject<TrackerServer> p) throws Exception {
	}

	AtomicInteger atomIndex = new AtomicInteger(0);

	public String getNextKey() throws MyException {
		int index = atomIndex.incrementAndGet();
		if (index < 0) {
			index = 0;
			atomIndex = new AtomicInteger(0);
		}
		try {
			return trackerAddresss.get(index % trackerAddresss.size());
		} catch (Exception e) {
			LOGGER.error("trackerAddresss is empty", e);
			return getNextAvailableKey();
		}
	}

	public String getNextAvailableKey() throws MyException {
		try {
			String activeAddress = this.config.getTrackerGroup().getConnection().getAddrWithPort();
			if (!this.trackerAddresss.contains(activeAddress)) {
				this.trackerAddresss.add(activeAddress);
			}
			return activeAddress;
		} catch (Exception e) {
			LOGGER.error("no tracker can be used", e);
		}
		throw new MyException("no tracker can be used");
	}
}
