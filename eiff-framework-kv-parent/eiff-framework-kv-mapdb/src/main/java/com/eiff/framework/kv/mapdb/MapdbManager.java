package com.eiff.framework.kv.mapdb;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;

import org.apache.commons.io.FileUtils;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eiff.framework.kv.KVDb;
import com.eiff.framework.kv.KVManager;

public class MapdbManager implements KVManager {

	private static Logger LOGGER = LoggerFactory.getLogger(MapdbManager.class);

	private Collection<String> kvdbNames = new LinkedList<String>();

	@Override
	public KVDb getKVDB(String dbFilePathName, String name) {
		try {
			String dir = dbFilePathName.substring(0, dbFilePathName.lastIndexOf("/"));
			FileUtils.forceMkdir(new File(dir));
			DB db = DBMaker.fileDB(new File(dbFilePathName)).closeOnJvmShutdown().encryptionEnable("password").make();
			kvdbNames.add(name);
			return new Mapdb(db, name);
		} catch (Throwable e) {
			LOGGER.warn("getKVDB error :" + dbFilePathName + "," + e.getMessage(), e);
		}
		return null;
	}

	@Override
	public Collection<String> getKVDBNames() {
		return kvdbNames;
	}

}
