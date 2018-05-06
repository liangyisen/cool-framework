package com.eiff.framework.fs.fastdfs.conf;

import java.io.IOException;

import com.eiff.framework.fs.fastdfs.TrackerGroup;
import com.eiff.framework.fs.fastdfs.common.IniFileReader;
import com.eiff.framework.fs.fastdfs.common.MyException;
import com.eiff.framework.fs.fastdfs.pool.conf.BasePoolConfig;

public class NonGlobalConfig {

	private int networkTimeout;
	private int connectionTimeout;
	private String[] trackerServers;
	private String charset;
	private int trackerHttpPort;
	private boolean antiStealToken; // if anti-steal token
	private String secretKey; // generage token secret key
	private TrackerGroup trackerGroup;
	private BasePoolConfig trackerPoolConfig;
	private BasePoolConfig storageReadPoolConfig;
	private BasePoolConfig storageWritePoolConfig;
	private BasePoolConfig storageReadWritePoolConfig;

	public static final int DEFAULT_CONNECT_TIMEOUT = 5 * 1000; // ms
	public static final int DEFAULT_NETWORK_TIMEOUT = 30 * 1000; // ms

	public NonGlobalConfig(String confFilename) throws IOException, MyException {
		IniFileReader iniReader;
		String[] szTrackerServers;

		iniReader = new IniFileReader(confFilename);

		connectionTimeout = iniReader.getIntValue("connect_timeout", DEFAULT_CONNECT_TIMEOUT);
		if (connectionTimeout < 0) {
			connectionTimeout = DEFAULT_CONNECT_TIMEOUT;
		}
		connectionTimeout *= 1000; // millisecond

		networkTimeout = iniReader.getIntValue("network_timeout", DEFAULT_NETWORK_TIMEOUT);
		if (networkTimeout < 0) {
			networkTimeout = DEFAULT_NETWORK_TIMEOUT;
		}
		networkTimeout *= 1000; // millisecond

		charset = iniReader.getStrValue("charset");
		if (charset == null || charset.length() == 0) {
			charset = "ISO8859-1";
		}

		szTrackerServers = iniReader.getValues("tracker_server");
		if (szTrackerServers == null) {
			throw new MyException("item \"tracker_server\" in " + confFilename + " not found");
		}

		this.trackerServers = szTrackerServers;
		this.trackerGroup = new TrackerGroup(this);
	}

	public NonGlobalConfig(int connectTimeout, int networkTimeout, String charset, String[] trackerServers,
			int trackerPort, boolean antiStealToken) throws MyException {

		if (connectTimeout < 0) {
			connectTimeout = DEFAULT_CONNECT_TIMEOUT;
		}
		if (networkTimeout < 0) {
			networkTimeout = DEFAULT_NETWORK_TIMEOUT;
		}
		if (charset == null || charset.length() == 0) {
			charset = "UTF-8";
		}
		if (trackerServers == null || trackerServers.length == 0) {
			throw new MyException("trackerServers can not be null");
		}

		this.connectionTimeout = connectTimeout;
		this.networkTimeout = networkTimeout;
		this.charset = charset;
		this.trackerHttpPort = trackerPort;
		this.antiStealToken = antiStealToken;
		this.trackerServers = trackerServers;
		this.trackerGroup = new TrackerGroup(this);
	}

	public int getNetworkTimeout() {
		return networkTimeout;
	}

	public void setNetworkTimeout(int networkTimeout) {
		this.networkTimeout = networkTimeout;
	}

	public String getCharset() {
		return charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

	public int getTrackerHttpPort() {
		return trackerHttpPort;
	}

	public void setTrackerHttpPort(int trackerHttpPort) {
		this.trackerHttpPort = trackerHttpPort;
	}

	public boolean isAntiStealToken() {
		return antiStealToken;
	}

	public void setAntiStealToken(boolean antiStealToken) {
		this.antiStealToken = antiStealToken;
	}

	public String getSecretKey() {
		return secretKey;
	}

	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}

	public TrackerGroup getTrackerGroup() {
		return trackerGroup;
	}

	public void setTrackerGroup(TrackerGroup trackerGroup) {
		this.trackerGroup = trackerGroup;
	}

	public int getConnectionTimeout() {
		return connectionTimeout;
	}

	public void setConnectionTimeout(int connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}

	public String[] getTrackerServers() {
		return trackerServers;
	}

	public void setTrackerServers(String[] trackerServers) {
		this.trackerServers = trackerServers;
	}

	public BasePoolConfig getTrackerPoolConfig() {
		return trackerPoolConfig;
	}

	public void setTrackerPoolConfig(BasePoolConfig trackerPoolConfig) {
		this.trackerPoolConfig = trackerPoolConfig;
	}

	public BasePoolConfig getStorageReadPoolConfig() {
		return storageReadPoolConfig;
	}

	public void setStorageReadPoolConfig(BasePoolConfig storageReadPoolConfig) {
		this.storageReadPoolConfig = storageReadPoolConfig;
	}

	public BasePoolConfig getStorageWritePoolConfig() {
		return storageWritePoolConfig;
	}

	public void setStorageWritePoolConfig(BasePoolConfig storageWritePoolConfig) {
		this.storageWritePoolConfig = storageWritePoolConfig;
	}

	public BasePoolConfig getStorageReadWritePoolConfig() {
		return storageReadWritePoolConfig;
	}

	public void setStorageReadWritePoolConfig(BasePoolConfig storageReadWritePoolConfig) {
		this.storageReadWritePoolConfig = storageReadWritePoolConfig;
	}
}
