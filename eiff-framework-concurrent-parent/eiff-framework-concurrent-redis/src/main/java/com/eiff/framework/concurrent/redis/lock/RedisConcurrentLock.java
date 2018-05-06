package com.eiff.framework.concurrent.redis.lock;

import com.eiff.framework.common.biz.code.CommonRspCode;
import com.eiff.framework.concurrent.api.IConcurrentLock;
import com.eiff.framework.concurrent.redis.common.AssertUtils;
import com.eiff.framework.concurrent.redis.exception.LockAcquireException;
import org.apache.commons.lang3.Validate;
import org.redisson.Redisson;
import org.redisson.RedissonRedLock;
import org.redisson.api.RFuture;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.Codec;
import org.redisson.client.codec.StringCodec;
import org.redisson.client.protocol.RedisCommands;
import org.redisson.cluster.ClusterNodeInfo;
import org.redisson.command.CommandSyncService;
import org.redisson.config.ClusterServersConfig;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;

/**
 * 使用redis原子操作实现的分布式锁控制器
 *
 * @author tangzhaowei
 */
public class RedisConcurrentLock implements IConcurrentLock {

    private static Logger LOGGER = LoggerFactory.getLogger(RedisConcurrentLock.class);

    private static final String ADDRESS_SPLITER = ",";

    private static String CONCURRENT_LOCK_PREFIX = "redisConcurrentLock:";
    private static long LOCK_WATCHDOG_TIMEOUT = 60 * 60 * 1000;
    private static int DEFAULT_RETRY_TIMES = 3;
    private static long DEFAULT_RETRY_INTERVAL = 500;

    private long lockWaitTimeout = 3000;
    private RedissonClient redissonClient = null;
    private String[] hashLockKeys = new String[3];

    public RedisConcurrentLock(RedissonClient redissonClient) {
        Validate.notNull(redissonClient);
        setRedissonClient(redissonClient);
    }

    public RedisConcurrentLock(RedissonClient redissonClient, long lockWaitTimeout) {
        this(redissonClient);
        this.lockWaitTimeout = lockWaitTimeout;
    }

    public RedisConcurrentLock(String clusterNodes, Codec codec, int scanInterval, int retryInterval, String password,
                               int timeout, int pingTimeout, int connectTimeout,
                               int reconnectionTimeout, int idleConnectionTimeout, int masterConnectionPoolSize,
                               int slaveConnectionPoolSize, int subscriptionConnectionPoolSize) {
        Validate.notNull(clusterNodes);
        Config config = new Config();
        ClusterServersConfig clusterServersConfig = config.setCodec(codec)
                .useClusterServers()
                .setScanInterval(scanInterval)
                .setRetryInterval(retryInterval)
                .setPassword(password)
                .setTimeout(timeout)
                .setPingTimeout(pingTimeout)
                .setConnectTimeout(connectTimeout)
                .setReconnectionTimeout(reconnectionTimeout)
                .setIdleConnectionTimeout(idleConnectionTimeout)
                .setMasterConnectionPoolSize(masterConnectionPoolSize)
                .setSlaveConnectionPoolSize(slaveConnectionPoolSize)
                .setSubscriptionConnectionPoolSize(subscriptionConnectionPoolSize);
        for (String address : clusterNodes.split(ADDRESS_SPLITER)) {
            clusterServersConfig.addNodeAddress(address);
        }
        RedissonClient redissonClient = Redisson.create(config);
        Validate.notNull(redissonClient);
        setRedissonClient(redissonClient);
    }

    public RedisConcurrentLock(String clusterNodes, Codec codec, int scanInterval, int retryInterval, String password,
                               int timeout, int pingTimeout, int connectTimeout,
                               int reconnectionTimeout, int idleConnectionTimeout, int masterConnectionPoolSize,
                               int slaveConnectionPoolSize, int subscriptionConnectionPoolSize, long lockWaitTimeout) {
        this(clusterNodes, codec, scanInterval, retryInterval, password, timeout, pingTimeout, connectTimeout,
                reconnectionTimeout, idleConnectionTimeout, masterConnectionPoolSize, slaveConnectionPoolSize,
                subscriptionConnectionPoolSize);
        this.lockWaitTimeout = lockWaitTimeout;
    }

    @Override
    public void lock() {
        lock(Thread.currentThread().getName(), LOCK_WATCHDOG_TIMEOUT, DEFAULT_RETRY_INTERVAL, DEFAULT_RETRY_TIMES);
    }

    @Override
    public void lockInterruptibly() {
        try {
            lockInterruptibly(Thread.currentThread().getName(), LOCK_WATCHDOG_TIMEOUT);
        } catch (Exception e) {
            LOGGER.error("Get lockInterruptibly error.", e);
        }
    }

    @Override
    public boolean tryLock() {
        String concurrentKey = Thread.currentThread().getName();
        try {
            return tryLock(concurrentKey, LOCK_WATCHDOG_TIMEOUT);
        } catch (InterruptedException e) {
            LOGGER.warn("tryLock warn, key :" + concurrentKey, e);
        }
        return false;
    }

    @Override
    public boolean tryLock(long time, TimeUnit timeUnit) throws InterruptedException {
        AssertUtils.lessThan0((int) time);

        return tryLock(Thread.currentThread().getName(), TimeUnit.MILLISECONDS.convert(time, timeUnit));
    }

    @Override
    public void unlock() {
        unlock(Thread.currentThread().getName());
    }

    @Override
    public Condition newCondition() {
        // TODO implement
        throw new UnsupportedOperationException();
    }

    /**
     * 根据key获取分布式锁，获取不到就抛异常
     *
     * @param lockTime 单位为毫秒
     */
    public void lockInterruptibly(String concurrentKey, long lockTime) throws LockAcquireException, InterruptedException {
        Validate.notBlank(concurrentKey);
        AssertUtils.lessThan0((int) lockTime);

        if (!tryLock(concurrentKey, lockTime)) {
            throw new LockAcquireException("Lock lockInterruptibly failed for key: " + concurrentKey + ", "
                    + " redis tryLock operation failed, please wait.", CommonRspCode.REDIS_LOCK_OP_ERROR.getCode());
        }
    }

    /**
     * 根据key获取分布式锁，获取到锁返回true，获取不到返回false
     *
     * @param lockTime      锁占用时间，单位为毫秒
     * @return 是否获取到锁
     */
    public boolean tryLock(String concurrentKey, long lockTime) throws InterruptedException {
        Validate.notBlank(concurrentKey);
        AssertUtils.lessThan0((int) lockTime);

        return getRedissonRedLock(concurrentKey).tryLock(lockWaitTimeout, lockTime, TimeUnit.MILLISECONDS);
    }

    /**
     * 阻塞方式根据key获取分布式锁，获取到锁返回true，获取不到返回false，可以设置获取次数和每两次获取之间的间隔时间（毫秒）
     *
     * @param concurrentKey 锁的key
     * @param lockTime      锁占用时间，单位为毫秒
     * @param intervalTime  每两次获取锁之间的间隔时间，毫秒
     * @param tryTimes      获取锁尝试次数
     * @return 是否获取到锁
     */
    public boolean lock(String concurrentKey, long lockTime, long intervalTime, int tryTimes) {
        Validate.notBlank(concurrentKey);
        AssertUtils.lessThan0((int) lockTime);
        AssertUtils.lessThan0((int) intervalTime);
        AssertUtils.lessThan0(tryTimes);

        int times = tryTimes;
        while (times > 0) {
            try {
                if (tryLock(concurrentKey, lockTime)) {
                    return true;
                } else {
                    times--;
                    TimeUnit.MILLISECONDS.sleep(intervalTime);
                }
            } catch (InterruptedException ignored) {
            }
        }

        return false;
    }

    /**
     * 释放分布式锁，有可能释放锁失败
     */
    public void unlock(String concurrentKey) {
        Validate.notBlank(concurrentKey);

        RedissonRedLock redissonRedLock = getRedissonRedLock(concurrentKey);

        if (!isHeldByCurrentThread(concurrentKey)) {
            throw new LockAcquireException("Lock is not held by current thread for key: " + concurrentKey, CommonRspCode.REDIS_LOCK_OP_ERROR.getCode());
        }

        redissonRedLock.unlock();
    }

    /**
     * 是否有进程占用这个锁
     */
    public boolean isLocked(String concurrentKey) {
        Validate.notBlank(concurrentKey);

        int locked = 0;
        List<RLock> rLocks = getRLocks(concurrentKey);
        for (RLock rLock : rLocks) {
            if (rLock.isLocked()) {
                locked++;
            }
        }
        return locked > rLocks.size() / 2;
    }

    public boolean isHeldByCurrentThread(String concurrentKey) {
        Validate.notBlank(concurrentKey);

        int held = 0;
        List<RLock> rLocks = getRLocks(concurrentKey);
        for (RLock rLock : rLocks) {
            if (rLock.isHeldByCurrentThread()) {
                held++;
            }
        }
        return held > rLocks.size() / 2;
    }

    private void setRedissonClient(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
        List<ClusterNodeInfo> clusterNodeInfos;
        try {
            clusterNodeInfos = clusterNode();
        } catch (Exception e) {
            LOGGER.error("Get cluster info error. Please check cluster status", e);
            return;
        }
        int i = 0;
        for (ClusterNodeInfo clusterNodeInfo : clusterNodeInfos) {
            if (i >= 3) {
                break;
            }
            if (clusterNodeInfo.containsFlag(ClusterNodeInfo.Flag.MASTER)) {
                int slotIndex = clusterNodeInfo.getSlotRanges().iterator().next().getStartSlot();

                if (slotIndex == 0) {
                    continue;
                }

                if (slotIndex < 8192) {
                    hashLockKeys[i] = "{" + CRC16SlotTable1.crc16SlotTable[slotIndex] + "}";
                } else {
                    hashLockKeys[i] = "{" + CRC16SlotTable2.crc16SlotTable[slotIndex - 8192] + "}";
                }
                i++;
            }
        }
    }

    private List<ClusterNodeInfo> clusterNode() throws Exception {
        Redisson redisson = (Redisson) redissonClient;
        CommandSyncService commandSyncService = redisson.getConnectionManager().getCommandExecutor();
        RFuture<List<ClusterNodeInfo>> rFuture = commandSyncService.readAsync(null, RedisCommands.CLUSTER_NODES, new Object[0]);
        return rFuture.get();
    }

    private RedissonRedLock getRedissonRedLock(String concurrentKey) {
        RLock lock1 = redissonClient.getLock(CONCURRENT_LOCK_PREFIX + concurrentKey + hashLockKeys[0]);
        RLock lock2 = redissonClient.getLock(CONCURRENT_LOCK_PREFIX + concurrentKey + hashLockKeys[1]);
        RLock lock3 = redissonClient.getLock(CONCURRENT_LOCK_PREFIX + concurrentKey + hashLockKeys[2]);
        return new RedissonRedLock(lock1, lock2, lock3);
    }

    private List<RLock> getRLocks(String concurrentKey) {
        List<RLock> rLocks = new ArrayList<>();
        rLocks.add(redissonClient.getLock(CONCURRENT_LOCK_PREFIX + concurrentKey + hashLockKeys[0]));
        rLocks.add(redissonClient.getLock(CONCURRENT_LOCK_PREFIX + concurrentKey + hashLockKeys[1]));
        rLocks.add(redissonClient.getLock(CONCURRENT_LOCK_PREFIX + concurrentKey + hashLockKeys[2]));
        return rLocks;
    }
}
