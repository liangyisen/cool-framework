package com.eiff.framework.common.biz.mobile;

import java.io.Serializable;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

public abstract class FrontDTO implements Serializable {

	private static final long serialVersionUID = 752625707505718769L;

	@Override
	public String toString() {
		try {
			return ReflectionToStringBuilder.toString(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return super.toString();
	}
}
