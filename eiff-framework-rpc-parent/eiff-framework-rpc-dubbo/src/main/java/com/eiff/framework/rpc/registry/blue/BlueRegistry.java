package com.eiff.framework.rpc.registry.blue;

import java.util.List;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.registry.NotifyListener;
import com.alibaba.dubbo.registry.zookeeper.ZookeeperRegistry;
import com.alibaba.dubbo.remoting.zookeeper.ZookeeperTransporter;

public class BlueRegistry extends ZookeeperRegistry {

	public BlueRegistry(URL url, ZookeeperTransporter zookeeperTransporter) {
		super(url, zookeeperTransporter);
	}

	private URL addGroup(URL url) {
		String side = url.getParameter(Constants.SIDE_KEY);
		if (Constants.PROVIDER_SIDE.equals(side) || Constants.CONSUMER_SIDE.equals(side)) {
			url = url.addParameter(Constants.GROUP_KEY, "blue");
		}
		return url;
	}

	@Override
	protected void doRegister(URL url) {
		super.doRegister(addGroup(url));
	}

	@Override
	protected void doUnregister(URL url) {
		super.doUnregister(addGroup(url));
	}

	@Override
	protected void doSubscribe(URL url, NotifyListener listener) {
		super.doSubscribe(addGroup(url), listener);
	}

	@Override
	protected void doUnsubscribe(URL url, NotifyListener listener) {
		super.doUnsubscribe(addGroup(url), listener);
	}

	@Override
	public List<URL> lookup(URL url) {
		return super.lookup(addGroup(url));
	}
}
