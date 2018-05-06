package com.eiff.framework.test.runner;

import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import com.eiff.framework.test.annotation.AutoMockComponent;

@RunWith(DefaultRunner.class)
@AutoMockComponent()
@ContextConfiguration(locations = { "classpath:test_spring/default.xml" }, inheritLocations = true)
public class WithAutoDefaultRunner {

}
