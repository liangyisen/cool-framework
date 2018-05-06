package com.eiff.framework.test.runner;

import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

@RunWith(DefaultRunner.class)
@ContextConfiguration(locations = { "classpath:test_spring/default.xml" }, inheritLocations = true)
public class WithAutoTransactionalJUnit4SpringContextRunner {

}
