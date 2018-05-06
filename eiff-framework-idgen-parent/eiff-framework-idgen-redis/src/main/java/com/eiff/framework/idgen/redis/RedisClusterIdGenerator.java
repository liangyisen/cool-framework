package com.eiff.framework.idgen.redis;

import com.eiff.framework.idgen.api.AbstractIncrementIdGenerator;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.util.JedisClusterCRC16;

import java.util.HashSet;
import java.util.Set;

/**
 * @author tangzhaowei
 */
public class RedisClusterIdGenerator extends AbstractIncrementIdGenerator {

    private final static String COLON = ":";
    private final static String KEY = "idGenerator";
    private final static String SLOT_CHANGER = "{Qi}";

    private JedisCluster jedisCluster;

    public RedisClusterIdGenerator(JedisCluster jedisCluster, String prefix, int preFetchCount, int idLen) {
        this.jedisCluster = jedisCluster;
        this.prefix = prefix;
        this.preFetchCount = preFetchCount;
        this.idLen = idLen;

        fillingIds();
    }

    public RedisClusterIdGenerator(JedisCluster jedisCluster, String prefix, int preFetchCount) {
        this.jedisCluster = jedisCluster;
        this.prefix = prefix;
        this.preFetchCount = preFetchCount;

        fillingIds();
    }

    public RedisClusterIdGenerator(String clusterNodes, int connectionTimeout, int soTimeout, int maxAttempts, String password,
                       GenericObjectPoolConfig poolConfig, String prefix, int preFetchCount) {
        Validate.notNull(clusterNodes);
        String[] clusterNodesArray = clusterNodes.split(",");
        Set<HostAndPort> hostAndPorts = new HashSet<>();
        for (String clusterNode : clusterNodesArray) {
            hostAndPorts.add(HostAndPort.parseString(clusterNode));
        }
        this.jedisCluster = new JedisCluster(hostAndPorts, connectionTimeout, soTimeout, maxAttempts, password, poolConfig);
        this.prefix = prefix;
        this.preFetchCount = preFetchCount;

        fillingIds();
    }

    public RedisClusterIdGenerator(String clusterNodes, int connectionTimeout, int soTimeout, int maxAttempts, String password,
                                   GenericObjectPoolConfig poolConfig, String prefix, int preFetchCount, int idLen) {
        Validate.notNull(clusterNodes);
        String[] clusterNodesArray = clusterNodes.split(",");
        Set<HostAndPort> hostAndPorts = new HashSet<>();
        for (String clusterNode : clusterNodesArray) {
            hostAndPorts.add(HostAndPort.parseString(clusterNode));
        }
        this.jedisCluster = new JedisCluster(hostAndPorts, connectionTimeout, soTimeout, maxAttempts, password, poolConfig);
        this.prefix = prefix;
        this.preFetchCount = preFetchCount;
        this.idLen = idLen;

        fillingIds();
    }

    @Override
    protected void fillingIds() {
        Long result = jedisCluster.incrBy(getKey(prefix + COLON + KEY), preFetchCount);
        long id = result - preFetchCount;
        for (long i = id; i < id + preFetchCount; i++) {
            ids.offer(i);
        }
    }

    private String getKey(String key) {
        if (JedisClusterCRC16.getSlot(key) == 0) {
            key = key + SLOT_CHANGER;
        }
        return key;
    }
}
