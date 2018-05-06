package com.eiff.framework.rpc.controller.model;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("rawtypes")
public class LocalProvider {
	public static final String UNKNOWN = "unknown";

	private List<String> classNames = new ArrayList<>();
	private Map<String, LocalProviderDetails> localProviderDetailsMap = new HashMap<>();
	private String servletPort;
	private String localIP;
	private String servletContext;

	public void addClass(Class cls, String servletPort, String servletContext) {
		classNames.add(cls.getName());
		this.servletPort = servletPort;
		this.servletContext = servletContext;
		try {
			localIP = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			localIP = UNKNOWN;
		}
		localProviderDetailsMap.put(cls.getName(), new LocalProviderDetails(cls, this));
	}

	public List<String> getClassNames() {
		return classNames;
	}

	public Map<String, LocalProviderDetails> getLocalProviderDetailsMap() {
		return localProviderDetailsMap;
	}

	public String getServletPort() {
		return servletPort;
	}

	public String getLocalIP() {
		return localIP;
	}

	public String getServletContext() {
		return servletContext;
	}
}
