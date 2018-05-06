package com.eiff.framework.cache.redis;

import com.eiff.framework.cache.redis.client.HadesClient;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import redis.clients.jedis.BinaryClient;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.exceptions.JedisDataException;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:spring/spring-cache-anno.xml" }, inheritLocations = true)
public class HadesClientTest {

    @Resource(name = "hadesClient")
    private HadesClient hadesClient;

    @Test
    public void testGetPut() throws Exception {
        hadesClient.put("name", "jack");
        Assert.assertEquals(hadesClient.get("name"), "jack");
    }

    @Test
    public void testExpireExist() throws Exception {
        hadesClient.put("name", "jack");
        hadesClient.expire("name", 5);
        Thread.sleep(7000);
        Assert.assertFalse(hadesClient.exists("name"));
    }

    @Test
    public void testByteArray() throws Exception {
        hadesClient.putSerializableObj("byteArray", new GregorianCalendar());
        Assert.assertNotNull(hadesClient.getSerializableObj("byteArray"));
        hadesClient.putSerializableObj("byteArray", new GregorianCalendar(), 5);
        Thread.sleep(7000);
        Assert.assertFalse(hadesClient.exists("byteArray"));
    }

    @Test
    public void testDel() throws Exception {
        hadesClient.put("name", "jack");
        hadesClient.del("name");
        Assert.assertFalse(hadesClient.exists("name"));

        hadesClient.put("name", "jack");
        hadesClient.del("name".getBytes(StandardCharsets.UTF_8));
        Assert.assertFalse(hadesClient.exists("name"));
    }

    @Test
    public void testAppend() throws Exception {
        hadesClient.put("name", "jack");
        Assert.assertEquals(hadesClient.append("name", "tang").intValue(), 8);
        Assert.assertEquals(hadesClient.get("name"), "jacktang");
    }

    @Test
    public void testSetnx() throws Exception {
        hadesClient.del("name");
        Assert.assertEquals(hadesClient.setnx("name", "jack").intValue(), 1);
        Assert.assertEquals(hadesClient.setnx("name", "jack").intValue(), 0);

        hadesClient.del("name");
        Assert.assertEquals(hadesClient.setnx("name", "jack", 5).intValue(), 1);
        Thread.sleep(3000);
        Assert.assertEquals(hadesClient.setnx("name", "jack").intValue(), 0);
        Thread.sleep(4000);
        Assert.assertEquals(hadesClient.setnx("name", "jack").intValue(), 1);
    }

    @Test
    public void testSetex() throws Exception {
        hadesClient.setex("name", "jack", 5);
        Assert.assertEquals(hadesClient.get("name"), "jack");
        Thread.sleep(7000);
        Assert.assertFalse(hadesClient.exists("name"));
    }

    @Test
    public void testSetRange() throws Exception {
        hadesClient.put("name", "jack");
        hadesClient.setrange("name", 2, "kkie");
        Assert.assertEquals(hadesClient.get("name"), "jakkie");
        hadesClient.setrange("name", 7, "aaa");
        Assert.assertEquals(hadesClient.get("name"), "jakkie\u0000aaa");
        hadesClient.del("name");
        hadesClient.setrange("name", 3, "aaa");
        Assert.assertEquals(hadesClient.get("name"), "\u0000\u0000\u0000aaa");
    }

    @Test
    public void testMGet() throws Exception {
        hadesClient.put("name", "jack");
        hadesClient.put("sex", "male");
        Assert.assertEquals(hadesClient.mget("name", "sex").get(0), "jack");
        Assert.assertEquals(hadesClient.mget("name", "sex").get(1), "male");
    }

    @Test
    public void testGetSet() throws Exception {
        hadesClient.put("name", "jack");
        Assert.assertEquals(hadesClient.getset("name", "jackie"), "jack");
        Assert.assertEquals(hadesClient.get("name"), "jackie");
    }

    @Test
    public void testGetRange() throws Exception {
        hadesClient.put("name", "jackie");
        Assert.assertEquals(hadesClient.getrange("name", 1, 3), "ack");
    }

    @Test
    public void testIncr() throws Exception {
        hadesClient.put("int", "10");
        Assert.assertEquals(hadesClient.incr("int").intValue(), 11);
        Assert.assertEquals(hadesClient.incrBy("int", 5).intValue(), 16);
        Assert.assertEquals(hadesClient.increx("int", 5).intValue(), 17);
        Thread.sleep(7000);
        Assert.assertFalse(hadesClient.exists("int"));
    }

    @Test
    public void testDecr() throws Exception {
        hadesClient.put("int", "10");
        Assert.assertEquals(hadesClient.decr("int").intValue(), 9);
        Assert.assertEquals(hadesClient.decrBy("int", 5).intValue(), 4);
    }

    @Test
    public void testStrLen() throws Exception {
        hadesClient.put("name", "jack");
        Assert.assertEquals(hadesClient.serlen("name").intValue(), 4);
    }

    @Test
    public void testHSet() throws Exception {
        hadesClient.hset("hash", "field", "value");
        Assert.assertEquals(hadesClient.hget("hash", "field"), "value");
        Assert.assertEquals(hadesClient.hsetnx("hash", "field", "value").intValue(), 0);
        Assert.assertEquals(hadesClient.hsetnx("hash".getBytes(), "field".getBytes(), "value".getBytes()).intValue(), 0);
    }

    @Test
    public void testKeys() throws Exception {
        hadesClient.put("name1", "jack");
        hadesClient.put("name2", "jackie");
        for (byte[] byteArray : hadesClient.keys("name*".getBytes())) {
            Assert.assertTrue(new String(byteArray).startsWith("test:name"));
        }
    }

    @Test
    public void testHMGet() throws Exception {
        hadesClient.del("hash");
        Map<String, String> map = new HashMap<>();
        map.put("field1", "value1");
        map.put("field2", "value2");
        hadesClient.hmset("hash", map);
        Map<String, String> result = hadesClient.hgetall("hash");
        Assert.assertEquals(result.get("field1"), "value1");
        Assert.assertEquals(result.get("field2"), "value2");

        Map<byte[], byte[]> byteResult = hadesClient.hgetAll("hash".getBytes());
        for (Map.Entry<byte[], byte[]> entry : byteResult.entrySet()) {
            Assert.assertTrue(Arrays.asList(new String[]{"field1", "field2"}).contains(new String(entry.getKey())));
            Assert.assertTrue(Arrays.asList(new String[]{"value1", "value2"}).contains(new String(entry.getValue())));
        }

        List<String> resultList = hadesClient.hmget("hash", "field1", "field2");
        Assert.assertEquals(resultList.get(0), "value1");
        Assert.assertEquals(resultList.get(1), "value2");
    }

    @Test
    public void testHGet() throws Exception {
        hadesClient.hset("hash", "field", "value");
        Assert.assertEquals(hadesClient.hget("hash", "field"), "value");
    }

    @Test
    public void testHIncrBy() throws Exception {
        hadesClient.hset("hash", "int", "10");
        Assert.assertEquals(hadesClient.hincrby("hash", "int", 5).intValue(), 15);
        hadesClient.hset("hash", "int", String.valueOf(Long.MAX_VALUE));
        try {
            hadesClient.hincrby("hash", "int", 5);
        } catch (JedisDataException e) {
            e.printStackTrace();
        }
        Assert.assertEquals(hadesClient.hget("hash", "int"), String.valueOf(Long.MAX_VALUE));
    }

    @Test
    public void testHMSet() throws Exception {
        Map<String, String> map = new HashMap<>();
        map.put("field1", "value1");
        map.put("field2", "value2");
        hadesClient.del("hash");
        hadesClient.hmset("hash", map);
        Assert.assertTrue(hadesClient.hexists("hash", "field1"));
        Assert.assertEquals(hadesClient.hlen("hash").intValue(), 2);
        hadesClient.hdel("hash", "field1");
        Assert.assertFalse(hadesClient.hexists("hash", "field1"));
    }

    @Test
    public void testHKeys() throws Exception {
        Map<String, String> map = new HashMap<>();
        map.put("field1", "value1");
        map.put("field2", "value2");
        hadesClient.del("hash");
        hadesClient.hmset("hash", map);
        Assert.assertTrue(hadesClient.hkeys("hash").contains("field1"));
        Assert.assertTrue(hadesClient.hkeys("hash").contains("field2"));
        Assert.assertTrue(hadesClient.hvals("hash").contains("value1"));
        Assert.assertTrue(hadesClient.hvals("hash").contains("value2"));
    }

    @Test
    public void testPush() throws Exception {
        hadesClient.del("list");
        hadesClient.lpush("list", "value1");
        hadesClient.rpush("list", "value2");
        Assert.assertEquals(hadesClient.lrange("list", 0, -1).size(), 2);
        Assert.assertEquals(hadesClient.lrange("list", 0, -1).get(0), "value1");
        Assert.assertEquals(hadesClient.lrange("list", 0, -1).get(1), "value2");
    }


    @Test
    public void testLInsert() throws Exception {
        hadesClient.del("list");
        hadesClient.lpush("list", "value1");
        hadesClient.rpush("list", "value2");
        hadesClient.linsert("list", BinaryClient.LIST_POSITION.AFTER, "value1", "value1.5");
        Assert.assertEquals(hadesClient.lrange("list", 0, -1).get(1), "value1.5");
        hadesClient.lset("list", 1, "value1.8");
        Assert.assertEquals(hadesClient.lrange("list", 0, -1).get(1), "value1.8");
        Assert.assertEquals(hadesClient.llen("list").intValue(), 3);
        hadesClient.lrem("list", 1, "value1.8");
        Assert.assertEquals(hadesClient.llen("list").intValue(), 2);
    }

    @Test
    public void testLPop() throws Exception {
        hadesClient.del("list");
        hadesClient.lpush("list", "value1");
        hadesClient.rpush("list", "value2");
        hadesClient.rpush("list", "value3");
        hadesClient.ltrim("list", 0, 1);
        Assert.assertEquals(hadesClient.llen("list").intValue(), 2);
        hadesClient.rpush("list", "value3");
        Assert.assertEquals(hadesClient.lpop("list"), "value1");
        Assert.assertEquals(hadesClient.llen("list").intValue(), 2);
        Assert.assertEquals(hadesClient.rpop("list"), "value3");
        Assert.assertEquals(hadesClient.llen("list").intValue(), 1);
    }

    @Test
    public void testRPopLPush() throws Exception {
        // hades 待确认，这里只pop了没有push
        hadesClient.del("list1", "list2");
        hadesClient.lpush("list1", "value1");
        hadesClient.lpush("list2", "value2");
        Assert.assertEquals(hadesClient.rpoplpush("list1", "list2"), "value1");
        Assert.assertEquals(hadesClient.llen("list1").intValue(), 0);
        Assert.assertEquals(hadesClient.llen("list2").intValue(), 2);
    }

    @Test
    public void testLIndex() throws Exception {
        hadesClient.del("list");
        hadesClient.lpush("list", "value1");
        hadesClient.rpush("list", "value2");
        Assert.assertEquals(hadesClient.lindex("list", 0),"value1");
    }

    @Test
    public void testSAdd() throws Exception {
        hadesClient.sadd("set", "value1");
        Assert.assertTrue(hadesClient.smembers("set").contains("value1"));
        hadesClient.srem("set", "value1");
        Assert.assertFalse(hadesClient.smembers("set").contains("value1"));
    }

    @Test
    public void testSPop() throws Exception {
        hadesClient.del("set");
        hadesClient.sadd("set", "value1");
        Assert.assertEquals(hadesClient.spop("set"), "value1");
        Assert.assertEquals(hadesClient.smembers("set").size(), 0);
    }

    @Test
    public void testSDiff() throws Exception {
        hadesClient.del("set1", "set2", "set3");
        hadesClient.sadd("set1", "value1");
        hadesClient.sadd("set2", "value2");
        Assert.assertEquals(hadesClient.sdiff("set1", "set2").size(), 1);
        Assert.assertTrue(hadesClient.sdiff("set1", "set2").contains("value1"));
        hadesClient.sdiffstore("set3", "set1", "set2");
        Assert.assertEquals(hadesClient.smembers("set3").size(), 1);
        Assert.assertTrue(hadesClient.smembers("set3").contains("value1"));
    }

    @Test
    public void testSInter() throws Exception {
        hadesClient.del("set1", "set2", "set3");
        hadesClient.sadd("set1", "value1", "value");
        hadesClient.sadd("set2", "value2", "value");
        Assert.assertEquals(hadesClient.sinter("set1", "set2").size(), 1);
        Assert.assertTrue(hadesClient.sinter("set1", "set2").contains("value"));
        hadesClient.sinterstore("set3", "set1", "set2");
        Assert.assertEquals(hadesClient.smembers("set3").size(), 1);
        Assert.assertTrue(hadesClient.smembers("set3").contains("value"));

        hadesClient.del("set1", "set2", "set3");
        hadesClient.sadd("set1", "value1", "value2", "value3");
        hadesClient.sadd("set2", "value1");
        hadesClient.sadd("set3", "value1", "value3");
        Assert.assertEquals(hadesClient.sinter("set1", "set2", "set3").size(), 1);
        Assert.assertTrue(hadesClient.sinter("set1", "set2", "set3").contains("value1"));
    }

    @Test
    public void testSUnion() throws Exception {
        hadesClient.del("set1", "set2", "set3");
        hadesClient.sadd("set1", "value1");
        hadesClient.sadd("set2", "value2");
        Assert.assertEquals(hadesClient.sunion("set1", "set2").size(), 2);
        Assert.assertTrue(hadesClient.sunion("set1", "set2").contains("value1"));
        Assert.assertTrue(hadesClient.sunion("set1", "set2").contains("value2"));
        hadesClient.sunionstore("set3", "set1", "set2");
        Assert.assertEquals(hadesClient.smembers("set3").size(), 2);
        Assert.assertTrue(hadesClient.smembers("set3").contains("value1"));
        Assert.assertTrue(hadesClient.smembers("set3").contains("value2"));
    }

    @Test
    public void testSMove() throws Exception {
        // hades 待确认，remove生效，add不生效
        hadesClient.del("set1", "set2");
        hadesClient.sadd("set1", "value1");
        hadesClient.sadd("set2", "value2");
        hadesClient.smove("set1", "set2", "value1");
        Assert.assertEquals(hadesClient.scard("set1").intValue(), 0);
        Assert.assertEquals(hadesClient.scard("set2").intValue(), 2);
        Assert.assertFalse(hadesClient.sismember("set1", "value1"));
        Assert.assertTrue(hadesClient.sismember("set2", "value1"));
    }

    @Test
    public void testSRandMember() throws Exception {
        hadesClient.del("set");
        hadesClient.sadd("set", "value1");
        Assert.assertEquals(hadesClient.srandmember("set"), "value1");
    }

    @Test
    public void testZAdd() throws Exception {
        Map<String, Double> map = new HashMap<>();
        map.put("value2", 2.0);
        hadesClient.del("zset");
        hadesClient.zadd("zset", 1, "value1");
        Assert.assertEquals(hadesClient.zcard("zset").intValue(), 1);
        hadesClient.zadd("zset", map);
        Assert.assertEquals(hadesClient.zcard("zset").intValue(), 2);
        hadesClient.zrem("zset", "value1");
        Assert.assertEquals(hadesClient.zcard("zset").intValue(), 1);
    }

    @Test
    public void testZIncrBy() throws Exception {
        hadesClient.del("zset");
        hadesClient.zadd("zset", 1, "value1");
        hadesClient.zincrby("zset", 1, "value1");
        Assert.assertEquals(hadesClient.zscore("zset", "value1").intValue(), 2);
    }

    @Test
    public void testZRank() throws Exception {
        hadesClient.del("zset");
        hadesClient.zadd("zset", 1, "value1");
        hadesClient.zadd("zset", 2, "value2");
        Assert.assertEquals(hadesClient.zrank("zset", "value1").intValue(), 0);
        Assert.assertEquals(hadesClient.zrevrank("zset", "value1").intValue(), 1);
    }

    @Test
    public void testZRange() throws Exception {
        hadesClient.del("zset");
        hadesClient.zadd("zset", 1, "value1");
        hadesClient.zadd("zset", 2, "value2");
        Assert.assertEquals(hadesClient.zrevrange("zset", 0, 0).size(), 1);
        Assert.assertEquals(hadesClient.zcount("zset", "0", "1").intValue(), 1); // 这里使用score的区间
        Assert.assertEquals(hadesClient.zrangebyscore("zset", "1", "0").size(), 1);
        Assert.assertEquals(hadesClient.zrangebyscore("zset", "1", "0", 0, 1).size(), 1);
        Assert.assertEquals(hadesClient.zrangeByScore("zset", 1, 0).size(), 1);
        Assert.assertEquals(hadesClient.zrangeByScore("zset", 1, 0, 0, 1).size(), 1);
    }

    @Test
    public void testZRem() throws Exception {
        hadesClient.del("zset");
        hadesClient.zadd("zset", 1, "value1");
        hadesClient.zadd("zset", 2, "value2");
        Assert.assertEquals(hadesClient.zremrangeByRank("zset", 1, 2).intValue(), 1);
        Assert.assertEquals(hadesClient.zremrangeByScore("zset", 1, 2).intValue(), 1);
    }

    @Test
    public void testType() throws Exception {
        hadesClient.del("name");
        hadesClient.put("name", "jack");
        Assert.assertEquals(hadesClient.type("name"), "string");
    }

    @Test
    public void testHandler() throws Exception {
        String string = hadesClient.handle(new HadesClient.HadesHandler<String>() {
            @Override
            public String callback(JedisCluster jedisCluster) throws Exception {
                jedisCluster.set("name", "jack");
                return jedisCluster.get("name");
            }
        });
        Assert.assertEquals(string, "jack");
    }
}
