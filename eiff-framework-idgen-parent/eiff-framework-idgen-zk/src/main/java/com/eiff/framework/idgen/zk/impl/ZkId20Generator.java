package com.eiff.framework.idgen.zk.impl;

import com.eiff.framework.idgen.zk.AbstractZkIdGenerator;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

/**
 * @author tangzhaowei
 */
public class ZkId20Generator extends AbstractZkIdGenerator {

	public ZkId20Generator(String prefix, int preFetchCount, String dateFormat, String idNodeName, String zkAddress) {
		Validate.notBlank(zkAddress);
		Validate.notBlank(idNodeName);

		this.prefix = prefix;
		this.preFetchCount = preFetchCount;
		this.dateFormat = dateFormat;
		this.idNodeName = idNodeName;
		this.zkAddress = zkAddress;

		if (StringUtils.isBlank(dateFormat)) {
			setDefaultDateFormat();
		}
		fillingIds();

		getMachineId();
	}

	@Override
	protected void setDefaultDateFormat() {
		dateFormat = "yyMMddHHmmssSSS";
	}
}
