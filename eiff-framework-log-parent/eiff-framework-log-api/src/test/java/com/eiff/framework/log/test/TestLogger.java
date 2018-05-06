package com.eiff.framework.log.test;

import org.junit.Test;

import com.eiff.framework.log.api.HdLogger;

public class TestLogger {
	
	@Test
	public void testWrite(){
		HdLogger hdLogger = HdLogger.getLogger(TestLogger.class);
		hdLogger.info("123");
	}
}
