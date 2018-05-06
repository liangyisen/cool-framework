package com.eiff.framework.common.biz.pkg;

import java.io.Serializable;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.eiff.framework.common.biz.code.CommonRspCode;

public class BaseResponse implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String respCode;
	private String msg;

	public String getMsg() {
		return this.msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public String getRespCode() {
		return respCode;
	}

	public void setRespCode(String respCode) {
		this.respCode = respCode;
	}

	@Override
	public String toString() {
		try {
			return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return super.toString();
	}

	public boolean isSuccess() {
		return CommonRspCode.SUCCESS.getCode().equals(respCode);
	}

}
