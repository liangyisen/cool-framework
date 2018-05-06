package com.eiff.framework.fs.fastdfs.client;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.eiff.framework.fs.fastdfs.FileInfo;
import com.eiff.framework.fs.fastdfs.log.OperationMessage;
import com.eiff.framework.log.api.HdLogger;
import com.eiff.framework.log.api.trace.Span;
import com.eiff.framework.log.api.trace.Tracer;

public class HfsClientProxy implements InvocationHandler {

	private HfsClientInterface source;
	private HfsClientSourceWithPool poolSource;

	private final static Logger OPERATION_LOGGER = LoggerFactory.getLogger(OperationMessage.class);
	private final static HdLogger LOGGER = HdLogger.getLogger(HfsClient.class);

	public HfsClientProxy(HfsClientSource source, HfsClientSourceWithPool poolSource) {
		this.source = source;
		this.poolSource = poolSource;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if (this.poolSource.isConfiged()) {
			this.source = this.poolSource;
		}
		String methodName = method.getName();
		OperationMessage.init(methodName);
		Tracer buildTracer = LOGGER.buildTracer();
		Span span = buildTracer.createSpan("MW-FDFS", methodName);
		try {
			Object returnObj = method.invoke(this.source, args);
			span.success();
			return returnObj;
		} catch (IllegalAccessException | IllegalArgumentException e) {
			OperationMessage.get().setFailedType(e.getMessage());
			span.failed(e);
			Class<?>[] exceptionTypes = method.getExceptionTypes();
			if (exceptionTypes == null || exceptionTypes.length == 0) {
				throw e;
			} else {
				Throwable newException = (Throwable) exceptionTypes[0].newInstance();
				throw newException;
			}
		} catch (InvocationTargetException e) {
			OperationMessage.get().setFailedType(e.getMessage());
			Throwable th = e.getTargetException();
			if (th == null)
				th = e;
			span.failed(th);
			throw th;
		} catch (Throwable e) {
			OperationMessage.get().setFailedType(e.getMessage());
			span.failed(e);
			throw e;
		} finally {
			if (StringUtils.isEmpty(OperationMessage.get().getFileSource())) {
				if (StringUtils.isNotEmpty(OperationMessage.get().getFileId())) {
					try {
						FileInfo fileInfo = this.source.getFileInfo(OperationMessage.get().getFileId());
						OperationMessage.get().setFileSource(fileInfo != null ? fileInfo.getSourceIpAddr() : "");
					} catch (Exception e2) {
						LOGGER.error("", e2);
					}
				}
			}
			OPERATION_LOGGER.info(JSON.toJSONString(OperationMessage.get()));
			OperationMessage.clear();
			span.close();
		}
	}
}
