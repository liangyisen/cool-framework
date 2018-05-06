package com.eiff.framework.idgen.zk;

import com.eiff.framework.idgen.zk.impl.ZkId17Generator;
import org.apache.curator.test.TestingServer;
import org.junit.*;

import java.io.File;
import java.io.IOException;

public class ZkIdGeneratorTest {

    private TestingServer server;

    @Before
    public void setUp() throws Exception {
        server = new TestingServer(2181, (File)null, true);
        server.start();
    }
    @After
    public void tearDown() throws IOException {
        server.stop();
    }

    @Test
    public void testZkIdGenerator() {
        ZkId17Generator zkId17Generator = new ZkId17Generator("test", 500, "yyMMddHHmmss", "zktest", "127.0.0.1:2181");
        System.out.println(zkId17Generator.genId());
        System.out.println(zkId17Generator.genId());
        System.out.println(zkId17Generator.genId());
    }
}
