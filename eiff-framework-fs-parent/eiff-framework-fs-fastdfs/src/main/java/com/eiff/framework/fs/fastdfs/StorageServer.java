
package com.eiff.framework.fs.fastdfs;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.eiff.framework.fs.fastdfs.conf.NonGlobalConfig;
import com.eiff.framework.fs.fastdfs.util.ConnectionUtils;

public class StorageServer extends TrackerServer {
	protected int store_path_index = 0;

	/**
	 * Constructor
	 * 
	 * @param ip_addr
	 *            the ip address of storage server
	 * @param port
	 *            the port of storage server
	 * @param store_path
	 *            the store path index on the storage server
	 */
	public StorageServer(String ip_addr, int port, int store_path, NonGlobalConfig config) throws IOException {
		super(ConnectionUtils.getSocket(ip_addr, port, config), new InetSocketAddress(ip_addr, port), config);
		this.store_path_index = store_path;
	}

	/**
	 * @return the store path index on the storage server
	 */
	public int getStorePathIndex() {
		return this.store_path_index;
	}
}
