package com.eiff.framework.rpc.filter;

import java.util.List;

import org.apache.commons.lang.ArrayUtils;

import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.remoting.TimeoutException;
import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.RpcResult;
import com.eiff.framework.common.biz.code.CommonRspCode;
import com.eiff.framework.common.biz.exception.BaseBusinessException;
import com.eiff.framework.common.biz.exception.BaseFrameworkCheckedException;
import com.eiff.framework.common.biz.exception.BaseFrameworkRuntimeException;
import com.eiff.framework.common.biz.exception.baseface.BaseFrameworkException;
import com.eiff.framework.common.biz.pkg.BaseResponse;
import com.eiff.framework.log.api.Constants;
import com.eiff.framework.log.api.HdLogger;
import com.eiff.framework.log.api.trace.Span;
import com.eiff.framework.log.api.trace.Tracer;
import com.eiff.framework.rpc.exception.wapper.RpcExceptionWapper;
import com.eiff.framework.rpc.exception.wapper.WapperResault;
import com.eiff.framework.rpc.trace.DubboTraceLinker;
import com.google.common.collect.Lists;

@Activate(group = { com.alibaba.dubbo.common.Constants.PROVIDER })
public class DubboProviderFilter implements Filter, Constants {

	private static HdLogger LOGGER = HdLogger.getLogger(DubboProviderFilter.class);

	public Result invoke(Invoker<?> invoker, final Invocation invocation) throws RpcException {
		Tracer buildTracer = LOGGER.buildTracer();
		Span span = buildTracer.createEmpty();
		String methodName = null;
		String consumerIp = null;
		try {
			methodName = invoker.getInterface().getName() + "." + invocation.getMethodName();
			consumerIp = RpcContext.getContext().getRemoteHost() + ":" + RpcContext.getContext().getRemotePort();

			buildTracer.create(new DubboTraceLinker(invocation)).receive();

			span = buildTracer.createSpan(TRANS_TYPE_RPC_PROVIDER, methodName);
			LOGGER.logTraceInfoTMDC();
			LOGGER.info(LOG_DUBBO_PROVIDER_IN_MSG, methodName, consumerIp);
			span.addEvent(TRANS_TYPE_RPC_PROVIDER + ".client", consumerIp);

			Object[] args = invocation.getArguments();
			if (ArrayUtils.isNotEmpty(args)) {
				List<String> argsList = Lists.newArrayList();
				for (Object arg : args) {
					argsList.add(String.valueOf(arg));
				}
				LOGGER.info(LOG_DUBBO_PROVIDER_REQUEST_MSG, methodName, consumerIp, argsList);
			}
		} catch (Throwable e) {
			LOGGER.error(LOG_FAILED_TO_CREATE_TRANS, e);
		}

		Result result = null;
		try {
			result = invoker.invoke(invocation);

			if (result != null && result instanceof RpcResult) {
				RpcResult rpcResult = (RpcResult) result;
				Throwable throwable = rpcResult.getException();
				Object resultObject = rpcResult.getValue();
				if (resultObject == null) {
					Class<?> returnType = invoker.getInterface()
							.getMethod(invocation.getMethodName(), invocation.getParameterTypes()).getReturnType();
					if (BaseResponse.class.isAssignableFrom(returnType)) {
						resultObject = (BaseResponse) returnType.newInstance();
						rpcResult.setValue(resultObject);
					} else {
						span.success();
						return result;
					}
				}
				boolean monitored = true;
				boolean loggable = true;

				if (resultObject instanceof BaseResponse) {
					BaseResponse baseResponse = (BaseResponse) resultObject;
					if (throwable != null) {
						if (throwable instanceof RpcException) {
							WapperResault wrapUp = RpcExceptionWapper.wrapUp((RpcException) throwable, baseResponse);
							throwable = wrapUp.getThrowable();
							monitored = wrapUp.isMonitored();
							loggable = wrapUp.isLoggable();
						} else {
							// TODO throw npe会到这里，可以修改下面的日志信息
							LOGGER.error(" did not use exceptionmapper!" + throwable.getClass());
							if (throwable instanceof BaseBusinessException) {
								BaseBusinessException baseBusinessException = (BaseBusinessException) throwable;
								baseResponse.setRespCode(baseBusinessException.getCode());
								baseResponse.setMsg(throwable.getMessage());
							} else if (throwable instanceof BaseFrameworkException) {
								BaseFrameworkException baseFrameworkExceptionRootCause = (BaseFrameworkException) throwable;
								baseResponse.setRespCode(baseFrameworkExceptionRootCause.getCode());
								baseResponse.setMsg(baseFrameworkExceptionRootCause.getMessage());
							} else {
								baseResponse.setRespCode(CommonRspCode.SYS_ERROR.getCode());
								baseResponse.setMsg(CommonRspCode.SYS_ERROR.getMessage());
							}
						}
						if (loggable) {
							LOGGER.error(LOG_DUBBO_PROVIDER_EX_MSG, methodName, consumerIp, throwable);
						} else {
							LOGGER.warn(LOG_DUBBO_PROVIDER_EX_MSG, methodName, consumerIp);
						}
						rpcResult.setException(null);
					}

					LOGGER.info(LOG_DUBBO_PROVIDER_RESPONSE_MSG, methodName, consumerIp, baseResponse.toString());
					span.addEvent(EVENT_TYPE_RPCRETURN, methodName + baseResponse.getRespCode());
				} else if (resultObject instanceof String || resultObject instanceof Integer
						|| resultObject instanceof Long || resultObject instanceof Double
						|| resultObject instanceof Float || resultObject instanceof Character
						|| resultObject instanceof Boolean) {
					LOGGER.info(LOG_DUBBO_PROVIDER_RESPONSE_MSG, methodName, consumerIp, String.valueOf(resultObject));
				} else {
					LOGGER.info("notimplbaseresponse  {} CON_HOST {} RSP {}", methodName, consumerIp,
							String.valueOf(resultObject));
				}

				if (throwable == null) {
					span.success();
				} else {
					if (monitored) {
						span.failed(throwable);
						if (throwable instanceof BaseBusinessException) {
							String exceptionName = throwable.getClass().getSimpleName() + "-"
									+ ((BaseBusinessException) throwable).getCode();
							span.addEvent(EVENT_TYPE_EXCEPTION, exceptionName, EVENT_TYPE_EXCEPTION);
						} // TODO
					} else {
						if (throwable instanceof BaseBusinessException) {
							String exceptionName = throwable.getClass().getSimpleName() + "-"
									+ ((BaseBusinessException) throwable).getCode();
							span.addEvent(EVENT_TYPE_EXCEPTION, exceptionName, EVENT_TYPE_EXCEPTION);
						} else {
							String exceptionName = throwable.getClass().getSimpleName();
							span.addEvent(EVENT_TYPE_EXCEPTION, exceptionName, exceptionName);
						}
					}
				}
				return result;
			}

			if (result == null) {
				throw new RpcException("dubbo result is null");
			}

			span.success();

			return result;
		} catch (Throwable e) {

			try {
				RpcResult rpcResult = new RpcResult();

				Class<?> returnType = invoker.getInterface()
						.getMethod(invocation.getMethodName(), invocation.getParameterTypes()).getReturnType();
				if (BaseResponse.class.isAssignableFrom(returnType)) {
					BaseResponse baseResponse = (BaseResponse) returnType.newInstance();
					if (e instanceof BaseBusinessException) {
						baseResponse.setRespCode(((BaseBusinessException) e).getCode());
						baseResponse.setMsg(e.getMessage());
					} else if (e instanceof TimeoutException) {
						baseResponse.setRespCode(CommonRspCode.SYS_TIMEOUT.getCode());
						baseResponse.setMsg(CommonRspCode.SYS_TIMEOUT.getMessage());
					} else if (e instanceof RpcException) {
						if (e.getCause() != null && e.getCause() instanceof TimeoutException) {
							e = e.getCause();
							baseResponse.setRespCode(CommonRspCode.SYS_TIMEOUT.getCode());
							baseResponse.setMsg(CommonRspCode.SYS_TIMEOUT.getMessage());
						} else {
							if (e.getCause() != null) {
								e = e.getCause();
							}
							LOGGER.error(" did not use exceptionmapper and not try catched!" + e.getClass());
							if (e instanceof BaseFrameworkRuntimeException
									|| e instanceof BaseFrameworkCheckedException) {
								BaseFrameworkException baseFrameworkExceptionRootCause = (BaseFrameworkException) e;
								baseResponse.setRespCode(baseFrameworkExceptionRootCause.getCode());
								baseResponse.setMsg(baseFrameworkExceptionRootCause.getMessage());
							} else {
								baseResponse.setRespCode(CommonRspCode.RPC_ERROR.getCode());
								baseResponse.setMsg(CommonRspCode.RPC_ERROR.getMessage());
							}
						}
					} else if (e instanceof BaseFrameworkRuntimeException
							|| e instanceof BaseFrameworkCheckedException) {
						BaseFrameworkException baseFrameworkExceptionRootCause = (BaseFrameworkException) e;
						baseResponse.setRespCode(baseFrameworkExceptionRootCause.getCode());
						baseResponse.setMsg(baseFrameworkExceptionRootCause.getMessage());
					} else {
						baseResponse.setRespCode(CommonRspCode.SYS_ERROR.getCode());
						baseResponse.setMsg(CommonRspCode.SYS_ERROR.getMessage());
					}
					rpcResult.setValue(baseResponse);

					LOGGER.info(LOG_DUBBO_PROVIDER_RESPONSE_MSG, methodName, consumerIp, baseResponse.toString());
					// span.addEvent(TRANS_TYPE_RPC_PROVIDER + ".response",
					// baseResponse.toString());
					span.addEvent(EVENT_TYPE_RPCRETURN, methodName + baseResponse.getRespCode());
				}
				return rpcResult;
			} catch (Exception exception) {
				LOGGER.error(LOG_FAILED_TO_RETURN_RPCRESULT, exception);
			} finally {
				LOGGER.error(LOG_DUBBO_CONSUMER_EX_MSG, methodName, consumerIp, e);
			}

			throw new RpcException(e);
		} finally {
			LOGGER.info(LOG_DUBBO_PROVIDER_OUT_MSG, methodName, consumerIp);
			span.close();
			LOGGER.cleanTraceInfoInMDC();
		}
	}
}
