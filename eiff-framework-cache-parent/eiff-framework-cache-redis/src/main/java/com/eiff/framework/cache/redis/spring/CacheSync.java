package com.eiff.framework.cache.redis.spring;

/**
 * @author tangzhaowei
 */
public interface CacheSync {

    public void sendCommand(Command command);

}
