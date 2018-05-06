package com.eiff.framework.rpc.controller;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.eiff.framework.rpc.controller.model.LocalProvider;
import com.eiff.framework.rpc.controller.model.LocalProviderDetails;
import com.eiff.framework.rpc.controller.model.ModuleParser;
import com.eiff.framework.rpc.registry.BaseZookeeperRegistry;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

@SuppressWarnings({ "rawtypes", "unchecked" })
@RequestMapping("/rpc")
public class DubboRestfulController {
	private LocalProvider localProvider;

	private int servletPort;
	private String servletContext;

	public DubboRestfulController() {
		System.out.println();
	}

	@RequestMapping(value = "/provider.html", method = RequestMethod.GET, produces = "text/html;charset=UTF-8")
	@ResponseBody
	public String getProviderMethodList() throws IOException, TemplateException {
		initMethodMapper();
		Configuration m_configuration = new Configuration();
		m_configuration.setDefaultEncoding("UTF-8");
		try {
			m_configuration.setClassForTemplateLoading(DubboRestfulController.class, "/framework_template");
		} catch (Exception e) {
		}
		Map<String, LocalProvider> root = new HashMap<>();
		Template t = m_configuration.getTemplate("allInterface.ftl");
		root.put("localProvider", localProvider);
		StringWriter sw = new StringWriter(5000);
		t.process(root, sw);
		return sw.toString();
	}

	@RequestMapping(value = "/consumer/", method = RequestMethod.GET)
	@ResponseBody
	public Map<String, Class> getConsumerMethodList() {
		return BaseZookeeperRegistry.CONSUMER_MAP;
	}

	@RequestMapping(value = "/call/{className}/{methodName}", method = RequestMethod.POST)
	@ResponseBody
	public Object getConsumerMethodList(@PathVariable String className, @PathVariable String methodName,
			@RequestBody List<Object> inputParams) throws IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, JsonParseException, JsonMappingException, IOException {
		initMethodMapper();

		Class referClass = BaseZookeeperRegistry.PROVIDER_MAP.get(className);
		Object caller = BaseZookeeperRegistry.getCaller(referClass);
		if (referClass == null || caller == null) {
			throw new NullPointerException("no such method please check the class name");
		}
		LocalProviderDetails localProviderDetails = localProvider.getLocalProviderDetailsMap().get(className);
		Method method = localProviderDetails.getRestfullToMethod().get(methodName);
		Class<?>[] parameterTypes = method.getParameterTypes();
		if (parameterTypes == null || parameterTypes.length == 0) {
			return method.invoke(caller);
		} else {
			Object[] objs = new Object[parameterTypes.length];
			ModuleParser moduleParser = new ModuleParser();
			for (int i = 0; i < parameterTypes.length; i++) {
				Type actualType = method.getGenericParameterTypes()[i];

				if (parameterTypes[i] != null) {
					if (inputParams.get(i) instanceof List) {
						objs[i] = moduleParser.handleJSONArray((List) inputParams.get(i),
								moduleParser.getActualClass(actualType));
					} else if (inputParams.get(i) instanceof Map && !Map.class.getName().equals(parameterTypes[i])) {
						JSONObject jObj = new JSONObject((Map) inputParams.get(i));
						objs[i] = JSON.parseObject(jObj.toJSONString(), parameterTypes[i]);
					} else if (inputParams.get(i) instanceof Map && Map.class.getName().equals(parameterTypes[i])) {
						// TODO?
					} else {
						Object readValue = new ObjectMapper().readValue("" + inputParams.get(i), parameterTypes[i]);
						objs[i] = readValue;
					}
				}
			}
			return method.invoke(caller, objs);
		}
	}

	@SuppressWarnings("unused")
	@RequestMapping(value = "/get/{className}/{methodName}", method = RequestMethod.GET, produces = "text/html;charset=UTF-8")
	@ResponseBody
	public String getMethodInfo(@PathVariable String className, @PathVariable String methodName)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, JsonParseException,
			JsonMappingException, IOException {

		Class referClass = BaseZookeeperRegistry.PROVIDER_MAP.get(className);
		LocalProviderDetails localProviderDetails = localProvider.getLocalProviderDetailsMap().get(className);
		Method method = localProviderDetails.getRestfullToMethod().get(methodName);
		Class<?>[] parameterTypes = method.getParameterTypes();
		Class<?> returnType = method.getReturnType();
		ModuleParser moduleParser = new ModuleParser();
		Object returnClass = moduleParser.constructTheClass(returnType);
		String returnShouldBe = "";
		if (returnClass != null) {
			returnClass = moduleParser.initObj(returnClass);
			returnShouldBe = JSON.toJSONString(returnClass, true);
		}
		String requestShouldBe = "";
		requestShouldBe += "[";
		for (int i = 0; i < parameterTypes.length; i++) {
			Object prarm = moduleParser.initMethodParam(method, i);
			requestShouldBe += JSON.toJSONString(prarm, true);
			if (i != parameterTypes.length - 1) {
				requestShouldBe += ",";
			}
		}
		requestShouldBe += "]";
		String[] details = new String[] { requestShouldBe, returnShouldBe };
		Configuration m_configuration = new Configuration();
		m_configuration.setDefaultEncoding("UTF-8");
		try {
			m_configuration.setClassForTemplateLoading(DubboRestfulController.class, "/framework_template");
		} catch (Exception e) {
		}
		Map<String, String[]> root = new HashMap<>();
		Template t = m_configuration.getTemplate("methodDetails.ftl");
		root.put("details", details);
		StringWriter sw = new StringWriter(5000);
		try {
			t.process(root, sw);
		} catch (TemplateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return sw.toString();
	}

	private void initMethodMapper() {
		if (localProvider == null) {
			try {
				HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
						.getRequest();
				servletPort = request.getLocalPort();
				servletContext = request.getContextPath();
			} catch (Exception e) {
				servletContext = LocalProvider.UNKNOWN;
			}
			localProvider = new LocalProvider();
			Set<Entry<String, Class>> entrySet = BaseZookeeperRegistry.PROVIDER_MAP.entrySet();
			for (Entry<String, Class> entry : entrySet) {
				localProvider.addClass(entry.getValue(), servletPort == 0 ? LocalProvider.UNKNOWN : "" + servletPort,
						servletContext);
			}
		}
	}
}
