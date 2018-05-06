package com.eiff.framework.third.httpclient.wrapper;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

import com.eiff.framework.log.api.HdLogger;
import com.eiff.framework.log.api.trace.Tracer;

@SuppressWarnings("deprecation")
public class InTraceCloseableHttpClient  extends CloseableHttpClient {
	private static HdLogger LOGGER = HdLogger.getLogger(CloseableHttpClient.class);
	
	private CloseableHttpClient httpClient;
	
	private InTraceCloseableHttpClient(CloseableHttpClient httpClient) {
		this.httpClient = httpClient;
	}
	
	public static InTraceCloseableHttpClient wrapUp(CloseableHttpClient client){
		InTraceCloseableHttpClient inTraceCloseableHttpClient = new InTraceCloseableHttpClient(client);
		return inTraceCloseableHttpClient;
	}
	
	@Override
	public void close() throws IOException {
		httpClient.close();
	}

	@Override
    public CloseableHttpResponse execute(
            final HttpHost target,
            final HttpRequest request,
            final HttpContext context) throws IOException, ClientProtocolException {
		addHeader(request);
        return this.httpClient.execute(target, request, context);
    }

    @Override
    public CloseableHttpResponse execute(
            final HttpUriRequest request,
            final HttpContext context) throws IOException, ClientProtocolException {
    	addHeader(request);
    	return this.httpClient.execute(request, context);
    }

    @Override
    public CloseableHttpResponse execute(
            final HttpUriRequest request) throws IOException, ClientProtocolException {
    	addHeader(request);
    	return this.httpClient.execute(request);
    }

    @Override
    public CloseableHttpResponse execute(
            final HttpHost target,
            final HttpRequest request) throws IOException, ClientProtocolException {
    	addHeader(request);
    	return this.httpClient.execute(target, request);
    }

    @Override
    public <T> T execute(final HttpUriRequest request,
            final ResponseHandler<? extends T> responseHandler) throws IOException,
            ClientProtocolException {
    	addHeader(request);
    	return this.httpClient.execute(request, responseHandler);
    }

    @Override
    public <T> T execute(final HttpUriRequest request,
            final ResponseHandler<? extends T> responseHandler, final HttpContext context)
            throws IOException, ClientProtocolException {
    	addHeader(request);
    	return this.httpClient.execute(request, responseHandler, context);
    }

    @Override
    public <T> T execute(final HttpHost target, final HttpRequest request,
            final ResponseHandler<? extends T> responseHandler) throws IOException,
            ClientProtocolException {
    	addHeader(request);
    	return this.httpClient.execute(target, request, responseHandler);
    }

    @Override
    public <T> T execute(final HttpHost target, final HttpRequest request,
            final ResponseHandler<? extends T> responseHandler, final HttpContext context)
            throws IOException, ClientProtocolException {
    	addHeader(request);
    	return this.httpClient.execute(target, request, responseHandler, context);
    }
    
	@Override
	public HttpParams getParams() {
		return httpClient.getParams();
	}

	@Override
	public ClientConnectionManager getConnectionManager() {
		return httpClient.getConnectionManager();
	}
	
	private void addHeader(HttpRequest request){
		Tracer buildTracer = LOGGER.buildTracer();
		Map<String, String> context = buildTracer.getContext();
		if(context == null) return;
		for (Map.Entry<String, String> head: context.entrySet()) {
			 request.addHeader(head.getKey(), head.getValue());
		}
	}

	@Override
	protected CloseableHttpResponse doExecute(HttpHost target, HttpRequest request, HttpContext context)
			throws IOException, ClientProtocolException {
		Method declaredMethod;
		Object invoke = null;
		try {
			declaredMethod = httpClient.getClass().getDeclaredMethod("doExecute", HttpHost.class, HttpRequest.class, HttpContext.class);
			invoke = declaredMethod.invoke(httpClient, target, request, context);
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
		return (CloseableHttpResponse)invoke;
	}
}
