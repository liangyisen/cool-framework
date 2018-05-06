package com.eiff.framework.common.biz.mobile;

import java.io.Serializable;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

public abstract class FrontResponse implements Serializable {

	private static final long serialVersionUID = -6080858828270195838L;
	
	private String resultCode;
    private String resultMsg;
    private String traceNo;

    public String getResultCode() {
        return resultCode;
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    public String getResultMsg() {
        return resultMsg;
    }

    public void setResultMsg(String resultMsg) {
        this.resultMsg = resultMsg;
    }

    public String getTraceNo() {
        return traceNo;
    }

    public void setTraceNo(String traceNo) {
		this.traceNo = traceNo;
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

