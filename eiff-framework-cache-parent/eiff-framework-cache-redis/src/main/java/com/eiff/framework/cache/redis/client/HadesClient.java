package com.eiff.framework.cache.redis.client;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.*;

import com.eiff.framework.cache.redis.common.RedisKeyUtils;
import com.eiff.framework.cache.redis.common.AssertUtils;
import com.eiff.framework.cache.redis.common.SerializeUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.util.CollectionUtils;
import redis.clients.jedis.BinaryClient.LIST_POSITION;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;
import redis.clients.util.JedisClusterCRC16;

/**
 * @author tangzhaowei
 */
public class HadesClient {

    private final static String SLOT_CHANGER = "{Qi}";

    private JedisCluster jedisCluster = null;

    private String keyPrefix = null;

    public HadesClient(JedisCluster jedisCluster, String module1, String... otherModules) {
        this.jedisCluster = jedisCluster;
        this.keyPrefix = RedisKeyUtils.keyBuilder(module1, StringUtils.EMPTY, otherModules);
    }

    public HadesClient(String clusterNodes, int connectionTimeout, int soTimeout, int maxAttempts, String password,
                       GenericObjectPoolConfig poolConfig, String module1, String... otherModules) {
        Validate.notNull(clusterNodes);
        String[] clusterNodesArray = clusterNodes.split(",");
        Set<HostAndPort> hostAndPorts = new HashSet<>();
        for (String clusterNode : clusterNodesArray) {
            hostAndPorts.add(HostAndPort.parseString(clusterNode));
        }
        this.jedisCluster = new JedisCluster(hostAndPorts, connectionTimeout, soTimeout, maxAttempts, password, poolConfig);
        this.keyPrefix = RedisKeyUtils.keyBuilder(module1, StringUtils.EMPTY, otherModules);
    }

    public void close() throws IOException{
        if (jedisCluster != null) {
            jedisCluster.close();
        }
    }

    public static interface HadesHandler<T> {
        /**
         * callback
         * @param jedisCluster jedisCluster
         * @return T
         * @throws Exception Exception
         */
        public T callback(JedisCluster jedisCluster) throws Exception;
    }

    public <T> T handle(HadesHandler<T> handler) throws Exception {

        return handler.callback(jedisCluster);
    }

    /**
     * 向redis存入key和value 如果key已经存在 则覆盖
     */
    public void put(String key, String value) throws Exception {
        Validate.notBlank(key);
        Validate.notNull(value);

        jedisCluster.set(getKey(key), value);
    }

    /**
     * 通过key获取储存在redis中的value
     */
    public String get(String key) throws Exception {
        Validate.notBlank(key);

        return jedisCluster.get(getKey(key));
    }

    /**
     * 设置key的过期时间
     */
    public Long expire(String key, int seconds) throws Exception {
        Validate.notBlank(key);
        AssertUtils.lessThan1(seconds);

        return jedisCluster.expire(getKey(key), seconds);
    }

    /**
     * 写入对象
     */
    public void putSerializableObj(String key, Serializable obj) throws Exception {
        Validate.notBlank(key);
        Validate.notNull(obj);

        jedisCluster.set((getKey(key)).getBytes(StandardCharsets.UTF_8), SerializeUtils.serialize(obj));
    }

    /**
     * 写入对象并制定这个键值的有效期
     */
    public void putSerializableObj(String key, Serializable obj, int seconds) throws Exception {
        Validate.notBlank(key);
        Validate.notNull(obj);
        AssertUtils.lessThan1(seconds);

        jedisCluster.setex((getKey(key)).getBytes(StandardCharsets.UTF_8), seconds, SerializeUtils.serialize(obj));
    }

    /**
     * 读取对象
     *
     * @return 成功返回value
     *
     */
    public Object getSerializableObj(String key) throws Exception {
        Validate.notBlank(key);

        byte[] value = jedisCluster.get((getKey(key)).getBytes(StandardCharsets.UTF_8));
        if (ArrayUtils.isNotEmpty(value)) {
            return SerializeUtils.unserialize(value);
        }
        return null;
    }

    /**
     * 删除指定的key,也可以传入一个包含key的数组
     *
     * @param keys
     *            一个key 也可以使 string 数组
     * @return 返回删除成功的个数
     */
    public Long del(String... keys) throws Exception {
        Validate.notEmpty(keys);

        Long success = 0L;
        for (String key : keys) {
            success += jedisCluster.del(getKey(key));
        }
        return success;
    }

    /**
     * 通过key向指定的value值追加值
     */
    public Long append(String key, String value) throws Exception {
        Validate.notBlank(key);
        Validate.notNull(value);

        return jedisCluster.append(getKey(key), value);
    }

    /**
     * 判断key是否存在
     */
    public Boolean exists(String key) throws Exception {
        Validate.notBlank(key);

        return jedisCluster.exists(getKey(key));
    }

    /**
     * 设置key value,如果key已经存在则返回0,nx==> not exist
     */
    public Long setnx(String key, String value) throws Exception {
        Validate.notBlank(key);
        Validate.notNull(value);

        return jedisCluster.setnx(getKey(key), value);
    }

    public Long setnx(String key, String value, int seconds) throws Exception {
        Validate.notBlank(key);
        Validate.notNull(value);
        AssertUtils.lessThan1(seconds);

        Long result = jedisCluster.setnx(getKey(key), value);
        jedisCluster.expire(getKey(key), seconds);
        return result;
    }

    /**
     * 设置key value并制定这个键值的有效期
     */
    public String setex(String key, String value, int seconds) throws Exception {
        Validate.notBlank(key);
        Validate.notNull(value);
        AssertUtils.lessThan1(seconds);

        return jedisCluster.setex(getKey(key), seconds, value);
    }

    /**
     * 通过key 和offset 从指定的位置开始将原先value替换 下标从0开始,offset表示从offset下标开始替换
     * 如果替换的字符串长度过小则会这样 example: value : abcdefghi str : abc 从下标3开始替换 则结果为
     * RES : abcabcghi
     */
    public Long setrange(String key, int offset, String str) throws Exception {
        Validate.notBlank(key);
        AssertUtils.lessThan0(offset);
        Validate.notNull(str);

        return jedisCluster.setrange(getKey(key), offset, str);
    }

    /**
     * 通过批量的key获取批量的value
     *
     * @param keys
     *            string数组 也可以是一个key
     * @return 成功返回value的集合, 失败返回空集合
     */
    public List<String> mget(String... keys) throws Exception {
        Validate.notEmpty(keys);

        List<String> result = new ArrayList<>();
        for (String key : keys) {
            result.add(jedisCluster.get(getKey(key)));
        }
        return result;
    }

    /**
     * 设置key的值,并返回一个旧值
     */
    public String getset(String key, String value) throws Exception {
        Validate.notBlank(key);
        Validate.notNull(value);

        return jedisCluster.getSet(getKey(key), value);
    }

    /**
     * 通过下标 和key 获取指定下标位置的 value
     */
    public String getrange(String key, int startOffset, int endOffset) throws Exception {
        Validate.notBlank(key);

        return jedisCluster.getrange(getKey(key), startOffset, endOffset);
    }

    /**
     * 通过key 对value进行加值+1操作,当value不是int类型时会返回错误,当key不存在是则value为1
     */
    public Long incr(String key) throws Exception {
        Validate.notBlank(key);

        return jedisCluster.incr(getKey(key));
    }

    public Long increx(String key, int seconds) throws Exception {
        Validate.notBlank(key);
        AssertUtils.lessThan1(seconds);

        Long result = jedisCluster.incr(getKey(key));
        jedisCluster.expire(getKey(key), seconds);
        return result;
    }

    /**
     * 通过key给指定的value加值,如果key不存在,则这是value为该值
     */
    public Long incrBy(String key, long integer) throws Exception {
        Validate.notBlank(key);

        return jedisCluster.incrBy(getKey(key), integer);
    }

    /**
     * 对key的值做减减操作,如果key不存在,则设置key为-1
     */
    public Long decr(String key) throws Exception {
        Validate.notBlank(key);

        return jedisCluster.decr(getKey(key));
    }

    /**
     * 减去指定的值
     */
    public Long decrBy(String key, long integer) throws Exception {
        Validate.notBlank(key);

        return jedisCluster.decrBy(getKey(key), integer);
    }

    /**
     * 通过key获取value值的长度
     */
    public Long serlen(String key) throws Exception {
        Validate.notBlank(key);

        return jedisCluster.strlen(getKey(key));
    }

    /**
     * 通过key给field设置指定的值,如果key不存在,则先创建
     */
    public Long hset(String key, String field, String value) throws Exception {
        Validate.notBlank(key);
        Validate.notNull(field);
        Validate.notNull(value);

        return jedisCluster.hset(getKey(key), field, value);
    }

    /**
     * 通过key给field设置指定的值,如果key不存在则先创建,如果field已经存在,返回0
     */
    public Long hsetnx(String key, String field, String value) throws Exception {
        Validate.notBlank(key);
        Validate.notNull(field);
        Validate.notNull(value);

        return jedisCluster.hsetnx(getKey(key), field, value);
    }

    private byte[] getKey(byte[] k) {
        byte[] prefix = keyPrefix.getBytes();
        byte[] key = new byte[prefix.length + k.length];
        System.arraycopy(prefix, 0, key, 0, prefix.length);
        System.arraycopy(k, 0, key, prefix.length, k.length);
        if (JedisClusterCRC16.getSlot(key) == 0) {
            byte[] slotChanger = SLOT_CHANGER.getBytes();
            byte[] newKey = new byte[prefix.length + k.length + slotChanger.length];
            System.arraycopy(key, 0, newKey, 0, prefix.length + k.length);
            System.arraycopy(slotChanger, 0, newKey, prefix.length + k.length, slotChanger.length);
            return newKey;
        }
        return key;
    }

    private String getKey(String key) {
        key = keyPrefix + key;
        if (JedisClusterCRC16.getSlot(key) == 0) {
            key = key + SLOT_CHANGER;
        }
        return key;
    }

    public Long hsetnx(byte[] key, byte[] field, byte[] value) throws Exception {
        AssertUtils.notEmpty(key);
        AssertUtils.notEmpty(field);
        AssertUtils.notEmpty(value);

        return jedisCluster.hsetnx(getKey(key), field, value);
    }

    public Long del(byte[] key) throws Exception {
        AssertUtils.notEmpty(key);

        return jedisCluster.del(getKey(key));
    }

    public Set<byte[]> keys(byte[] key) throws Exception {
        AssertUtils.notEmpty(key);

        Set<String> keys = new HashSet<>();
        Map<String, JedisPool> clusterNodes = jedisCluster.getClusterNodes();
        for(String node : clusterNodes.keySet()){
            JedisPool jedisPool = clusterNodes.get(node);
            try (Jedis connection = jedisPool.getResource()) {
                keys.addAll(connection.keys(new String(getKey(key))));
            }
        }
        if (CollectionUtils.isEmpty(keys)) {
            return null;
        }

        Set<byte[]> result = new HashSet<>();
        for (String stringKey : keys) {
            result.add(stringKey.getBytes(StandardCharsets.UTF_8));
        }
        return result;
    }

    /**
     * ！！！
     * 慎用该方法。使用该方法时需要注意，由于byte array的equals方法和hashcode方法本质上都是在使用该数组的内存地址。
     * 因此使用map的get方法可能会出现取不到想要的数据的情况。
     * ！！！
     */
    public Map<byte[], byte[]> hgetAll(byte[] key) throws Exception {
        AssertUtils.notEmpty(key);

        return jedisCluster.hgetAll(getKey(key));
    }

    /**
     * 通过key同时设置 hash的多个field
     */
    public String hmset(String key, Map<String, String> hash) throws Exception {
        Validate.notBlank(key);
        Validate.notEmpty(hash);

        return jedisCluster.hmset(getKey(key), hash);
    }

    /**
     * 通过key 和 field 获取指定的 value
     */
    public String hget(String key, String field) throws Exception {
        Validate.notBlank(key);
        Validate.notNull(field);

        return jedisCluster.hget(getKey(key), field);
    }

    /**
     * 通过key 和 fields 获取指定的value 如果没有对应的value则返回null
     */
    public List<String> hmget(String key, String... fields) throws Exception {
        Validate.notBlank(key);
        Validate.notEmpty(fields);

        return jedisCluster.hmget(getKey(key), fields);
    }

    /**
     * 通过key给指定的field的value加上给定的值
     */
    public Long hincrby(String key, String field, long value) throws Exception {
        Validate.notBlank(key);
        Validate.notNull(field);

        return jedisCluster.hincrBy(getKey(key), field, value);
    }

    /**
     * 通过key和field判断是否有指定的value存在
     */
    public Boolean hexists(String key, String field) throws Exception {
        Validate.notBlank(key);
        Validate.notNull(field);

        return jedisCluster.hexists(getKey(key), field);
    }

    /**
     * 通过key返回field的数量
     */
    public Long hlen(String key) throws Exception {
        Validate.notBlank(key);

        return jedisCluster.hlen(getKey(key));
    }

    /**
     * 通过key 删除指定的 field
     */
    public Long hdel(String key, String... fields) throws Exception {
        Validate.notBlank(key);
        Validate.notEmpty(fields);

        return jedisCluster.hdel(getKey(key), fields);
    }

    /**
     * 通过key返回所有的field
     */
    public Set<String> hkeys(String key) throws Exception {
        Validate.notBlank(key);

        return jedisCluster.hkeys(getKey(key));
    }

    /**
     * 通过key返回所有和key有关的value
     */
    public List<String> hvals(String key) throws Exception {
        Validate.notBlank(key);

        return jedisCluster.hvals(getKey(key));
    }

    /**
     * 通过key获取所有的field和value
     */
    public Map<String, String> hgetall(String key) throws Exception {
        Validate.notBlank(key);

        return jedisCluster.hgetAll(getKey(key));
    }

    /**
     * 通过key向list头部添加字符串
     */
    public Long lpush(String key, String... strs) throws Exception {
        Validate.notBlank(key);
        Validate.notEmpty(strs);

        return jedisCluster.lpush(getKey(key), strs);
    }

    /**
     * 通过key向list尾部添加字符串
     */
    public Long rpush(String key, String... strs) throws Exception {
        Validate.notBlank(key);
        Validate.notEmpty(strs);

        return jedisCluster.rpush(getKey(key), strs);
    }

    /**
     * 通过key在list指定的位置之前或者之后 添加字符串元素
     */
    public Long linsert(String key, LIST_POSITION where, String pivot, String value) throws Exception {
        Validate.notBlank(key);
        Validate.notNull(where);
        Validate.notNull(pivot);
        Validate.notNull(value);

        return jedisCluster.linsert(getKey(key), where, pivot, value);
    }

    /**
     * 通过key设置list指定下标位置的value 如果下标超过list里面value的个数则报错
     */
    public String lset(String key, long index, String value) throws Exception {
        Validate.notBlank(key);
        Validate.notNull(value);

        return jedisCluster.lset(getKey(key), index, value);
    }

    /**
     * 通过key从对应的list中删除指定的count个 和 value相同的元素
     */
    public Long lrem(String key, long count, String value) throws Exception {
        Validate.notBlank(key);
        Validate.notNull(value);

        return jedisCluster.lrem(getKey(key), count, value);
    }

    /**
     * 通过key保留list中从strat下标开始到end下标结束的value值
     */
    public String ltrim(String key, long start, long end) throws Exception {
        Validate.notBlank(key);

        return jedisCluster.ltrim(getKey(key), start, end);
    }

    /**
     * 通过key从list的头部删除一个value,并返回该value
     */
    public String lpop(String key) throws Exception {
        Validate.notBlank(key);

        return jedisCluster.lpop(getKey(key));
    }

    /**
     * 通过key从list尾部删除一个value,并返回该元素
     */
    public String rpop(String key) throws Exception {
        Validate.notBlank(key);

        return jedisCluster.rpop(getKey(key));
    }

    /**
     * 通过key从一个list的尾部删除一个value并添加到另一个list的头部,并返回该value 如果第一个list为空或者不存在则返回null
     */
    public String rpoplpush(String srckey, String dstkey) throws Exception {
        Validate.notBlank(srckey);
        Validate.notBlank(dstkey);

        String str = rpop(srckey);
        if (str == null) {
            return null;
        }
        lpush(dstkey, str);
        return str;
    }

    /**
     * 通过key获取list中指定下标位置的value
     */
    public String lindex(String key, long index) throws Exception {
        Validate.notBlank(key);

        return jedisCluster.lindex(getKey(key), index);
    }

    /**
     * 通过key返回list的长度
     */
    public Long llen(String key) throws Exception {
        Validate.notBlank(key);

        return jedisCluster.llen(getKey(key));
    }

    /**
     * 通过key获取list指定下标位置的value 如果start 为 0 end 为 -1 则返回全部的list中的value
     */
    public List<String> lrange(String key, long start, long end) throws Exception {
        Validate.notBlank(key);

        return jedisCluster.lrange(getKey(key), start, end);
    }

    /**
     * 通过key向指定的set中添加value
     */
    public Long sadd(String key, String... members) throws Exception {
        Validate.notBlank(key);
        Validate.notEmpty(members);

        return jedisCluster.sadd(getKey(key), members);
    }

    /**
     * 通过key删除set中对应的value值
     */
    public Long srem(String key, String... members) throws Exception {
        Validate.notBlank(key);
        Validate.notEmpty(members);

        return jedisCluster.srem(getKey(key), members);
    }

    /**
     * 通过key随机删除一个set中的value并返回该值
     */
    public String spop(String key) throws Exception {
        Validate.notBlank(key);

        return jedisCluster.spop(getKey(key));
    }

    /**
     * 通过key获取set中的差集 以第一个set为标准
     */
    public Set<String> sdiff(String... keys) throws Exception {
        Validate.notEmpty(keys);

        Set<String> result = jedisCluster.smembers(getKey(keys[0]));
        for (int i = 1; i < keys.length; i++) {
            result.removeAll(jedisCluster.smembers(getKey(keys[i])));
        }
        return result;
    }

    /**
     * 通过key获取set中的差集并存入到另一个key中 以第一个set为标准
     */
    public Long sdiffstore(String dstkey, String... keys) throws Exception {
        Validate.notBlank(dstkey);
        Validate.notEmpty(keys);

        Set<String> result = sdiff(keys);
        jedisCluster.del(getKey(dstkey));
        if (!result.isEmpty()) {
            jedisCluster.sadd(getKey(dstkey), result.toArray(new String[result.size()]));
        }
        return (long) result.size();
    }

    /**
     * 通过key获取指定set中的交集
     */
    public Set<String> sinter(String... keys) throws Exception {
        Validate.notEmpty(keys);

        Set<String> result = jedisCluster.smembers(getKey(keys[0]));
        for (int i = 1; i < keys.length; i++) {
            result.retainAll(jedisCluster.smembers(getKey(keys[i])));
        }
        return result;
    }

    /**
     * 通过key获取指定set中的交集 并将结果存入新的set中
     */
    public Long sinterstore(String dstkey, String... keys) throws Exception {
        Validate.notBlank(dstkey);
        Validate.notEmpty(keys);

        Set<String> result = sinter(keys);
        jedisCluster.del(getKey(dstkey));
        if (!result.isEmpty()) {
            jedisCluster.sadd(getKey(dstkey), result.toArray(new String[result.size()]));
        }
        return (long) result.size();
    }

    /**
     * 通过key返回所有set的并集
     */
    public Set<String> sunion(String... keys) throws Exception {
        Validate.notEmpty(keys);

        Set<String> result = jedisCluster.smembers(getKey(keys[0]));
        for (int i = 1; i < keys.length; i++) {
            result.addAll(jedisCluster.smembers(getKey(keys[i])));
        }
        return result;
    }

    /**
     * 通过key返回所有set的并集,并存入到新的set中
     */
    public Long sunionstore(String dstkey, String... keys) throws Exception {
        Validate.notBlank(dstkey);
        Validate.notEmpty(keys);

        Set<String> result = sunion(keys);
        jedisCluster.del(getKey(dstkey));
        if (!result.isEmpty()) {
            jedisCluster.sadd(getKey(dstkey), result.toArray(new String[result.size()]));
        }
        return (long) result.size();
    }

    /**
     * 通过key将set中的value移除并添加到第二个set中
     */
    public Long smove(String srckey, String dstkey, String member) throws Exception {
        Validate.notBlank(srckey);
        Validate.notBlank(dstkey);
        Validate.notNull(member);

        if (jedisCluster.sismember(getKey(srckey), member)) {
            jedisCluster.srem(getKey(srckey), member);
            jedisCluster.sadd(getKey(dstkey), member);
            return 1L;
        } else {
            return 0L;
        }
    }

    /**
     * 通过key获取set中value的个数
     */
    public Long scard(String key) throws Exception {
        Validate.notBlank(key);

        return jedisCluster.scard(getKey(key));
    }

    /**
     * 通过key判断value是否是set中的元素
     */
    public Boolean sismember(String key, String member) throws Exception {
        Validate.notBlank(key);
        Validate.notNull(member);

        return jedisCluster.sismember(getKey(key), member);
    }

    /**
     * 通过key获取set中随机的value,不删除元素
     */
    public String srandmember(String key) throws Exception {
        Validate.notBlank(key);

        return jedisCluster.srandmember(getKey(key));
    }

    /**
     * 通过key获取set中所有的value
     */
    public Set<String> smembers(String key) throws Exception {
        Validate.notBlank(key);

        return jedisCluster.smembers(getKey(key));
    }

    /**
     * 通过key向zset中添加value,score,其中score就是用来排序的 如果该value已经存在则根据score更新元素
     */
    public Long zadd(String key, Map<String, Double> scoreMembers) throws Exception {
        Validate.notBlank(key);
        Validate.notEmpty(scoreMembers);

        return jedisCluster.zadd(getKey(key), scoreMembers);
    }

    /**
     * 通过key向zset中添加value,score,其中score就是用来排序的 如果该value已经存在则根据score更新元素
     */
    public Long zadd(String key, double score, String member) throws Exception {
        Validate.notBlank(key);
        Validate.notNull(member);

        return jedisCluster.zadd(getKey(key), score, member);
    }

    /**
     * 通过key删除在zset中指定的value
     */
    public Long zrem(String key, String... members) throws Exception {
        Validate.notBlank(key);
        Validate.notEmpty(members);

        return jedisCluster.zrem(getKey(key), members);
    }

    /**
     * 通过key增加该zset中value的score的值
     */
    public Double zincrby(String key, double score, String member) throws Exception {
        Validate.notBlank(key);
        Validate.notNull(member);

        return jedisCluster.zincrby(getKey(key), score, member);
    }

    /**
     * 通过key返回zset中value的排名 下标从小到大排序
     */
    public Long zrank(String key, String member) throws Exception {
        Validate.notBlank(key);
        Validate.notNull(member);

        return jedisCluster.zrank(getKey(key), member);
    }

    /**
     * 通过key返回zset中value的排名 下标从大到小排序
     */
    public Long zrevrank(String key, String member) throws Exception {
        Validate.notBlank(key);
        Validate.notNull(member);

        return jedisCluster.zrevrank(getKey(key), member);
    }

    /**
     * 通过key将获取score从start到end中zset的value socre从大到小排序 当start为0 end为-1时返回全部
     */
    public Set<String> zrevrange(String key, long start, long end) throws Exception {
        Validate.notBlank(key);

        return jedisCluster.zrevrange(getKey(key), start, end);
    }

    /**
     * 通过key返回指定score内zset中的value
     */
    public Set<String> zrangebyscore(String key, String max, String min) throws Exception {
        Validate.notBlank(key);
        Validate.notNull(min);
        Validate.notNull(max);

        return jedisCluster.zrangeByScore(getKey(key), min, max);
    }

    public Set<String> zrangebyscore(String key, String max, String min, int offset, int count) throws Exception {
        Validate.notBlank(key);
        Validate.notNull(min);
        Validate.notNull(max);

        return jedisCluster.zrangeByScore(getKey(key), min, max, offset, count);
    }

    /**
     * 通过key返回指定score内zset中的value
     */
    public Set<String> zrangeByScore(String key, double max, double min) throws Exception {
        Validate.notBlank(key);

        return jedisCluster.zrangeByScore(getKey(key), min, max);
    }

    public Set<String> zrangeByScore(String key, double max, double min, int offset, int count) throws Exception {
        Validate.notBlank(key);

        return jedisCluster.zrangeByScore(getKey(key), min, max, offset, count);
    }

    /**
     * 返回指定区间内zset中value的数量
     */
    public Long zcount(String key, String min, String max) throws Exception {
        Validate.notBlank(key);
        Validate.notNull(min);
        Validate.notNull(max);

        return jedisCluster.zcount(getKey(key), min, max);
    }

    /**
     * 通过key返回zset中的value个数
     */
    public Long zcard(String key) throws Exception {
        Validate.notBlank(key);

        return jedisCluster.zcard(getKey(key));
    }

    /**
     * 通过key获取zset中value的score值
     */
    public Double zscore(String key, String member) throws Exception {
        Validate.notBlank(key);
        Validate.notNull(member);

        return jedisCluster.zscore(getKey(key), member);
    }

    /**
     * 通过key删除给定区间内的元素
     */
    public Long zremrangeByRank(String key, long start, long end) throws Exception {
        Validate.notBlank(key);

        return jedisCluster.zremrangeByRank(getKey(key), start, end);
    }

    /**
     * 通过key删除指定score内的元素
     */
    public Long zremrangeByScore(String key, double start, double end) throws Exception {
        Validate.notBlank(key);

        return jedisCluster.zremrangeByScore(getKey(key), start, end);
    }

    /**
     * 通过key判断值得类型
     */
    public String type(String key) throws Exception {
        Validate.notBlank(key);

        return jedisCluster.type(getKey(key));
    }

    public String getKeyPrefix() {
        return keyPrefix;
    }

    protected JedisCluster getJedisCluster() {
        return jedisCluster;
    }
}

