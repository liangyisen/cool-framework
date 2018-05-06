
package com.eiff.framework.fs.fastdfs;

import java.io.IOException;

import com.eiff.framework.fs.fastdfs.conf.NonGlobalConfig;

/**
 * Tracker client
 * 
 * @author Happy Fish / YuQing
 * @version Version 1.19
 */
public class TrackerCommand extends TrackerClient {

	public TrackerCommand(NonGlobalConfig config) {
		super(config);
	}

	public TrackerServer getConnection() throws IOException {
		return super.getConnection();
	}

	public ServerInfo[] getStorages() throws IOException {
		return super.getStorages();
	}

	public String getFetchStorageInfo(TrackerServer trackerServer, String fileId) throws IOException {
		return super.getFetchStorageInfo(trackerServer, fileId);
	}

	public String getUpdateStorageInfo(TrackerServer trackerServer, String fileId) throws IOException {
		return super.getUpdateStorageInfo(trackerServer, fileId);
	}

	public StructStorageStat[] listStorages() throws IOException {
		return super.listStorages();
	}

	public StorageServer[] getStoreStorages() throws IOException {
		return super.getStoreStorages();
	}

	public StructGroupStat[] listGroups() throws IOException {
		return super.listGroups();
	}

	public byte getErrorCode() {
		return super.errno;
	}

	public StructStorageStat[] listStorages(TrackerServer trackerServer, String groupName) throws IOException {
		return super.listStorages(trackerServer, groupName);
	}
}
