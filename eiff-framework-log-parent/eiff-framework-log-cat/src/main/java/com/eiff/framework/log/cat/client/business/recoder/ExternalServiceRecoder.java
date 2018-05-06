package com.eiff.framework.log.cat.client.business.recoder;

import com.eiff.framework.log.api.Constants;

public class ExternalServiceRecoder extends AbsBusinessRecoder {

	private ExternalServiceRecoder(String rootName) {
		this.rootType = Constants.RECODER_EXTERNALSERVICES_ROOT_TYPE;
		this.rootName = rootName;
	}

	public static ExternalServiceRecoder build(String name) {
		return new ExternalServiceRecoder(name);
	}
}
