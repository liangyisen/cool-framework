
package com.eiff.framework.fs.fastdfs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eiff.framework.fs.fastdfs.conf.NonGlobalConfig;
import com.eiff.framework.fs.fastdfs.util.ConnectionUtils;

@SuppressWarnings("rawtypes")
public class TrackerServer implements PoolableObject {

	private static Logger LOGGER = LoggerFactory.getLogger(PoolableObject.class);

	protected Socket sock;
	private InetSocketAddress inetSockAddr;
	private NonGlobalConfig config;
	private String addrWithPort;
	private GenericKeyedObjectPool dataSource = null;

	/**
	 * Constructor
	 * 
	 * @param sock
	 *            Socket of server
	 * @param inetSockAddr
	 *            the server info
	 */
	public TrackerServer(Socket sock, InetSocketAddress inetSockAddr, NonGlobalConfig config) {
		this.sock = sock;
		this.inetSockAddr = inetSockAddr;
		this.config = config;
		this.addrWithPort = inetSockAddr.getAddress().getHostAddress() + ":" + inetSockAddr.getPort();
	}

	/**
	 * get the connected socket
	 * 
	 * @return the socket
	 */
	public Socket getSocket() throws IOException {
		if (this.sock == null) {
			this.sock = ConnectionUtils.getSocket(this.inetSockAddr, this.config);
		}

		return this.sock;
	}

	/**
	 * get the server info
	 * 
	 * @return the server info
	 */
	public InetSocketAddress getInetSocketAddress() {
		return this.inetSockAddr;
	}

	public OutputStream getOutputStream() throws IOException {
		return this.sock.getOutputStream();
	}

	public InputStream getInputStream() throws IOException {
		return this.sock.getInputStream();
	}

	public void close() throws IOException {
		if (this.sock != null) {
			try {
				ProtoCommon.closeSocket(this.sock);
			} finally {
				this.sock = null;
			}
		}
	}

	protected void finalize() throws Throwable {
		this.close();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void returnObject() {
		if (dataSource != null) {
			try {
				dataSource.returnObject(this.addrWithPort, this);
			} catch (Exception e) {
				LOGGER.error("", e);
			}
		}
	}

	public void setDataSource(GenericKeyedObjectPool dataSource) {
		this.dataSource = dataSource;
	}

	public String getAddrWithPort() {
		return addrWithPort;
	}
}
