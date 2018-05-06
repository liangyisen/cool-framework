package com.eiff.framework.gfs;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import com.eiff.framework.fs.glusterfs.client.HglusterfsClient;
import com.eiff.framework.fs.glusterfs.client.meta.Metadata;

public class TestHglusterfsClient {
	
	
	@Test
	public void upload() {
		HglusterfsClient client = new HglusterfsClient("/tmp", 10, 10);
		try {
			client.upload("adsda".getBytes(), "xx.txt", "eiftest");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void uploadWithMeta() {
		HglusterfsClient client = new HglusterfsClient("/tmp", 10, 10);
		try {
			client.upload("adsda".getBytes(), "xx.txt", "eiftest", Metadata.build().editExpire(200));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void getFile(){
		HglusterfsClient client = new HglusterfsClient("/tmp", 10, 10);
		try {
			String fileName = "/tmp/aabbcc/eiftest/26/09d3ea5f765c4b4bafe5f464104c00e1-xx.txt";
			if(client.exist(fileName)){
				System.out.println(new String(client.getFile("/tmp/aabbcc/eiftest/26/09d3ea5f765c4b4bafe5f464104c00e1-xx.txt")));
				client.deleteFile(fileName);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void deleteFile(String storedfileName) throws IOException{
		FileUtils.forceDeleteOnExit(new File(storedfileName));
	}
	
	public boolean exist(String storedfileName){
		return  new File(storedfileName).exists();
	}
	
}
