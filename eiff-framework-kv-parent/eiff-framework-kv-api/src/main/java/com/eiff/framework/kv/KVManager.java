package com.eiff.framework.kv;

import java.util.Collection;

public interface KVManager {

	/**
	 * GET KVDB instance
	 * 
	 * @param name
	 * @return
	 */
	KVDb getKVDB(String dbFilePathName, String name);

	/**
	 * get kvdb names
	 * 
	 * @return
	 */
	Collection<String> getKVDBNames();

}
