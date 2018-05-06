package com.eiff.framework.rpc.filter.hystrix;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.remoting.RemotingException;
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
import com.eiff.framework.common.biz.pkg.BaseResponse;
import com.eiff.framework.log.api.Constants;
import com.eiff.framework.log.api.HdLogger;
import com.eiff.framework.log.api.trace.Span;
import com.eiff.framework.log.api.trace.Tracer;
import com.eiff.framework.rpc.filter.DubboProviderFilter;
import com.eiff.framework.rpc.trace.DubboTraceLinker;
import com.netflix.hystrix.exception.HystrixBadRequestException;
import com.netflix.hystrix.exception.HystrixRuntimeException;

@Activate(group = { com.alibaba.dubbo.common.Constants.CONSUMER })
public class DubboConsumerFilter implements Filter, Constants {

	private static HdLogger LOGGER = HdLogger.getLogger(DubboProviderFilter.class);
	private static final String MAX_CON = ".maxcon";

	private final static Set<String> EXCLUDES = new HashSet<String>();

	static {
		EXCLUDES.add(com.alibaba.dubbo.monitor.MonitorService.class.getName());
	}

	@Override
	public Result invoke(Invoker<?> invoker, final Invocation invocation) throws RpcException {
		Tracer buildTracer = LOGGER.buildTracer();
		Span span = buildTracer.createEmpty();
		String methodName = null;
		String providerIp = null;

		String invokerInterfaceName = null;
		String invokerMethodName = null;
		try {
			invokerInterfaceName = invoker.getInterface().getName();
			if (EXCLUDES.contains(invokerInterfaceName)) {
				return invoker.invoke(invocation);
			}
		} catch (RpcException e) {
			throw e;
		} catch (Throwable e) {
		}

		try {
			invokerMethodName = invocation.getMethodName();
			methodName = invokerInterfaceName + "." + invokerMethodName;
			providerIp = RpcContext.getContext().getRemoteHost() + ":" + RpcContext.getContext().getRemotePort();

			span = buildTracer.createSpan(TRANS_TYPE_RPC_CONSUMER, methodName);
			LOGGER.info(LOG_DUBBO_CONSUMER_IN_MSG, methodName, providerIp);
			span.addEvent(TRANS_TYPE_RPC_CONSUMER + ".server", providerIp);

			Object[] args = invocation.getArguments();
			if (ArrayUtils.isNotEmpty(args)) {
				List<String> argsList = new ArrayList<>();
				for (Object arg : args) {
					argsList.add(String.valueOf(arg));
				}
				LOGGER.info(LOG_DUBBO_CONSUMER_REQUEST_MSG, methodName, providerIp, argsList);
			}

			buildTracer.create(new DubboTraceLinker(invocation)).send();
		} catch (Exception e) {
			LOGGER.error(LOG_FAILED_TO_CREATE_TRANS, e);
		}

		Result result = null;

		String maxConStr = invoker.getUrl().getMethodParameter(invokerMethodName, invokerMethodName + MAX_CON);
		int maxCon = -1;
		if (StringUtils.isNotBlank(maxConStr)) {
			try {
				maxCon = Integer.parseInt(maxConStr);
			} catch (Exception e) {
			}
		}

		try {
			if (maxCon >= 0) {
				DubboCommand dubboCommand = new DubboCommand(invoker, invocation, maxCon);
				result = dubboCommand.execute();
			} else {
				result = invoker.invoke(invocation);
			}

			if (result != null && result instanceof RpcResult) {
				RpcResult rpcResult = (RpcResult) result;
				Throwable throwable = rpcResult.getException();
				Object resultObject = rpcResult.getValue();
				if (resultObject == null) {
					Class<?> returnType = invoker.getInterface()
							.getMethod(invokerMethodName, invocation.getParameterTypes()).getReturnType();
					if (BaseResponse.class.isAssignableFrom(returnType)) {
						resultObject = (BaseResponse) returnType.newInstance();
						rpcResult.setValue(resultObject);
					} else {
						span.success();
						return result;
					}
				}

				if (resultObject instanceof BaseResponse) {
					BaseResponse baseResponse = (BaseResponse) resultObject;

					if (throwable != null) {

						if (throwable instanceof BaseBusinessException) {
							baseResponse.setRespCode(((BaseBusinessException) throwable).getCode());
							baseResponse.setMsg(throwable.getMessage());
						} else if (throwable instanceof TimeoutException) {
							baseResponse.setRespCode(CommonRspCode.SYS_TIMEOUT.getCode());
							baseResponse.setMsg(CommonRspCode.SYS_TIMEOUT.getMessage());
						} else if (throwable instanceof RpcException) {
							if (throwable.getCause() != null && throwable.getCause() instanceof TimeoutException) {
								throwable = throwable.getCause();
								baseResponse.setRespCode(CommonRspCode.SYS_TIMEOUT.getCode());
								baseResponse.setMsg(CommonRspCode.SYS_TIMEOUT.getMessage());
							} else {
								if (throwable.getCause() != null) {
									throwable = throwable.getCause();
								}
								baseResponse.setRespCode(CommonRspCode.RPC_ERROR.getCode());
								baseResponse.setMsg(CommonRspCode.RPC_ERROR.getMessage());
							}
						} else {
							baseResponse.setRespCode(CommonRspCode.SYS_ERROR.getCode());
							baseResponse.setMsg(CommonRspCode.SYS_ERROR.getMessage());
						}

						LOGGER.error(LOG_DUBBO_CONSUMER_EX_MSG, methodName, providerIp, throwable);
						rpcResult.setException(null);
					}

					LOGGER.info(LOG_DUBBO_CONSUMER_RESPONSE_MSG, methodName, providerIp, baseResponse.toString());
					// span.addEvent(TRANS_TYPE_RPC_CONSUMER + ".response",
					// baseResponse.toString());
				} else if (resultObject instanceof String || resultObject instanceof Integer
						|| resultObject instanceof Long || resultObject instanceof Double
						|| resultObject instanceof Float || resultObject instanceof Character
						|| resultObject instanceof Boolean) {
					LOGGER.info(LOG_DUBBO_CONSUMER_RESPONSE_MSG, methodName, providerIp, String.valueOf(resultObject));
					// span.addEvent(TRANS_TYPE_RPC_CONSUMER + ".response",
					// String.valueOf(resultObject));
				}

				if (throwable != null) {
					span.failed(throwable.getClass().getName());
				} else {
					span.success();
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
						.getMethod(invokerMethodName, invocation.getParameterTypes()).getReturnType();
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
							baseResponse.setRespCode(CommonRspCode.SYS_TIMEOUT.getCode());
							baseResponse.setMsg(CommonRspCode.SYS_TIMEOUT.getMessage());
							e = e.getCause();
						} else {
							if (e.getCause() != null) {
								e = e.getCause();
							}
							baseResponse.setRespCode(CommonRspCode.RPC_ERROR.getCode());
							baseResponse.setMsg(CommonRspCode.RPC_ERROR.getMessage());
						}
					} else if (e instanceof RemotingException) {
						baseResponse.setRespCode(CommonRspCode.RPC_ERROR.getCode());
						baseResponse.setMsg(CommonRspCode.RPC_ERROR.getMessage());
					} else if (e instanceof HystrixRuntimeException) {
						HystrixRuntimeException hystrixRuntimeException = (HystrixRuntimeException) e;
						if (hystrixRuntimeException.getFailureType() != null) {
							if (hystrixRuntimeException.getFailureType()
									.equals(HystrixRuntimeException.FailureType.REJECTED_SEMAPHORE_EXECUTION)) {
								baseResponse.setRespCode(CommonRspCode.MAX_CON.getCode());
								baseResponse.setMsg(CommonRspCode.MAX_CON.getMessage());
							} else if (hystrixRuntimeException.getFailureType()
									.equals(HystrixRuntimeException.FailureType.SHORTCIRCUIT)) {
								baseResponse.setRespCode(CommonRspCode.CIR_BRE.getCode());
								baseResponse.setMsg(CommonRspCode.CIR_BRE.getMessage());
							} else if (hystrixRuntimeException.getFailureType()
									.equals(HystrixRuntimeException.FailureType.COMMAND_EXCEPTION)) {
								Throwable fallbackThrowable = hystrixRuntimeException.getFallbackException();
								if (fallbackThrowable != null && fallbackThrowable instanceof RuntimeException
										&& fallbackThrowable.getCause() != null) {
									e = fallbackThrowable.getCause();
									if (fallbackThrowable.getCause() instanceof BaseBusinessException) {
										baseResponse.setRespCode(((BaseBusinessException) e).getCode());
										baseResponse.setMsg(e.getMessage());
									} else if (fallbackThrowable.getCause() instanceof TimeoutException) {
										baseResponse.setRespCode(CommonRspCode.SYS_TIMEOUT.getCode());
										baseResponse.setMsg(CommonRspCode.SYS_TIMEOUT.getMessage());
									} else if (fallbackThrowable.getCause() instanceof RpcException) {
										if (fallbackThrowable.getCause().getCause() != null && fallbackThrowable
												.getCause().getCause() instanceof TimeoutException) {
											baseResponse.setRespCode(CommonRspCode.SYS_TIMEOUT.getCode());
											baseResponse.setMsg(CommonRspCode.SYS_TIMEOUT.getMessage());
											e = fallbackThrowable.getCause().getCause();
										} else {
											if (fallbackThrowable.getCause().getCause() != null) {
												e = fallbackThrowable.getCause().getCause();
											}
											baseResponse.setRespCode(CommonRspCode.RPC_ERROR.getCode());
											baseResponse.setMsg(CommonRspCode.RPC_ERROR.getMessage());
										}
									} else {
										baseResponse.setRespCode(CommonRspCode.SYS_ERROR.getCode());
										baseResponse.setMsg(CommonRspCode.SYS_ERROR.getMessage());
									}
								} else {
									baseResponse.setRespCode(CommonRspCode.CB_ERROR.getCode());
									baseResponse.setMsg(CommonRspCode.CB_ERROR.getMessage());
								}
							} else {
								baseResponse.setRespCode(CommonRspCode.CB_ERROR.getCode());
								baseResponse.setMsg(CommonRspCode.CB_ERROR.getMessage());
							}
						} else {
							baseResponse.setRespCode(CommonRspCode.CB_ERROR.getCode());
							baseResponse.setMsg(CommonRspCode.CB_ERROR.getMessage());
						}
					} else if (e instanceof HystrixBadRequestException) {
						baseResponse.setRespCode(CommonRspCode.CB_ERROR.getCode());
						baseResponse.setMsg(CommonRspCode.CB_ERROR.getMessage());
					} else {
						baseResponse.setRespCode(CommonRspCode.SYS_ERROR.getCode());
						baseResponse.setMsg(CommonRspCode.SYS_ERROR.getMessage());
					}
					rpcResult.setValue(baseResponse);

					LOGGER.info(LOG_DUBBO_CONSUMER_RESPONSE_MSG, methodName, providerIp, baseResponse.toString());
					// span.addEvent(TRANS_TYPE_RPC_CONSUMER + ".response",
					// baseResponse.toString());
				}

				LOGGER.error(LOG_DUBBO_CONSUMER_EX_MSG, methodName, providerIp, e);
				return rpcResult;
			} catch (Exception exception) {
				LOGGER.error(LOG_FAILED_TO_RETURN_RPCRESULT, exception);
			}

			throw new RpcException(e);
		} finally {
			LOGGER.info(LOG_DUBBO_CONSUMER_OUT_MSG, methodName, providerIp);
			span.close();
		}
	}
}