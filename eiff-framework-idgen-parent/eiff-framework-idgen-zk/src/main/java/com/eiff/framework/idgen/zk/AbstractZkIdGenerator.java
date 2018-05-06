package com.eiff.framework.idgen.zk;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Iterator;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eiff.framework.idgen.api.AbstractDateIdGenerator;

/**
 * @author tangzhaowei
 */
public abstract class AbstractZkIdGenerator extends AbstractDateIdGenerator {

	private static Logger LOGGER = LoggerFactory.getLogger(AbstractZkIdGenerator.class);

	private final static String NODE_PREFIX = "/seq";

	protected String zkAddress;
	protected String idNodeName;

	public void setZkAddress(String zkAddress) {
		Validate.notBlank(zkAddress);
		this.zkAddress = zkAddress;
	}

	public void setIdNodeName(String idNodeName) {
		Validate.notBlank(idNodeName);
		this.idNodeName = idNodeName;
	}

	protected void getMachineId() {
		CuratorFramework client = CuratorFrameworkFactory.builder().connectString(zkAddress).retryPolicy(new ExponentialBackoffRetry(1000, 3))
				.sessionTimeoutMs(5000).build();
		client.start();

		try {
			String localAddress = getLocalAddress();
			if (StringUtils.isBlank(localAddress)) {
				setMachineId(client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT_SEQUENTIAL).forPath(getRootPath() + NODE_PREFIX));
			} else {
				Stat stat = client.checkExists().forPath(getRootPath());
				if (stat == null) {
					setMachineId(client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT_SEQUENTIAL)
							.forPath(getRootPath() + NODE_PREFIX, localAddress.getBytes("UTF8")));
				} else {
					String nodeName = null;
					Iterator<String> it = client.getChildren().forPath(getRootPath()).iterator();
					while (it.hasNext()) {
						nodeName = it.next();
						if (StringUtils.isBlank(nodeName)) {
							continue;
						}

						byte[] data = client.getData().forPath(getRootPath() + "/" + nodeName);
						if (ArrayUtils.isNotEmpty(data)) {
							String nodeAddress = new String(data, "UTF8");
							if (StringUtils.isNotBlank(nodeAddress) && nodeAddress.equals(localAddress)) {
								setMachineId(nodeName);
								break;
							}
						}
					}

					if (StringUtils.isBlank(machineId)) {
						setMachineId(client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT_SEQUENTIAL)
								.forPath(getRootPath() + NODE_PREFIX, localAddress.getBytes("UTF8")));
					}
				}
			}
		} catch (Throwable e) {
			LOGGER.error("failed to get machineId", e);
		} finally {
			client.close();
		}
	}

	private String getLocalAddress() {
		try {
			return InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			LOGGER.error("failed to get local address", e);
		}
		return null;
	}

	private String getRootPath() {
		return "/eif/" + prefix + "/" + idNodeName;
	}
}
