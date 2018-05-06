package com.eiff.framework.fs.fastdfs;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.eiff.framework.fs.fastdfs.client.HfsClient;
import com.eiff.framework.fs.fastdfs.client.HfsClientInterface;
import com.eiff.framework.fs.fastdfs.common.MyException;
import com.eiff.framework.fs.fastdfs.common.NotOverridableException;
import com.eiff.framework.fs.fastdfs.pool.conf.BasePoolConfig;
import com.eiff.framework.fs.fastdfs.token.AntiStealToken;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:spring/applicationContext-fastdfs.xml" })
public class FsPoolTest {

	@Autowired
	private HfsClientInterface client;

	private HfsClientInterface client_qa;

	public static final String SYSTEM_ID = "1";
	public static final String BASE_Path = FsTest.class.getResource("/").getPath();
	public static final String FILE_1 = BASE_Path + "/test1.txt";
	public static final String FILE_2 = BASE_Path + "/test2.txt";

	@Before
	public void setUp() throws Exception {
		BasePoolConfig stPoolConfig = new BasePoolConfig();
		stPoolConfig.setMaxTotalPerKey(3);
		stPoolConfig.setMaxIdlePerKey(1);
		stPoolConfig.setMaxWaitMillis(1000);
		stPoolConfig.setPoolName("TestPool");

		HfsClient hfsClientQa = new HfsClient(15000, 30000, "utf-8",
				new String[] { "172.16.57.19:22122", "172.16.57.20:22122" }, 8080, false);
		hfsClientQa.setStorageReadWritePoolConfig(stPoolConfig);
		hfsClientQa.setTrackerPoolConfig(stPoolConfig);
		client_qa = hfsClientQa;
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testUpload() {
		for (int i = 0; i < 5; i++) {
			try {
				long start = System.currentTimeMillis();
				String uploadFileName = client.upload(FILE_1, FILE_1, SYSTEM_ID);
				uploadFileName = client.upload(FILE_1, FILE_1, SYSTEM_ID);
				byte[] fileBytes = client.getFile(uploadFileName);
				System.out.println(uploadFileName);
				System.out.println(new String(fileBytes));
				client.downloadFile(uploadFileName, "down_load_file");
				client.deleteFile(uploadFileName);
				long end = System.currentTimeMillis();
				System.out.println(Thread.currentThread().getName() + " cost-err " + (end - start));
			} catch (Exception e) {
				e.printStackTrace();
			}

			try {
				String uploadFileName = client_qa.upload(FILE_1, FILE_1, SYSTEM_ID);
				byte[] fileBytes = client_qa.getFile(uploadFileName);
				System.out.println(uploadFileName);
				System.out.println(new String(fileBytes));

				client_qa.downloadFile(uploadFileName, "down_load_file");
				TimeUnit.SECONDS.sleep(1);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Test
	public void testGetFileInfo() throws IOException, MyException {
		String file_id = "group1/M00/01/29/rBA5FlmSrSOAOMjQAAAAMEn_gqQ994.jpg";
		System.out.println("file content: " + client.getFileInfo(file_id));
	}

	@Test
	public void testDeleteFile() throws IOException {
		String file_id = "group1/M00/01/23/wKg4Z1cB53SAMBTxAAAABmr8bp0316.txt";
		System.out.println(client.deleteFile(file_id));
	}

	@Test
	public void testUploadOverridable() throws Exception {
		String fileId = client.uploadOverridable(FILE_1, FILE_1, SYSTEM_ID);
		System.out.println(fileId);
		System.out.println("content: " + new String(client.getFile(fileId)));
		System.out.println("delete: " + client.deleteFile(fileId));
		System.out.println("===================================");
		fileId = client.uploadOverridable("this is the byte test".getBytes(), FILE_1, SYSTEM_ID);
		System.out.println(fileId);
		System.out.println("content: " + new String(client.getFile(fileId)));
		System.out.println("delete: " + client.deleteFile(fileId));
		if (client.getFile(fileId) == null) {
			System.out.println("file is deleted");
		} else {
			System.out.println("deleted content: " + new String(client.getFile(fileId)));
		}
		System.out.println();
	}

	@Test
	public void testUpdateFile() throws Exception {
		String fileId = client.uploadOverridable(FILE_1, FILE_1, SYSTEM_ID);
		String fileContent = "this is not txt1";
		System.out.println(fileId);
		System.out.println("content: " + new String(client.getFile(fileId)));
		System.out.println("prepare update file content to :" + fileContent);
		client.modifyOverridable(fileId, fileContent.getBytes());
		System.out.println("updated file content :" + new String(client.getFile(fileId)));
		System.out.println("delete: " + client.deleteFile(fileId));

		fileId = client.upload(FILE_1, FILE_2, SYSTEM_ID);
		try {
			client.modifyOverridable(fileId, fileContent.getBytes());
		} catch (NotOverridableException e) {
			client.deleteFile(fileId);
			System.out.println("file is deleted ");
			System.out.println();
		} catch (Exception e) {
			client.deleteFile(fileId);
			System.out.println("file is deleted ");
			System.out.println();
		}

	}

	@Test
	public void testToken() throws NoSuchAlgorithmException, MyException, IOException {
		String fileName1 = "/group1/M00/01/33/wKg4Z1lxwpeADLyRAAAAK6dWlxU857.txt";
		String fileName2 = "group1/M00/00/00/wKgH3Vai9xKALsdwAAAkd6DhCjc494.xml";
		System.out.println(new String(client.getFile(fileName2)));
		AntiStealToken antiStealToken1 = client.getAntiStealToken(fileName1);
		AntiStealToken antiStealToken2 = client.getAntiStealToken(fileName2);

		System.out.println("http://192.168.56.103/" + fileName1 + "?" + antiStealToken1.getTokenUrlParam());
		System.out.println("http://192.168.56.103/" + fileName2 + "?" + antiStealToken2.getTokenUrlParam());
	}
}
