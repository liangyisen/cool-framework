package com.eiff.framework.cache.redis.spring;

import org.springframework.cache.interceptor.SimpleKeyGenerator;
import redis.clients.util.JedisClusterCRC16;

import java.lang.reflect.Method;

/**
 * @author tangzhaowei
 */
public class PrefixKeyGenerator extends SimpleKeyGenerator {

    private final static String SLOT_CHANGER = "{Qi}";
    private final static String COLON = ":";

    private String module;

    public PrefixKeyGenerator(String module) {
        super();
        this.module = module;
    }

    @Override
    public Object generate(Object target, Method method, Object... params) {
        Object result = super.generate(target, method, params);

        if (result instanceof String && JedisClusterCRC16.getSlot((String) result) == 0) {
            result = result + SLOT_CHANGER;
        }

        return module + COLON + result;
    }
}
