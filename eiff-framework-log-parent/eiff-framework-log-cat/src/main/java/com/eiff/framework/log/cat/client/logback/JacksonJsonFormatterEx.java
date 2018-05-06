package com.eiff.framework.log.cat.client.logback;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

import com.eiff.framework.log.api.Constants;

import ch.qos.logback.contrib.jackson.JacksonJsonFormatter;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class JacksonJsonFormatterEx extends JacksonJsonFormatter {

	private String module;
	private static final String PROPERTIES_PROPERTIES = "/META-INF/app.properties";

	public JacksonJsonFormatterEx() {
		if (module == null) {
			module = loadProjectName();
		}
	}

	@Override
	public String toJsonString(Map m) throws IOException {
		try {
			// m.put("traceId", CatUtils.getTraceId());
			Object mdcMap = m.get("mdc");
			if (mdcMap != null && mdcMap instanceof Map) {
				Map mdc = (Map) mdcMap;
				String traceId = (String) mdc.get(Constants.TRACE_ID_PREFIX);
				String parentId = (String) mdc.get(Constants.PARENT_ID_PREFIX);
				String currentId = (String) mdc.get(Constants.CURRENT_ID_PREFIX);
				if (StringUtils.isBlank(traceId)) {
					traceId = Constants.EMPTY_TRACE_ID;
					;
				}
				m.remove("mdc");
				m.put("traceId", traceId);
				if (StringUtils.isNotBlank(parentId))
					m.put("parent", parentId);
				if (StringUtils.isNotBlank(currentId))
					m.put("current", currentId);
			} else {
				m.put("traceId", Constants.EMPTY_TRACE_ID);
			}

			String value = (String) m.get("exception");
			if (StringUtils.isNotBlank(value)) {
				// value = trim(value);

				if (value.length() > 3072) {
					// TODO Anders 性能问题
					value = value.substring(0, 3072);
					m.put("exception", value);
				}
			}
			m.put("module", module);
			// value = (String) m.get("message");
			// if (StringUtils.isNotBlank(value)) {
			// value = trim(value);
			// m.put("message", value);
			// }
		} catch (Throwable e) {
			e.printStackTrace();
		}

		return super.toJsonString(m);
	}

	private String loadProjectName() {
		String appName = null;
		InputStream in = null;
		try {
			in = Thread.currentThread().getContextClassLoader().getResourceAsStream(PROPERTIES_PROPERTIES);

			if (in == null) {
				in = JacksonJsonFormatterEx.class.getResourceAsStream(PROPERTIES_PROPERTIES);
			}
			if (in != null) {
				Properties prop = new Properties();
				prop.load(in);
				appName = prop.getProperty("app.name");
				if (appName == null) {
					appName = "UNSET";
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (Exception e) {
				}
			}
		}
		return appName;
	}

}
