package com.eiff.framework.log.api.biz;

import com.eiff.framework.log.api.EventPair;

public interface BusinessRecoder {

	public String getRootType();

	public String getRootName();

	public BusinessRecoder logSuccess(EventPair eventPair);

	public BusinessRecoder logFailed(EventPair eventPair);

	public void submit();
}
