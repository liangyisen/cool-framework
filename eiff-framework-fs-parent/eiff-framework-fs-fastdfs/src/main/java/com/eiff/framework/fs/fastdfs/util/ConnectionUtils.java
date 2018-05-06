
package com.eiff.framework.fs.fastdfs.util;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import com.eiff.framework.fs.fastdfs.conf.NonGlobalConfig;

public class ConnectionUtils {

	/**
	 * construct Socket object
	 * 
	 * @param ip_addr
	 *            ip address or hostname
	 * @param port
	 *            port number
	 * @return connected Socket object
	 */
	public static Socket getSocket(String ip_addr, int port, NonGlobalConfig nonGlobalConfig) throws IOException {
		Socket sock = new Socket();
		sock.setSoTimeout(nonGlobalConfig.getNetworkTimeout());
		sock.connect(new InetSocketAddress(ip_addr, port), nonGlobalConfig.getConnectionTimeout());
		return sock;
	}

	/**
	 * construct Socket object
	 * 
	 * @param addr
	 *            InetSocketAddress object, including ip address and port
	 * @return connected Socket object
	 */
	public static Socket getSocket(InetSocketAddress addr, NonGlobalConfig nonGlobalConfig) throws IOException {
		Socket sock = new Socket();
		sock.setSoTimeout(nonGlobalConfig.getNetworkTimeout());
		sock.connect(addr, nonGlobalConfig.getConnectionTimeout());
		return sock;
	}
}
