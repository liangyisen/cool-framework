package com.eiff.framework.rpc.registry;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.registry.Registry;
import com.alibaba.dubbo.registry.RegistryFactory;
import com.alibaba.dubbo.remoting.zookeeper.ZookeeperTransporter;

@SuppressWarnings("unused")
public class BaseZookeeperRegistryFactory implements RegistryFactory {

	private ZookeeperTransporter zookeeperTransporter;
	private RegistryFactory registryFactory;

	public BaseZookeeperRegistryFactory(RegistryFactory registryFactory) {
		this.registryFactory = registryFactory;
	}

	public void setZookeeperTransporter(ZookeeperTransporter zookeeperTransporter) {
		this.zookeeperTransporter = zookeeperTransporter;
	}

	@Override
	public Registry getRegistry(URL url) {
		return new BaseZookeeperRegistry(registryFactory.getRegistry(url));
	}
}
