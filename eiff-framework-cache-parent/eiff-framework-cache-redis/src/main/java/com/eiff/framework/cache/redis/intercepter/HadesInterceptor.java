package com.eiff.framework.cache.redis.intercepter;

import java.util.Arrays;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cglib.core.Constants;

import com.eiff.framework.cache.redis.client.HadesClient;
import com.eiff.framework.log.api.HdLogger;
import com.eiff.framework.log.api.trace.Span;
import com.eiff.framework.log.api.trace.Tracer;

public class HadesInterceptor implements MethodInterceptor, Constants {

    class KeyLogger {
    }

    private final static HdLogger LOGGER = HdLogger.getLogger(HadesInterceptor.class);
    private static Logger KEY_LOGGER = LoggerFactory
            .getLogger(HadesInterceptor.class.getName() + "." + KeyLogger.class.getSimpleName());

    private static final String GET = "get";
    private static final String PUT = "put";
    private static final String ADD = "add";
    private static final String REMOVE = "remove";
    private static final String DEL = "del";

    private static final String CACHE_TYPE = "Cache.redis-1";


    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        String keyPrefix = "";

        String hadesMethodName = invocation.getMethod().getName();
        String hadesMethodCategroy = hadesMethodName;
        if (hadesMethodName.startsWith(GET)) {
            hadesMethodCategroy = GET;
        } else {
            if (hadesMethodName.startsWith(PUT)) {
                hadesMethodCategroy = ADD;
            } else {
                if (hadesMethodName.equals(DEL)) {
                    hadesMethodCategroy = REMOVE;
                }
            }
        }
        Tracer buildTracer = LOGGER.buildTracer();
        Span span = buildTracer.createSpan(CACHE_TYPE, hadesMethodName + ":" + hadesMethodCategroy);
        try {
            try {
                if (invocation.getThis() instanceof HadesClient) {
                    keyPrefix = ((HadesClient) invocation.getThis()).getKeyPrefix();
                }
                Object[] args = invocation.getArguments();
                if (ArrayUtils.isNotEmpty(args)) {
                    Object keyOrNot = args[0];
                    if (keyOrNot == null) {
                        LOGGER.warn("CANNOT_LOG_NULL_KEY");
                        span.addData("key", "null");
                    } else if (keyOrNot instanceof String) {
                        keyOrNot = keyPrefix + keyOrNot;
                        KEY_LOGGER.info(String.valueOf(keyOrNot));
                        span.addData("key", String.valueOf(keyOrNot));
                    } else if (keyOrNot.getClass().isArray()) {
                        Object[] keyArrays = (Object[]) keyOrNot;
                        if (keyArrays[0] != null && keyArrays[0] instanceof String) {
                            String[] keyStrings = new String[keyArrays.length > 10 ? 10 : keyArrays.length];
                            for (int i = 0; i < keyStrings.length; i++) {
                                KEY_LOGGER.info(keyPrefix + String.valueOf(keyArrays[i]));
                                keyStrings[i] = keyPrefix + String.valueOf(keyArrays[i]);
                            }
                            span.addData("key", Arrays.toString(keyStrings));
                        } else {
                            LOGGER.warn("CANNOT_LOG_KEY {} ", keyOrNot.getClass().toString());
                        }
                    } else {
                        LOGGER.warn("CANNOT_LOG_KEY {} ", keyOrNot.getClass().toString());
                    }
                }
            } catch (Throwable e) {
                LOGGER.error("", e);
            }
            Object returnValue = invocation.proceed();
            if (returnValue == null) {
                if (GET.equals(hadesMethodCategroy)) {
                    span.addEvent(CACHE_TYPE, hadesMethodName + ":missed");
                }
            }
            span.success();
            return returnValue;
        } catch (Throwable e) {
            span.failed(e);
            throw e;
        } finally {
            span.close();
        }
    }
}
