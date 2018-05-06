package com.eiff.framework.rpc.controller.model;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

@SuppressWarnings("rawtypes")
public class LocalProviderDetails {

	private Map<String, Method> restfullToMethod = new HashMap<>();
	private Map<String, String[]> restfullToMethodDesc = new HashMap<>();

	private LocalProvider localProvider;
	private String className;

	public LocalProviderDetails(Class cls, LocalProvider localProvider) {
		this.localProvider = localProvider;
		this.className = cls.getName();
		Method[] declaredMethods = cls.getDeclaredMethods();
		for (Method method : declaredMethods) {
			String methodName = method.getName();

			if (restfullToMethod.get(methodName) != null) {
				methodName = methodName + "_" + getParameters(method);
			}
			restfullToMethod.put(methodName, method);
			makeDescription(methodName, method);
		}
	}

	private void makeDescription(String methodName, Method method) {
		String url = "";
		if (StringUtils.isEmpty(localProvider.getServletContext())) {
			url = "http://" + this.localProvider.getLocalIP() + ":" + this.localProvider.getServletPort() + "/rpc/call/"
					+ this.className + "/" + methodName;
		} else {
			url = "http://" + this.localProvider.getLocalIP() + ":" + this.localProvider.getServletPort() + "/"
					+ localProvider.getServletContext() + "/rpc/call/" + this.className + "/" + methodName;
		}

		String[] methodDesc = new String[4];
		methodDesc[0] = method.getName();
		methodDesc[1] = getParameters(method);
		methodDesc[2] = url;
		String methodDetail = methodDesc[0] + "(";
		Type[] paramTypes = method.getGenericParameterTypes();
		if (paramTypes != null && paramTypes.length > 0) {
			for (Type type : paramTypes) {
				String typeString = type.toString();
				if (typeString.startsWith("class ")) {
					typeString = typeString.replace("class ", "");
				}
				methodDetail += typeString + ",";
			}
			int lastIndexOf = methodDetail.lastIndexOf(",");
			methodDetail = methodDetail.substring(0, lastIndexOf);
		}
		methodDetail = methodDetail + ")";
		methodDesc[3] = methodDetail;

		restfullToMethodDesc.put(methodName, methodDesc);
	}

	private String getParameters(Method method) {
		String additionalName = "";
		Class<?>[] parameterTypes = method.getParameterTypes();
		if (parameterTypes == null || parameterTypes.length == 0) {
			additionalName = "NA";
		} else {
			for (Class<?> class1 : parameterTypes) {
				additionalName = additionalName + "_" + class1.getSimpleName();
			}
		}
		return additionalName;
	}

	public Map<String, Method> getRestfullToMethod() {
		return restfullToMethod;
	}

	public Map<String, String[]> getRestfullToMethodDesc() {
		return restfullToMethodDesc;
	}
}
