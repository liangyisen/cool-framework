package com.eiff.framework.fs.fastdfs.common;

import java.util.ArrayList;
import java.util.List;

import com.eiff.framework.log.api.HdLogger;

public class BaseTool {
	private final static HdLogger LOGGER = HdLogger.getLogger(BaseTool.class);

	public static NameValuePair[] creatNameValuePairs(String fileName, String fileExtName, String sysId) {
		List<NameValuePair> list = new ArrayList<>();
		list.add(new NameValuePair("fileName", fileName));
		list.add(new NameValuePair("fileExtName", fileExtName));
		list.add(new NameValuePair("sysId", sysId));
		list.add(new NameValuePair("module", LOGGER.buildTracer().getDomainName()));
		list.add(new NameValuePair(NotOverridableException.OVERRIDABLEFLAG, "true"));
		NameValuePair[] returnArray = new NameValuePair[list.size()];
		for (int i = 0; i < returnArray.length; i++) {
			returnArray[i] = list.get(i);
		}
		return returnArray;
	}
}
