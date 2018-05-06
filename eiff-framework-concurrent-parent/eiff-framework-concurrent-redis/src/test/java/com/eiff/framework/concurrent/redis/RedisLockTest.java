package com.eiff.framework.concurrent.redis;

import java.io.IOException;

import javax.annotation.Resource;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.eiff.framework.concurrent.redis.lock.RedisConcurrentLock;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:spring/spring-cache-anno.xml" }, inheritLocations = true)
public class RedisLockTest {

    @Resource(name = "redisConcurrentLock")
    private RedisConcurrentLock redisConcurrentLock;

    @Test
    public void testConcurrentController() throws IOException, InterruptedException {
        final String cocurrentkey = "hello";

        for (int i = 0; i < 50; i++) {
            new Thread(new Runnable() {

                @Override
                public void run() {
                    // testTryAcquire(cocurrentkey);

                    //testAcquireUninterruptibly(cocurrentkey);

                    try {
                        testAcquire(cocurrentkey);
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                }
            }).start();
        }

        new Thread(new Runnable() {

            @Override
            public void run() {
                for (int i = 0; i < 10; i++) {
                    try {
                        Thread.sleep(3000);
                        System.out.println("############################" + redisConcurrentLock.isLocked(cocurrentkey));
                    } catch (InterruptedException e) {
                    }
                }

            }
        }).start();

        try {
            Thread.sleep(300000000);
        } catch (InterruptedException e) {
        }

    }

    private void testAcquire(final String cocurrentkey) throws Exception {

        redisConcurrentLock.lockInterruptibly(cocurrentkey, 3000);

        // do something
        try {
            System.out.println(Thread.currentThread().getName() + " got lockInterruptibly, do something");
            Thread.sleep(2000);
        } catch (Exception e) {
        } finally {
            // unlock lockInterruptibly
            redisConcurrentLock.unlock(cocurrentkey);
            System.out.println(Thread.currentThread().getName() + " released lockInterruptibly.>>>>>>>>>>>>>>>>"
                    + redisConcurrentLock.isLocked(cocurrentkey));
        }

    }

    @Test
    public void testRedLock() throws InterruptedException {

        for (int i = 0; i < 10; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    boolean result = false;
                    try {
                        result = redisConcurrentLock.tryLock("lockInterruptibly", 5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println(Thread.currentThread().getName() + ":" + result);
                }
            }).start();
        }
        Thread.sleep(1000);
        Assert.assertFalse(redisConcurrentLock.tryLock("lockInterruptibly", 5000));
        Thread.sleep(10000);
        Assert.assertTrue(redisConcurrentLock.tryLock("lockInterruptibly", 5000));
        redisConcurrentLock.unlock("lockInterruptibly");
    }

}
