package com.eiff.framework.common.biz.pkg;

import java.io.Serializable;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class BaseRequest implements Serializable {

	private static final long serialVersionUID = 1L;

	@Override
	public String toString() {
		try {
			return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return super.toString();
	}

}
