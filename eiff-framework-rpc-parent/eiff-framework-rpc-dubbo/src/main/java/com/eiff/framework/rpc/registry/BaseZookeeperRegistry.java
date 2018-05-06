package com.eiff.framework.rpc.registry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ReferenceConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.alibaba.dubbo.registry.NotifyListener;
import com.alibaba.dubbo.registry.Registry;

@SuppressWarnings("rawtypes")
public class BaseZookeeperRegistry implements Registry {

	private static final Logger logger = LoggerFactory.getLogger(BaseZookeeperRegistry.class);
	private static Map<String, ReferenceConfig> localCallerReferMap = new HashMap<>();

	public static Map<String, Class> PROVIDER_MAP = new HashMap<>();
	public static Map<String, Class> CONSUMER_MAP = new HashMap<>();
	private String protocol;
	private String zkAddr;
	private Registry originRegistry;

	public BaseZookeeperRegistry(Registry originRegistry) {
		logger.warn("use BaseZookeeperRegistry");
		this.originRegistry = originRegistry;
	}

	@Override
	public URL getUrl() {
		return originRegistry.getUrl();
	}

	@Override
	public boolean isAvailable() {
		return originRegistry.isAvailable();
	}

	@Override
	public void destroy() {
		originRegistry.destroy();
	}

	@Override
	public void register(URL url) {
		logger.info("do base zk regist");
		String side = url.getParameter(Constants.SIDE_KEY);
		boolean isProvider = false;
		if (Constants.PROVIDER_SIDE.equals(side)) {
			isProvider = true;
			if (StringUtils.isEmpty(url.getParameter(Constants.RETRIES_KEY))) {
				url = url.addParameter(Constants.RETRIES_KEY, 0);
			}
		}
		originRegistry.register(url);
		String className = url.getPath();
		if (className.startsWith("com.alibaba"))
			return;
		String protocal = url.getProtocol();
		try {
			if (protocal.equals(Constants.CONSUMER)) {
				CONSUMER_MAP.put(className, Class.forName(className));
			} else if (isProvider) {
				PROVIDER_MAP.put(className, Class.forName(className));
				String localCallerName = getCreateLocalCallerName(className);

				if (localCallerReferMap.get(localCallerName) == null
						&& Boolean.valueOf(System.getProperty("export_restful"))) {
					ApplicationConfig application = new ApplicationConfig();
					application.setName(localCallerName);
					RegistryConfig registry = new RegistryConfig();
					registry.setAddress(this.protocol + "://" + this.zkAddr);
					registry.setProtocol(this.protocol);
					ReferenceConfig reference = new ReferenceConfig();
					reference.setApplication(application);
					reference.setRetries(url.getParameter(Constants.RETRIES_KEY, 0));
					reference.setGroup(url.getParameter(Constants.GROUP_KEY, ""));
					reference.setVersion(url.getParameter(Constants.VERSION_KEY, ""));
					reference.setTimeout(Integer.valueOf(url.getParameter(Constants.TIMEOUT_KEY, "3000")));
					reference.setStub(Boolean.valueOf(url.getParameter(Constants.STUB_KEY, "false")));
					reference.setRegistry(registry);
					reference.setInterface(className);
					reference.setUrl("dubbo://" + url.getAddress());

					localCallerReferMap.put(localCallerName, reference);
				}
			}
		} catch (ClassNotFoundException e) {
			logger.error("", e);
		}
		logger.info("do base zk regist done");
	}

	@Override
	public void unregister(URL url) {
		originRegistry.unregister(url);
	}

	@Override
	public void subscribe(URL url, NotifyListener listener) {
		originRegistry.subscribe(url, listener);
	}

	@Override
	public void unsubscribe(URL url, NotifyListener listener) {
		originRegistry.unsubscribe(url, listener);
	}

	@Override
	public List<URL> lookup(URL url) {
		return originRegistry.lookup(url);
	}

	private static String getCreateLocalCallerName(String className) {
		return className + ".local.consumer";
	}

	public static <T> T getCaller(Class<T> refer) {
		if (refer == null)
			return null;
		@SuppressWarnings("unchecked")
		ReferenceConfig<T> referenceConfig = localCallerReferMap.get(getCreateLocalCallerName(refer.getName()));
		return referenceConfig.get();
	}
}
