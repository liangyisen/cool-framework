package com.eiff.framework.fs.ftp;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.eiff.framework.fs.sftp.log.InTraceSftpService;
import com.eiff.framework.fs.sftp.utils.ScpBean;
import com.eiff.framework.fs.sftp.utils.SftpMeta;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:spring/applicationContext-sftp.xml" })
public class SftpTest {

	@Autowired
	private InTraceSftpService inTraceSftpUtil;
	ScpBean scpBean = new ScpBean("172.16.57.19", 22, "work", "%TGB^YHN7ujm");

	@Test
	public void testUpload() {
		for (int i = 0; i < 1; i++) {
			String upload;
			String remoteFilePath = inTraceSftpUtil.convertToAbsolutePath("img");
			try {
				inTraceSftpUtil.createDir(scpBean, remoteFilePath);
				if (!inTraceSftpUtil.isDirExists(scpBean, remoteFilePath)) {
					inTraceSftpUtil.createDir(scpBean, remoteFilePath);
				}
				upload = inTraceSftpUtil.upload(scpBean, "img",
						"C:\\tmp\\test.jpg",
						true);
				System.out.println(upload);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("done");
		}
	}

	@Test
	public void testUploadWithMeta() {
		for (int i = 0; i < 1000; i++) {
			SftpMeta meta = SftpMeta.build().addFileName("test").addFileType("json").addExpiredTime(1000l);
			String upload;
			String remoteFilePath = inTraceSftpUtil.convertToAbsolutePath("a/b/c");
			try {
				if (!inTraceSftpUtil.isDirExists(scpBean, remoteFilePath)) {
					inTraceSftpUtil.createDir(scpBean, remoteFilePath);
				}
				upload = inTraceSftpUtil.upload(scpBean, "a/b/c",
						"E:\\git\\eif-middleware\\eif-middleware-monitor-fs\\target\\eif-middleware-monitor-fs-1.0.0-SNAPSHOT.jar.original",
						true, meta);
				System.out.println(upload);
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("done");
		}
	}

	@Test(expected = IOException.class)
	public void testExists() throws IOException {
		boolean exists = inTraceSftpUtil.exists(scpBean, inTraceSftpUtil.convertToAbsolutePath("a/b/c"));
		System.out.println(exists);

		exists = inTraceSftpUtil.isDirExists(scpBean, "/home/work/newlts/a/b/c");
		System.out.println(exists);

		exists = inTraceSftpUtil.isDirExists(scpBean,
				"/home/work/newlts/a/b/c/eif-middleware-monitor-fs-1.0.0-SNAPSHOT.jar.original/");
		System.out.println(exists);
	}
	
	
	@Test
	public void testUploadWithStream() throws IOException {
		System.out.println(inTraceSftpUtil.upload(scpBean, "a/b/c", "test.text", new ByteArrayInputStream(new String("abcdefg\\n\\1").getBytes()), true));
		System.out.println(inTraceSftpUtil.upload(scpBean, "a/b/c", "test2.text", new ByteArrayInputStream(new String("abcdefg\\n\\1").getBytes()), true, SftpMeta.build().addFileName("test").addFileType("json").addExpiredTime(1000l)));
	}
}
