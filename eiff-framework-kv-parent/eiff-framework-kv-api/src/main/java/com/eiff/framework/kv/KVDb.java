package com.eiff.framework.kv;

import java.util.Set;

public interface KVDb {

	public Object put(Object key, Object value);

	public Object get(Object key);

	public Object remove(Object key);

	public Set<Object> keySet();

	public void close();

	public void commit();

}
