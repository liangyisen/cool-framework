package com.eiff.framework.cache.redis.cache;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:spring/spring-cache-anno.xml" }, inheritLocations = true)
public class Main {

    @Resource(name = "accountServiceBean")
    private AccountService accountService;

    @Test
    public void test() {

        // 第一次查询，应该走数据库
        System.out.println("first query...");
        accountService.getAccountByName("somebody");
        // 第二次查询，应该不查数据库，直接返回缓存的值
        System.out.println("second query...");
        accountService.getAccountByName("somebody");
        System.out.println();
    }
}
