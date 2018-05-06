package com.eiff.framework.common.biz.mobile;

import java.io.Serializable;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

public abstract class FrontRequest implements Serializable {

	private static final long serialVersionUID = -7250473166523181107L;
	
	private String clientId;
    private String appId;
    
    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }
    
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
