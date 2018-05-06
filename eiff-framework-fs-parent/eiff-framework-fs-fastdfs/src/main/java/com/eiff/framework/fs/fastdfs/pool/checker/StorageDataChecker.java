package com.eiff.framework.fs.fastdfs.pool.checker;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eiff.framework.fs.fastdfs.ProtoCommon;
import com.eiff.framework.fs.fastdfs.StructStorageStat;
import com.eiff.framework.fs.fastdfs.TrackerCommand;
import com.eiff.framework.fs.fastdfs.conf.NonGlobalConfig;

public class StorageDataChecker extends Thread {
	private static Logger LOGGER = LoggerFactory.getLogger(StorageDataChecker.class);

	private NonGlobalConfig config;
	private CopyOnWriteArrayList<String> keys;

	public StorageDataChecker(NonGlobalConfig config, CopyOnWriteArrayList<String> keys) {
		this.config = config;
		this.keys = keys;
	}

	@Override
	public void run() {
		while (true) {
			TrackerCommand tkCommand = new TrackerCommand(this.config);
			try {
				StructStorageStat[] listStorages = tkCommand.listStorages();
				for (int i = 0; i < listStorages.length; i++) {
					if (listStorages[i].getStatus() != ProtoCommon.FDFS_STORAGE_STATUS_ACTIVE)
						continue;
					String address = listStorages[i].getIpAddr() + ":" + listStorages[i].getStoragePort();
					if (!keys.contains(address)) {
						keys.add(address);
					}
				}
			} catch (Exception e1) {
				LOGGER.error("", e1);
			}

			try {
				TimeUnit.SECONDS.sleep(60);
			} catch (InterruptedException e) {
			}
		}
	}
}
