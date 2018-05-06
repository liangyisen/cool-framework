package com.eiff.framework.rpc.registry.green;

import java.util.List;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.registry.NotifyListener;
import com.alibaba.dubbo.registry.zookeeper.ZookeeperRegistry;
import com.alibaba.dubbo.remoting.zookeeper.ZookeeperTransporter;

public class GreenRegistry extends ZookeeperRegistry {

	public GreenRegistry(URL url, ZookeeperTransporter zookeeperTransporter) {
		super(url, zookeeperTransporter);
	}

	private URL addGroup(URL url) {
		if (StringUtils.isNotEmpty(url.getServiceInterface())
				&& !url.getServiceInterface().equalsIgnoreCase("com.alibaba.dubbo.monitor.MonitorService")) {
			String side = url.getParameter(Constants.SIDE_KEY);
			if (Constants.PROVIDER_SIDE.equals(side) || Constants.CONSUMER_SIDE.equals(side)) {
				url = url.addParameter(Constants.GROUP_KEY, "green");
			}
		}
		return url;
	}

	@Override
	protected void doRegister(URL url) {
		super.doRegister(url);
	}

	@Override
	protected void doUnregister(URL url) {
		super.doUnregister(url);
	}

	@Override
	protected void doSubscribe(URL url, NotifyListener listener) {
		super.doSubscribe(url, listener);
	}

	@Override
	protected void doUnsubscribe(URL url, NotifyListener listener) {
		super.doUnsubscribe(url, listener);
	}

	@Override
	public List<URL> lookup(URL url) {
		return super.lookup(addGroup(url));
	}

	@Override
	public void register(URL url) {
		super.register(addGroup(url));
	}

	@Override
	public void unregister(URL url) {
		super.unregister(addGroup(url));
	}

	@Override
	public void subscribe(URL url, NotifyListener listener) {
		super.subscribe(addGroup(url), listener);
	}

	@Override
	public void unsubscribe(URL url, NotifyListener listener) {
		super.unsubscribe(addGroup(url), listener);
	}

	@Override
	protected void notify(URL url, NotifyListener listener, List<URL> urls) {
		super.notify(url, listener, urls);
	}

	@Override
	protected void doNotify(URL url, NotifyListener listener, List<URL> urls) {
		super.doNotify(url, listener, urls);
	}

	@Override
	protected void setUrl(URL url) {
		super.setUrl(url);
	}

	@Override
	public List<URL> getCacheUrls(URL url) {
		return super.getCacheUrls(url);
	}

	@Override
	protected void notify(List<URL> urls) {
		super.notify(urls);
	}

}
