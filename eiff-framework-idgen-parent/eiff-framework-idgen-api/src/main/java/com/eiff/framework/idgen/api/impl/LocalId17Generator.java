package com.eiff.framework.idgen.api.impl;

import com.eiff.framework.idgen.api.AbstractDateIdGenerator;
import org.apache.commons.lang3.StringUtils;

/**
 * @author tangzhaowei
 */
public class LocalId17Generator extends AbstractDateIdGenerator {

	public LocalId17Generator(String prefix, int preFetchCount, String dateFormat) {
		this.prefix = prefix;
		this.preFetchCount = preFetchCount;
		this.dateFormat = dateFormat;

		if (StringUtils.isBlank(dateFormat)) {
			setDefaultDateFormat();
		}
		fillingIds();
	}

	@Override
	protected void setDefaultDateFormat() {
		dateFormat = "yyMMddHHmmss";
	}
}
