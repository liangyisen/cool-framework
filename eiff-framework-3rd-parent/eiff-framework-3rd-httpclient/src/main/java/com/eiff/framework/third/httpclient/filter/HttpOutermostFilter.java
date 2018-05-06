package com.eiff.framework.third.httpclient.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;

import com.eiff.framework.common.biz.code.CommonRspCode;
import com.eiff.framework.log.api.Constants;
import com.eiff.framework.log.api.HdLogger;
import com.eiff.framework.log.api.trace.Span;
import com.eiff.framework.log.api.trace.Tracer;

public class HttpOutermostFilter implements Filter {
	private static HdLogger LOGGER = HdLogger.getLogger(HttpOutermostFilter.class);
	
	private final static String  IGNOREREQUESTPATH_KEY = "ignoreRequestPath";
	private final static String  ESCAPE_KEY = "escapes";
	private final static String  SEPARATOR = ";";
	
	private boolean ignoreRequestPath = false;
	private boolean escape = false;
	private String[] escapePaths = {};
	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		if(Boolean.valueOf(filterConfig.getInitParameter(IGNOREREQUESTPATH_KEY))){
			ignoreRequestPath = true;
		}
		String escapes = filterConfig.getInitParameter(ESCAPE_KEY);
		if(!StringUtils.isEmpty(escapes)){
			escape = true;
			escapePaths = escapes.split(SEPARATOR);
		}
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		Tracer buildTracer = LOGGER.buildTracer();
		Span span = buildTracer.createEmpty();
		
		if(escape){
			HttpServletRequest httpServletRequest = (HttpServletRequest)request;
			 String requestURI = httpServletRequest.getRequestURI();
			 for (String string : escapePaths) {
				 if(requestURI.startsWith(string)){
					 chain.doFilter(request, response);
					 return;
				 }
			}
		}
		
		try{
			String requestUri = "";
			boolean isHttpServletRequest = request instanceof HttpServletRequest;
			if(!ignoreRequestPath && isHttpServletRequest){
				HttpServletRequest httpServletRequest = (HttpServletRequest)request;
				requestUri = httpServletRequest.getRequestURI();
				span = buildTracer.createSpan(Constants.TRANS_TYPE_HTTP,  requestUri);
				LOGGER.logTraceInfoTMDC();
				LOGGER.info(httpServletRequest.getRequestURL().toString());
			}else{
				span = buildTracer.createSpan(Constants.TRANS_TYPE_HTTP,  Constants.TRANS_TYPE_HTTP);
				LOGGER.logTraceInfoTMDC();
			}
			if(isHttpServletRequest){
				LOGGER.info("traceidmapping: " + ((HttpServletRequest)request).getHeader(Constants.TRACE_ROOT));
				//CatUtils.buildContext((HttpServletRequest)request);
				HttpServletRequest httpServletRequest = (HttpServletRequest)request;
				requestUri = httpServletRequest.getRequestURI();
				span.addData( "url", requestUri);
			}
			chain.doFilter(request, response);
			
			boolean frontResponse = true;
			if(StringUtils.isNotEmpty(MDC.get(Constants.EVENT_FRONTRESPONSE_RPCRETURN))){
				if(StringUtils.isEmpty(MDC.get(Constants.EVENT_FRONTRESPONSE_RPCRETURN_SUCCESS))){
					frontResponse = false;
				}else{
					frontResponse = Boolean.parseBoolean(MDC.get(Constants.EVENT_FRONTRESPONSE_RPCRETURN_SUCCESS));
				}
			}
			
			if(frontResponse){
				span.success();
			}else{
				String returnCode = MDC.get(Constants.EVENT_FRONTRESPONSE_RPCRETURN);
				if(returnCode.equals(CommonRspCode.SUCCESS.getCode())){
					span.success();
				}else{
					if(!ignoreRequestPath){
						span.addEvent(Constants.EVENT_FRONTRESPONSE_RPCRETURN, requestUri + returnCode, returnCode);
					}
				}
			}
		} catch(Throwable e){
			span.failed(e);
			throw e;
		}finally{
			span.close();
			LOGGER.cleanTraceInfoInMDC();
			MDC.remove(Constants.EVENT_FRONTRESPONSE_RPCRETURN);
			MDC.remove(Constants.EVENT_FRONTRESPONSE_RPCRETURN_SUCCESS);
		}
	}

	@Override
	public void destroy() {
	}

}
