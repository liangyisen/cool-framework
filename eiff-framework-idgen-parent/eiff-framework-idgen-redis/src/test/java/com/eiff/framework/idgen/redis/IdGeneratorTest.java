package com.eiff.framework.idgen.redis;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:spring-cache-anno.xml" }, inheritLocations = true)
public class IdGeneratorTest {

    @Resource(name = "redisClusterIdGenerator")
    private RedisClusterIdGenerator redisClusterIdGenerator;

    @Test
    public void test() throws IOException {

        ExecutorService pool = Executors.newFixedThreadPool(30);
        for (int i = 0; i < 2000; i++) {
            pool.submit(new MyThread(redisClusterIdGenerator));
        }
        System.in.read();
    }
}

class MyThread extends Thread {

    private RedisClusterIdGenerator redisClusterIdGenerator;

    public MyThread(RedisClusterIdGenerator redisClusterIdGenerator) {
        this.redisClusterIdGenerator = redisClusterIdGenerator;
    }

    @Override
    public void run() {
        long time = System.currentTimeMillis();
        System.out.println(redisClusterIdGenerator.genId() + "--" + (System.currentTimeMillis() - time));
    }
}
