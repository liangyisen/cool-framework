package com.eiff.framework.fs.fastdfs.pool.manager;

import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import com.eiff.framework.fs.fastdfs.pool.StoragePool;
import com.eiff.framework.fs.fastdfs.pool.TrackerPool;

public class PoolStatManager {
	private final static Map<StoragePool, String> storagePools = Collections.synchronizedMap(new IdentityHashMap<StoragePool, String>());
	private final static Set<String> storageKeys =  Collections.synchronizedSet(new HashSet<String>());
	private final static Map<TrackerPool, String> trackerPools = Collections.synchronizedMap(new IdentityHashMap<TrackerPool, String>());
	private final static Set<String> trackerKeys = Collections.synchronizedSet(new HashSet<String>());

	public static Map<StoragePool, String> getStoragePools() {
		return storagePools;
	}

	public static Map<TrackerPool, String> getTrackerPools() {
		return trackerPools;
	}

	public static Set<String> getStoragekeys() {
		return storageKeys;
	}

	public static Set<String> getTrackerkeys() {
		return trackerKeys;
	}

}
