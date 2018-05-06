package com.eiff.framework.rpc.registry.green;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.registry.Registry;
import com.alibaba.dubbo.registry.support.AbstractRegistryFactory;
import com.alibaba.dubbo.remoting.zookeeper.ZookeeperTransporter;

public class GreenRegistryFactory extends AbstractRegistryFactory {

	private ZookeeperTransporter zookeeperTransporter;

	public void setZookeeperTransporter(ZookeeperTransporter zookeeperTransporter) {
		this.zookeeperTransporter = zookeeperTransporter;
	}

	public Registry createRegistry(URL url) {
		return new GreenRegistry(url, zookeeperTransporter);
	}
}
