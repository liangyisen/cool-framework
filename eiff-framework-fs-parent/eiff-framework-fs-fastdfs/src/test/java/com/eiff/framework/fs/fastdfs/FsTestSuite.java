package com.eiff.framework.fs.fastdfs;

import org.junit.Test;

import junit.extensions.RepeatedTest;
import junit.framework.JUnit4TestAdapter;
import junit.framework.TestResult;
import junit.framework.TestSuite;

public class FsTestSuite {

	@Test
	public void testMoreThanOnce() {
		TestResult testResult = new TestResult();
		TestSuite suite = new TestSuite("Test More than once");
		suite.addTest(new RepeatedTest(new JUnit4TestAdapter(FsTest.class), 200));
		suite.run(testResult);

		System.out.println(testResult.failureCount());
	}
}
