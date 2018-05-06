package com.eiff.framework.rpc.exception.wapper;

import com.alibaba.dubbo.remoting.TimeoutException;
import com.alibaba.dubbo.rpc.RpcException;
import com.eiff.framework.common.biz.code.CommonRspCode;
import com.eiff.framework.common.biz.exception.BaseBusinessException;
import com.eiff.framework.common.biz.exception.baseface.BaseFrameworkException;
import com.eiff.framework.common.biz.pkg.BaseResponse;

public class RpcExceptionWapper {

	public static WapperResault wrapUp(RpcException rpcException, BaseResponse baseResponse) {

		boolean monitored = true;
		boolean loggable = true;

		Throwable rootCause = rpcException.getCause();
		if (rootCause == null) {
			baseResponse.setRespCode(CommonRspCode.RPC_ERROR.getCode());
			baseResponse.setMsg(CommonRspCode.RPC_ERROR.getMessage());
			rootCause = rpcException;
		} else {
			if (rootCause instanceof BaseBusinessException) {

				BaseBusinessException baseBusinessExceptionRootCause = (BaseBusinessException) rootCause;
				monitored = baseBusinessExceptionRootCause.isMonitored();

				if (baseBusinessExceptionRootCause.getInnerException() != null) {
					rootCause = baseBusinessExceptionRootCause.getInnerException();
				} else {
					loggable = monitored || baseBusinessExceptionRootCause.isLoggable();
				}

				baseResponse.setRespCode(baseBusinessExceptionRootCause.getCode());
				baseResponse.setMsg(baseBusinessExceptionRootCause.getMessage());

			} else if (rootCause instanceof BaseFrameworkException) {
				BaseFrameworkException baseFrameworkExceptionRootCause = (BaseFrameworkException) rootCause;
				baseResponse.setRespCode(baseFrameworkExceptionRootCause.getCode());
				baseResponse.setMsg(baseFrameworkExceptionRootCause.getMessage());
			} else if (rootCause instanceof TimeoutException) {
				baseResponse.setRespCode(CommonRspCode.SYS_TIMEOUT.getCode());
				baseResponse.setMsg(CommonRspCode.SYS_TIMEOUT.getMessage());
			} else {
				baseResponse.setRespCode(CommonRspCode.SYS_ERROR.getCode());
				baseResponse.setMsg(CommonRspCode.SYS_ERROR.getMessage());
			}
		}
		WapperResault wapperResault = new WapperResault(rootCause, monitored);
		wapperResault.setLoggable(loggable);

		return wapperResault;
	}
}
