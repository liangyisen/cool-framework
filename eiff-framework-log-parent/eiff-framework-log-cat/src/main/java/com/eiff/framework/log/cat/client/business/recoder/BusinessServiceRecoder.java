package com.eiff.framework.log.cat.client.business.recoder;

import com.eiff.framework.log.api.Constants;

public class BusinessServiceRecoder extends AbsBusinessRecoder {

	private BusinessServiceRecoder(String rootName) {
		this.rootType = Constants.RECODER_BUSINESSSERVICES_ROOT_TYPE;
		this.rootName = rootName;
	}

	public static BusinessServiceRecoder build(String name) {
		return new BusinessServiceRecoder(name);
	}
}
