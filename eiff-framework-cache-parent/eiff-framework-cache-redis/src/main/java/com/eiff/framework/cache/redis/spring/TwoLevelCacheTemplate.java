package com.eiff.framework.cache.redis.spring;

import com.eiff.framework.log.api.HdLogger;
import org.ehcache.Cache;
import org.ehcache.CacheIterationException;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.MemoryUnit;
import org.ehcache.expiry.Duration;
import org.ehcache.expiry.Expirations;
import org.slf4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.redis.core.RedisTemplate;
import com.eiff.framework.cache.redis.spring.CacheInfo.Level;
import com.eiff.framework.cache.redis.spring.CacheInfo.Operator;

import java.util.*;
import java.util.concurrent.*;

/**
 * @author tangzhaowei
 */
public class TwoLevelCacheTemplate implements InitializingBean {

    public static final Logger logger = HdLogger.getLogger(TwoLevelCacheTemplate.class);

    private String module = "twoLevelCache";
    private String spliter = ":";
    /**
     * 是否启用本地缓存
     */
    private boolean localEnabled = true;

    /**
     * 本地缓存最大内存大小
     */
    private long localMaxHeapSize = 256;

    /**
     * 本地缓存10分钟过期
     */
    private int localTimeToLiveSeconds = 10 * 60;
    private int localTimeToIdleSeconds = 10 * 60;

    private RedisTemplate<String, Object> redisTemplate;
    private CacheManager cacheManager;
    private CacheSync cacheSync;
    private ConcurrentHashMap<String, Future<Cache<String, Object>>> caches = new ConcurrentHashMap<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        CacheInfo.MODULE = module;
        CacheInfo.CACHE_STORE = module + spliter + "cache";
        CacheInfo.CACHE_CHANNEL = CacheInfo.CACHE_STORE + spliter + "sync";
        if (this.localEnabled) {
            this.cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
                    .withCache(CacheInfo.MODULE,
                            CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, Object.class,
                                    ResourcePoolsBuilder.newResourcePoolsBuilder()
                                            .heap(localMaxHeapSize, MemoryUnit.MB))
                                    .withExpiry(Expirations.timeToLiveExpiration(Duration.of(localTimeToLiveSeconds, TimeUnit.SECONDS)))
                                    .withExpiry(Expirations.timeToIdleExpiration(Duration.of(localTimeToIdleSeconds, TimeUnit.SECONDS))).build()).build();
            cacheManager.init();
            this.cacheSync = new RedisPubSubSync(this);
        }
    }

    /**
     * 设置缓存
     */
    public void set(String name, String key, Object value) {
        this.set(name, key, value, Level.Remote);
        if (localEnabled) {
            this.sendSetCmd(name, key, value);
        }
    }

    /**
     * 设置缓存(根据缓存层级)
     */
    protected void set(String name, String key, Object value, Level level) {
        if (level.equals(Level.Local)) {
            if (!localEnabled) {
                return;
            }
            this.getCache(name).put(key, value);
        } else {
            this.syncToRedis(name, key, value, localTimeToLiveSeconds, Operator.SET);
        }
    }

    /**
     * 设置缓存与过期时间(根据缓存层级)
     */
    protected void set(String name, String key, Object value, int ttl, Level level) {
        if (level.equals(Level.Local)) {
            if (!localEnabled) {
                return;
            }
            if (ttl < 0) {
                return;
            } else {
                this.getCache(name).put(key, value);
            }
        } else {
            this.syncToRedis(name, key, value, ttl, Operator.SET);
        }
    }

    /**
     * 若缓存不存在则设置,若存在则返回原缓存值
     */
    public <T> T setIfAbsent(String name, String key, Object value) {
        T existing = this.get(name, key);
        if (existing != null) {
            return existing;
        } else {
            this.set(name, key, value);
            return null;
        }
    }

    /**
     * 获取缓存值
     */
    public <T> T get(String name, String key) {
        T value = null;
        if (localEnabled) {
            value = this.get(name, key, Level.Local);
        }
        if (value == null) {
            value = this.get(name, key, Level.Remote);
        }
        return value;
    }

    /**
     * 获取缓存值(根据缓存层级)
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String name, String key, Level level) {
        T value = null;
        if (level.equals(Level.Local)) {
            if (!localEnabled) {
                return null;
            }
            if (!this.caches.containsKey(name)) {
                return null;
            }
            Object element = this.getCache(name).get(key);
            if (element != null) {
                value = (T) element;
            }
        } else {
            value = (T) this.redisTemplate.opsForValue().get(this.getRedisKeyOfElement(name, key));
            if (value != null) {
                int ttl = this.ttl(name, key);
                if (ttl < 0) {
                    // key 已经失效
                    // ignore...
                } else {
                    this.set(name, key, value, ttl, Level.Local);
                }
            }
        }
        return value;
    }

    /**
     * 删除单个缓存值
     */
    public void del(String name, String key) {
        this.del(name, key, Level.Remote);
        this.sendDelCmd(name, key);
    }

    /**
     * 删除单个缓存值(根据缓存层级)
     */
    protected void del(String name, String key, Level level) {
        if (level.equals(Level.Local)) {
            if (!localEnabled) {
                return;
            }
            if (this.caches.containsKey(name)) {
                this.getCache(name).remove(key);
            }
        } else {
            this.syncToRedis(name, key, Operator.DEL);
        }
    }

    /**
     * 删除指定name下所有缓存
     */
    public void rem(String name) {
        this.rem(name, Level.Remote);
        this.sendRemCmd(name);
    }

    /**
     * 删除指定name下所有缓存(根据缓存层级)
     */
    protected void rem(String name, Level level) {
        if (level.equals(Level.Local)) {
            if (!localEnabled) {
                return;
            }
            if (this.caches.containsKey(name)) {
                this.getCache(name).clear();
                this.caches.remove(name);
            }
        } else {
            this.syncToRedis(name, Operator.REM);
        }
    }

    /**
     * 获取redis缓存过期剩余时间.单位为秒
     */
    public int ttl(String name, String key) {
        return this.redisTemplate.getExpire(this.getRedisKeyOfElement(name, key)).intValue();
    }

    /**
     * 获取所有缓存名称
     */
    public Set<String> names() {
        return this.getCaches();
    }

    /**
     * 关闭
     */
    public void shutdown() {
        this.cacheManager.close();
    }

    /**
     * 以如下数据格式同步单机缓存至全局Redis中保存.实现统一查询,抓取,删除等功能.
     * twoLevelCache:cache(set - cache name)
     * - user
     * twoLevelCache:cache:user(hash - key : ttl)
     * - John
     * - Terry
     * twoLevelCache:cache:user:John(string - key : value)
     * twoLevelCache:cache:user:Terry
     */
    private void syncToRedis(String name, String field, Object value, int timeToLiveSeconds, Operator operator) {
        if (operator.equals(Operator.SET)) {
            // 存入
            // twoLevelCache:cache:user:John
            // twoLevelCache:cache:user:Terry
            this.redisTemplate.opsForValue().set(this.getRedisKeyOfElement(name, field), value, timeToLiveSeconds, TimeUnit.SECONDS);
            // 创建数据结构
            // 存入
            // twoLevelCache:cache
            // - user
            this.redisTemplate.opsForSet().add(this.getRedisKeyOfStore(), name);
            // 存入
            // twoLevelCache:cache:user
            // - John
            // - Terry
            this.redisTemplate.opsForHash().put(this.getRedisKeyOfCache(name), field, timeToLiveSeconds);
        } else if (operator.equals(Operator.DEL)) {
            // 删除 twoLevelCache:cache:user:John
            this.redisTemplate.delete(this.getRedisKeyOfElement(name, field));
            // 删除 twoLevelCache:cache:user.John
            this.redisTemplate.opsForHash().delete(this.getRedisKeyOfCache(name), field);
        } else if (operator.equals(Operator.REM)) {
            // 获取 twoLevelCache:cache:user 所有 field
            Set<Object> storefields = this.redisTemplate.opsForHash().keys(this.getRedisKeyOfCache(name));
            List<String> deletekeys = new ArrayList<>();
            for (Object storefield : storefields) {
                if (!(storefield instanceof String)) {
                    continue;
                }
                // 依次取Key,并记录到要删除key列表
                deletekeys.add(this.getRedisKeyOfElement(name, (String) storefield));
            }
            // 将 twoLevelCache:cache:user,记录到要删除key列表
            deletekeys.add(this.getRedisKeyOfCache(name));
            // 批量删除
            this.redisTemplate.delete(deletekeys);
            // 删除 twoLevelCache:cache:user
            this.redisTemplate.opsForSet().remove(this.getRedisKeyOfStore(), name);
        } else {
            logger.warn("SyncToRedis > Unknown Operator");
        }
    }

    private void syncToRedis(String name, Operator operator) {
        this.syncToRedis(name, null, null, 0, operator);
    }

    private void syncToRedis(String name, String field, Operator operator) {
        this.syncToRedis(name, field, null, 0, operator);
    }

    /**
     * 发送新增缓存命令
     */
    private void sendSetCmd(String name, String key, Object value) {
        if (localEnabled) {
            Command c = Command.set(name, key, value);
            cacheSync.sendCommand(c);
        }
    }

    /**
     * 发送删除缓存命令
     */
    private void sendDelCmd(String name, String key) {
        if (localEnabled) {
            Command c = Command.del(name, key);
            cacheSync.sendCommand(c);
        }
    }

    /**
     * 发送移除缓存命令
     */
    private void sendRemCmd(String name) {
        if (localEnabled) {
            Command c = Command.rem(name);
            cacheSync.sendCommand(c);
        }
    }

    /**
     * 创建本地缓存
     */
    private Cache<String, Object> getCache(final String name) {
        Future<Cache<String, Object>> future = this.caches.get(name);
        if (future == null) {
            Callable<Cache<String, Object>> callable = new Callable<Cache<String, Object>>() {
                @Override
                public Cache<String, Object> call() throws Exception {
                    Cache<String, Object> cache = cacheManager.getCache(name, String.class, Object.class);
                    if (cache == null) {
                        cache = cacheManager.createCache(name, CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, Object.class,
                                ResourcePoolsBuilder.newResourcePoolsBuilder()
                                        .heap(localMaxHeapSize, MemoryUnit.MB))
                                .withExpiry(Expirations.timeToLiveExpiration(Duration.of(localTimeToLiveSeconds, TimeUnit.SECONDS)))
                                .withExpiry(Expirations.timeToIdleExpiration(Duration.of(localTimeToIdleSeconds, TimeUnit.SECONDS))).build());
                    }
                    return cache;
                }
            };
            FutureTask<Cache<String, Object>> task = new FutureTask<>(callable);
            future = this.caches.putIfAbsent(name, task);
            if (future == null) {
                future = task;
                task.run();
            }
        }
        try {
            return future.get();
        } catch (Exception e) {
            this.caches.remove(name);
            throw new CacheIterationException(e);
        }
    }

    private Set<String> getCaches() {
        Set<String> caches = new HashSet<>();
        Set<Object> members = this.redisTemplate.opsForSet().members(this.getRedisKeyOfStore());
        for (Object member : members) {
            if (!(member instanceof String)) {
                continue;
            }

            if (this.redisTemplate.opsForHash().size(this.getRedisKeyOfCache((String) member)) == 0) {
                this.redisTemplate.opsForSet().remove(this.getRedisKeyOfStore(), member);
                continue;
            }
            caches.add((String) member);
        }
        return caches;
    }

    private String getRedisKeyOfStore() {
        return CacheInfo.CACHE_STORE;
    }

    private String getRedisKeyOfCache(String name) {
        return CacheInfo.CACHE_STORE + spliter + name;
    }

    private String getRedisKeyOfElement(String name, String key) {
        return CacheInfo.CACHE_STORE + spliter + name + spliter + key;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public RedisTemplate<String, Object> getRedisTemplate() {
        return redisTemplate;
    }

    public void setRedisTemplate(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public CacheSync getCacheSync() {
        return cacheSync;
    }

    public void setCacheSync(CacheSync cacheSync) {
        this.cacheSync = cacheSync;
    }

    public long getLocalMaxHeapSize() {
        return localMaxHeapSize;
    }

    public void setLocalMaxHeapSize(long localMaxHeapSize) {
        this.localMaxHeapSize = localMaxHeapSize;
    }

    public int getLocalTimeToLiveSeconds() {
        return localTimeToLiveSeconds;
    }

    public void setLocalTimeToLiveSeconds(int localTimeToLiveSeconds) {
        this.localTimeToLiveSeconds = localTimeToLiveSeconds;
    }

    public int getLocalTimeToIdleSeconds() {
        return localTimeToIdleSeconds;
    }

    public void setLocalTimeToIdleSeconds(int localTimeToIdleSeconds) {
        this.localTimeToIdleSeconds = localTimeToIdleSeconds;
    }

    public void setLocalEnabled(boolean localEnabled) {
        this.localEnabled = localEnabled;
    }
}
