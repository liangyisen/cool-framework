package com.eiff.framework.kv.mapdb;

import java.util.Map;
import java.util.Set;

import org.mapdb.DB;

import com.eiff.framework.kv.KVDb;

public class Mapdb implements KVDb {

	private DB db;

	private Map<Object, Object> map;

	private String name;

	@SuppressWarnings("deprecation")
	public Mapdb(DB db, TypeEnum typeEnum, String name) {
		super();
		this.db = db;
		this.name = name;
		if (TypeEnum.BTreeMap.equals(typeEnum)) {
			this.map = db.getTreeMap(name);
		} else {
			this.map = db.getHashMap(name);
		}
	}

	public Mapdb(DB db, String name) {
		this(db, TypeEnum.HTreeMap, name);
	}

	@Override
	public Object put(Object key, Object value) {
		return map.put(key, value);
	}

	@Override
	public Object get(Object key) {
		return map.get(key);
	}

	@Override
	public Set<Object> keySet() {
		return map.keySet();
	}

	@Override
	public Object remove(Object key) {
		return map.remove(key);
	}

	@Override
	public void close() {
		db.close();
	}

	@Override
	public void commit() {
		db.commit();
	}

	public DB getDb() {
		return db;
	}

	public void setDb(DB db) {
		this.db = db;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
