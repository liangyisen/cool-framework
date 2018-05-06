package com.eiff.framework.cache.redis.common;

/**
 * @author tangzhaowei
 */
public class RedisKeyUtils {

    private static final String KEY_SPLIT_CHAR = ":";

    public static String keyBuilder(String module1, String key, String... otherModules){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(module1);
        for (String otherModule : otherModules) {
            stringBuilder.append(KEY_SPLIT_CHAR).append(otherModule);
        }
        stringBuilder.append(KEY_SPLIT_CHAR).append(key);
        return stringBuilder.toString();
    }
}
