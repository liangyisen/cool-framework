package com.eiff.framework.cache.redis.spring;

/**
 * @author tangzhaowei
 */
public class CacheInfo {

    public static String MODULE;
    public static String CACHE_STORE;
    public static String CACHE_CHANNEL;

    private CacheInfo() {
    }

    public static enum Level {
        Local, Remote
    }

    public static enum Operator {
        SET, GET, DEL, REM
    }

}
