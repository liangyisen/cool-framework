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
import com.eiff.framework.fs.fastdfs.ServerInfo;
import com.eiff.framework.fs.fastdfs.StorageServer;
import com.eiff.framework.fs.fastdfs.StructStorageStat;
import com.eiff.framework.fs.fastdfs.TrackerCommand;
import com.eiff.framework.fs.fastdfs.common.MyException;
import com.eiff.framework.fs.fastdfs.conf.NonGlobalConfig;
import com.eiff.framework.fs.fastdfs.pool.checker.StorageDataChecker;
import com.eiff.framework.fs.fastdfs.pool.exception.KeyLost;

public class StorageFactory implements KeyedPooledObjectFactory<String, StorageServer> {

	private static Logger LOGGER = LoggerFactory.getLogger(StorageFactory.class);
	private CopyOnWriteArrayList<String> storageAddresss = new CopyOnWriteArrayList<>();
	private NonGlobalConfig config;

	public StorageFactory(NonGlobalConfig config) {
		this.config = config;
		new StorageDataChecker(config, storageAddresss).start();
	}

	@Override
	public PooledObject<StorageServer> makeObject(String key) throws Exception {
		TrackerCommand tkCommand = new TrackerCommand(this.config);
		ServerInfo[] storeStorages = tkCommand.getStorages();
		if (storeStorages == null)
			throw new KeyLost(key);
		for (int i = 0; i < storeStorages.length; i++) {
			String addr = storeStorages[i].getIpAddr() + ":" + storeStorages[i].getPort();
			if (addr.equals(key)) {
				if (!storageAddresss.contains(key)) {
					storageAddresss.add(key);
				}
				LOGGER.info("new storage connection created");
				return new DefaultPooledObject<StorageServer>(new StorageServer(storeStorages[i].getIpAddr(),
						storeStorages[i].getPort(), storeStorages[i].getStorePath(), config));
			}
		}
		storageAddresss.remove(key);
		throw new KeyLost(key);
	}

	@Override
	public void destroyObject(String key, PooledObject<StorageServer> p) throws Exception {
		p.getObject().close();
	}

	@Override
	public boolean validateObject(String key, PooledObject<StorageServer> p) {
		try {
			boolean activeTest = ProtoCommon.activeTest(p.getObject().getSocket());
			if (!activeTest) {
				storageAddresss.remove(key);
			}
			return activeTest;
		} catch (IOException e) {
			LOGGER.error("checkfailed", e);
			storageAddresss.remove(key);
			return false;
		}
	}

	@Override
	public void activateObject(String key, PooledObject<StorageServer> p) throws Exception {
	}

	@Override
	public void passivateObject(String key, PooledObject<StorageServer> p) throws Exception {
	}

	AtomicInteger atomIndex = new AtomicInteger(0);

	public String getNextKey() throws MyException {
		if (this.storageAddresss.size() == 0) {
			StructStorageStat[] listStorages;
			try {
				listStorages = new TrackerCommand(this.config).listStorages();
				for (int i = 0; i < listStorages.length; i++) {
					if (listStorages[i].getStatus() != ProtoCommon.FDFS_STORAGE_STATUS_ACTIVE)
						continue;
					String address = listStorages[i].getIpAddr() + ":" + listStorages[i].getStoragePort();
					this.storageAddresss.add(address);
				}
			} catch (IOException e) {
				LOGGER.error("", e);
			}
		}
		int index = atomIndex.incrementAndGet();
		if (index < 0) {
			index = 0;
			atomIndex = new AtomicInteger(0);
		}
		try {
			return storageAddresss.get(index % storageAddresss.size());
		} catch (Exception e) {
			LOGGER.error("storageAddresss is empty", e);
			return getNextAvailableKey();
		}
	}

	public String getNextAvailableKey() throws MyException {
		StructStorageStat[] listStorage;
		try {
			listStorage = new TrackerCommand(this.config).listStorages();
			for (int i = 0; i < listStorage.length; i++) {
				String address = listStorage[i].getIpAddr() + ":" + listStorage[i].getStoragePort();
				if (listStorage[i].getStatus() != ProtoCommon.FDFS_STORAGE_STATUS_ACTIVE) {
					this.storageAddresss.remove(address);
				} else {
					if (!this.storageAddresss.contains(address)) {
						this.storageAddresss.add(address);
					}
					return address;
				}
			}
		} catch (IOException e) {
			LOGGER.error("no storage can be used", e);
		}
		throw new MyException("no storage can be used");
	}
}