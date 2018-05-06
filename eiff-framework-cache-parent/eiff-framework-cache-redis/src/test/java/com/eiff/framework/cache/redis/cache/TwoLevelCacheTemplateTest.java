package com.eiff.framework.cache.redis.cache;

import com.eiff.framework.cache.redis.spring.TwoLevelCacheTemplate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.io.IOException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:spring/spring-cache-anno.xml" }, inheritLocations = true)
public class TwoLevelCacheTemplateTest {

    @Resource(name = "twoLevelCacheTemplate")
    private TwoLevelCacheTemplate twoLevelCacheTemplate;

    @Test
    public void test() throws IOException {
        twoLevelCacheTemplate.set("testCache", "name", "jack");
        System.out.println(twoLevelCacheTemplate.get("testCache", "name"));
    }
}
