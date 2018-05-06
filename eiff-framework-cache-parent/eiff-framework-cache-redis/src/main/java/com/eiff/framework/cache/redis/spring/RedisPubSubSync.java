package com.eiff.framework.cache.redis.spring;

import com.eiff.framework.cache.redis.common.SerializeUtils;
import com.eiff.framework.log.api.HdLogger;
import org.slf4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import redis.clients.jedis.JedisPubSub;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author tangzhaowei
 */
public class RedisPubSubSync extends JedisPubSub implements CacheSync {

    public static final Logger logger = HdLogger.getLogger(RedisPubSubSync.class);

    private TwoLevelCacheTemplate twoLevelCacheTemplate;
    private RedisTemplate<String, Object> redisTemplate;
    private CacheSyncHandler   cacheSyncHandler;

    public RedisPubSubSync(final TwoLevelCacheTemplate twoLevelCacheTemplate) {
        this.twoLevelCacheTemplate = twoLevelCacheTemplate;
        this.cacheSyncHandler = new CacheSyncHandler(this.twoLevelCacheTemplate);
        this.redisTemplate = twoLevelCacheTemplate.getRedisTemplate();

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                redisTemplate.execute(new RedisCallback<Object>() {
                    @Override
                    public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
                        redisConnection.subscribe(new MessageListener() {
                            @Override
                            public void onMessage(Message message, byte[] channel) {
                                Command cmd = null;
                                try {
                                    cmd = (Command) SerializeUtils.unserialize(message.getBody());
                                } catch (Exception e) {
                                    logger.error("deserialize cmd error.", e);
                                }
                                if (cmd != null) {
                                    if (cmd.src != null && cmd.src.equals(CacheInfo.MODULE)) {
                                        cacheSyncHandler.handle(cmd);
                                    }
                                }
                            }
                        }, CacheInfo.CACHE_CHANNEL.getBytes());
                        return null;
                    }
                });
            }
        });
    }

    @Override
    public void sendCommand(Command command) {
        this.redisTemplate.convertAndSend(CacheInfo.CACHE_CHANNEL, command);
    }

}