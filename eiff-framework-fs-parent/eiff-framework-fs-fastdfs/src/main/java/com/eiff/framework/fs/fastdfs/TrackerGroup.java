
package com.eiff.framework.fs.fastdfs;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eiff.framework.fs.fastdfs.common.MyException;
import com.eiff.framework.fs.fastdfs.conf.NonGlobalConfig;

public class TrackerGroup {
	private static Logger LOGGER = LoggerFactory.getLogger(TrackerGroup.class);
	protected Integer lock;
	public int tracker_server_index;
	public InetSocketAddress[] tracker_servers;
	private NonGlobalConfig config;

	/**
	 * Constructor
	 * 
	 * @param tracker_servers
	 *            tracker servers
	 * @throws MyException
	 */
	public TrackerGroup(NonGlobalConfig config) throws MyException {
		this.config = config;
		this.lock = new Integer(0);
		this.tracker_server_index = 0;
		String[] parts;
		int trackerServerCount = config.getTrackerServers().length;
		InetSocketAddress[] tracker_servers = new InetSocketAddress[trackerServerCount];
		for (int i = 0; i < trackerServerCount; i++) {
			parts = config.getTrackerServers()[i].split("\\:", 2);
			if (parts.length != 2) {
				throw new MyException(
						"the value of item \"tracker_server\" is invalid, the correct format is host:port");
			}

			tracker_servers[i] = new InetSocketAddress(parts[0].trim(), Integer.parseInt(parts[1].trim()));
		}
		this.tracker_servers = tracker_servers;
	}

	/**
	 * return connected tracker server
	 * 
	 * @return connected tracker server, null for fail
	 */
	public TrackerServer getConnection(int serverIndex) throws IOException {
		Socket sock = new Socket();
		sock.setReuseAddress(true);
		sock.setSoTimeout(this.config.getNetworkTimeout());
		sock.connect(this.tracker_servers[serverIndex], this.config.getConnectionTimeout());
		return new TrackerServer(sock, this.tracker_servers[serverIndex], this.config);
	}

	/**
	 * return connected tracker server
	 * 
	 * @return connected tracker server, null for fail
	 */
	public TrackerServer getConnection() throws IOException {
		int current_index;

		synchronized (this.lock) {
			this.tracker_server_index++;
			if (this.tracker_server_index >= this.tracker_servers.length) {
				this.tracker_server_index = 0;
			}

			current_index = this.tracker_server_index;
		}

		try {
			return this.getConnection(current_index);
		} catch (IOException ex) {
			System.err.println("connect to server " + this.tracker_servers[current_index].getAddress().getHostAddress()
					+ ":" + this.tracker_servers[current_index].getPort() + " fail");
			ex.printStackTrace(System.err);
		}

		for (int i = 0; i < this.tracker_servers.length; i++) {
			if (i == current_index) {
				continue;
			}

			try {
				TrackerServer trackerServer = this.getConnection(i);

				synchronized (this.lock) {
					if (this.tracker_server_index == current_index) {
						this.tracker_server_index = i;
					}
				}

				return trackerServer;
			} catch (IOException ex) {
				System.err.println("connect to server " + this.tracker_servers[i].getAddress().getHostAddress() + ":"
						+ this.tracker_servers[i].getPort() + " fail");
				ex.printStackTrace(System.err);
			}
		}

		return null;
	}

	public Object clone() {
		try {
			return new TrackerGroup(this.config);
		} catch (MyException e) {
			LOGGER.error("", e);
		}
		return null;
	}

	public NonGlobalConfig getConfig() {
		return this.config;
	}
}
