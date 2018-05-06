package com.eiff.framework.fs.fastdfs.client;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;

import com.eiff.framework.fs.fastdfs.OriClient;
import com.eiff.framework.fs.fastdfs.common.MyException;
import com.eiff.framework.fs.fastdfs.conf.NonGlobalConfig;

public class OriClientTest {
	
	private OriClient client;
	
	@Before
	public void setUp() throws Exception {
		client = new OriClient(new NonGlobalConfig(15000, 30000, "utf-8", new String[] { "172.16.57.19:22122", "172.16.57.20:22122" },
				8080, false));
	}
	
	
	@Test
	public void upload(){
		try {
			String[] id = client.upload_file("C:\\tmp\\chitodo.png", "png", null);
			System.out.println(StringUtils.join(id, "/"));
		} catch (IOException | MyException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void uploadMster(){
		try {
			String[] ids = client.upload_file("group1", "M00/6C/C0/rBA5Flo6AXSAdaogAAApawFAty4975.png", "b", "C:\\tmp\\chitodo.png", "png", null);
			System.out.println(StringUtils.join(ids, "/"));
		} catch (IOException | MyException e) {
			e.printStackTrace();
		}
	}
}
