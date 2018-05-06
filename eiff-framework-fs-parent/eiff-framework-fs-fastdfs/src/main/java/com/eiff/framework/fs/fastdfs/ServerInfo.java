
package com.eiff.framework.fs.fastdfs;

/**
 * Server Info
 * 
 * @author Happy Fish / YuQing
 * @version Version 1.7
 */
public class ServerInfo {
	private String ipAddr;
	private int port;
	private byte storePath;

	/**
	 * Constructor
	 * 
	 * @param ipAddr
	 *            address of the server
	 * @param port
	 *            the port of the server
	 */
	public ServerInfo(String ipAddr, int port, byte storePath) {
		this.ipAddr = ipAddr;
		this.port = port;
		this.storePath = storePath;
	}

	public ServerInfo(String ipAddr, int port) {
		this.ipAddr = ipAddr;
		this.port = port;
		this.storePath = 0;
	}

	/**
	 * return the ip address
	 * 
	 * @return the ip address
	 */
	public String getIpAddr() {
		return this.ipAddr;
	}

	/**
	 * return the port of the server
	 * 
	 * @return the port of the server
	 */
	public int getPort() {
		return this.port;
	}

	public byte getStorePath() {
		return storePath;
	}

	public void setStorePath(byte storePath) {
		this.storePath = storePath;
	}
}
