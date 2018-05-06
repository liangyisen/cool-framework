package com.eiff.framework.fs.sftp.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.SftpProgressMonitor;

/**
 * 
 */
public class SftpUtil {
	private static Logger logger = LoggerFactory.getLogger(SftpUtil.class);

	/**
	 * sftp上传文件
	 * 
	 * @param bean
	 *            登陆远程服务器配置信息
	 * @param remoteFilePath
	 *            远程文件地址，绝对地址
	 * @param localFilePath
	 *            本地文件地址，绝对地址
	 * @param type
	 *            上传模式，true：OVERWRITE模式，false：RESUME
	 * @return
	 */
	public static boolean upload(ScpBean bean, String remoteFilePath, String localFilePath, boolean type) {
		if (StringUtils.isBlank(remoteFilePath)) {
			logger.warn("远程文件地址不能为空");
			return false;
		}
		if (StringUtils.isBlank(localFilePath)) {
			logger.warn("本地文件地址为空");
			return false;
		}
		if (!bean.validate()) {
			logger.warn("登陆远程服务器配置信息校验失败");
			return false;
		}
		if (remoteFilePath != null && !(remoteFilePath.endsWith("/") || remoteFilePath.endsWith("\\"))) {
			remoteFilePath = remoteFilePath + "/";
		}
		Session session = null;
		try {
			session = openSession(bean);
			ChannelSftp c = openChannel(session);
			int mode = ChannelSftp.OVERWRITE;
			if (!type) {
				mode = ChannelSftp.RESUME;
			}
			c.put(localFilePath, remoteFilePath, new FileProgressMonitor(), mode);
		} catch (Exception e) {
			logger.warn("SFTP上传文件失败，失败信息：", e);
			return false;
		} finally {
			if (session != null && session.isConnected()) {
				session.disconnect();
			}
		}
		logger.info("SFTP文件上传完成...");
		return true;
	}

	public static boolean upload(ScpBean bean, String remoteFilePath, String remoteFileName, InputStream input,
			boolean type) {
		if (StringUtils.isBlank(remoteFilePath)) {
			logger.warn("远程文件地址不能为空");
			return false;
		}
		if (input == null) {
			logger.warn("本地文件流为空");
			return false;
		}
		if (!bean.validate()) {
			logger.warn("登陆远程服务器配置信息校验失败");
			return false;
		}
		if (remoteFilePath != null && !(remoteFilePath.endsWith("/") || remoteFilePath.endsWith("\\"))) {
			remoteFilePath = remoteFilePath + "/";
		}
		Session session = null;
		try {
			session = openSession(bean);
			ChannelSftp c = openChannel(session);
			int mode = ChannelSftp.OVERWRITE;
			if (!type) {
				mode = ChannelSftp.RESUME;
			}
			c.put(input, remoteFilePath + remoteFileName, new FileProgressMonitor(), mode);
		} catch (Exception e) {
			logger.warn("SFTP上传文件失败，失败信息：", e);
			return false;
		} finally {
			if (session != null && session.isConnected()) {
				session.disconnect();
			}
		}
		logger.info("SFTP文件上传完成...");
		return true;
	}

	/**
	 * sftp上传文件
	 * 
	 * @param bean
	 *            登陆远程服务器配置信息
	 * @param remoteFilePath
	 *            远程文件地址，绝对地址
	 * @param localFilePath
	 *            本地文件地址，绝对地址
	 * @param type
	 *            上传模式，true：OVERWRITE模式，false：RESUME
	 * @return
	 */
	public static boolean upload(ScpBean bean, String remoteFilePath, String localFilePath, boolean type,
			SftpMeta sMeta) {
		if (StringUtils.isBlank(remoteFilePath)) {
			logger.warn("远程文件地址不能为空");
			return false;
		}
		if (StringUtils.isBlank(localFilePath)) {
			logger.warn("本地文件地址为空");
			return false;
		}
		if (!bean.validate()) {
			logger.warn("登陆远程服务器配置信息校验失败");
			return false;
		}
		if (remoteFilePath != null && !(remoteFilePath.endsWith("/") || remoteFilePath.endsWith("\\"))) {
			remoteFilePath = remoteFilePath + "/";
		}
		Session session = null;
		File metaFile = new File(localFilePath + "-m");
		try {
			session = openSession(bean);
			ChannelSftp c = openChannel(session);

			int mode = ChannelSftp.OVERWRITE;
			if (!type) {
				mode = ChannelSftp.RESUME;
			}
			c.put(localFilePath, remoteFilePath, new FileProgressMonitor(), mode);
			FileOutputStream fileOutputStream = null;
			try {
				fileOutputStream = new FileOutputStream(metaFile);
				sMeta.getMeta().store(fileOutputStream, "");
				c.put(metaFile.getAbsolutePath(), remoteFilePath, mode);
			} catch (Exception e) {
				logger.error("save meta failed", e);
				try {
					metaFile.delete();
				} catch (Exception e2) {
					logger.error("delete file error", e2);
				}
			} finally {
				if (fileOutputStream != null) {
					fileOutputStream.close();
				}
			}
		} catch (Exception e) {
			logger.warn("SFTP上传文件失败，失败信息：", e);
			return false;
		} finally {
			if (session != null && session.isConnected()) {
				session.disconnect();
			}
			if (metaFile.exists()) {
				try {
					metaFile.delete();
				} catch (Exception e) {
				}
			}
		}
		logger.info("SFTP文件上传完成...");
		return true;
	}
	
	/**
	 * sftp上传文件
	 * 
	 * @param bean
	 *            登陆远程服务器配置信息
	 * @param remoteFilePath
	 *            远程文件地址，绝对地址
	 * @param localFilePath
	 *            本地文件地址，绝对地址
	 * @param type
	 *            上传模式，true：OVERWRITE模式，false：RESUME
	 * @return
	 */
	public static boolean upload(ScpBean bean, String remoteFilePath, String remoteFileName, InputStream inputStream, boolean type,
			SftpMeta sMeta) {
		if (StringUtils.isBlank(remoteFilePath)) {
			logger.warn("远程文件地址不能为空");
			return false;
		}
		if (inputStream == null) {
			logger.warn("本地文件流为空");
			return false;
		}
		if (!bean.validate()) {
			logger.warn("登陆远程服务器配置信息校验失败");
			return false;
		}
		if (remoteFilePath != null && !(remoteFilePath.endsWith("/") || remoteFilePath.endsWith("\\"))) {
			remoteFilePath = remoteFilePath + "/";
		}
		Session session = null;
		try {
			session = openSession(bean);
			ChannelSftp c = openChannel(session);

			int mode = ChannelSftp.OVERWRITE;
			if (!type) {
				mode = ChannelSftp.RESUME;
			}
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			c.put(inputStream, remoteFilePath + remoteFileName, new FileProgressMonitor(), mode);
			try {
				sMeta.getMeta().store(byteArrayOutputStream, "");
				c.put(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()), remoteFilePath + remoteFileName + "-m", mode);
			} catch (Exception e) {
				logger.error("save meta failed", e);
			} finally {
				byteArrayOutputStream.close();
			}
		} catch (Exception e) {
			logger.warn("SFTP上传文件失败，失败信息：", e);
			return false;
		} finally {
			if (session != null && session.isConnected()) {
				session.disconnect();
			}
		}
		logger.info("SFTP文件上传完成...");
		return true;
	}
	/**
	 * sftp下载文件
	 * 
	 * @param bean
	 *            登陆远程服务器配置信息
	 * @param remoteFilePath
	 *            远程文件地址，绝对地址
	 * @param localFilePath
	 *            本地文件地址，绝对地址
	 * @param type
	 *            上传模式，true：OVERWRITE模式，false：RESUME
	 * @return
	 */
	public static boolean download(ScpBean bean, String remoteFilePath, String localFilePath, boolean type) {
		if (StringUtils.isBlank(remoteFilePath)) {
			logger.warn("远程文件地址不能为空");
			return false;
		}
		if (StringUtils.isBlank(localFilePath)) {
			logger.warn("本地文件地址为空");
			return false;
		}
		if (!bean.validate()) {
			logger.warn("登陆远程服务器配置信息校验失败");
			return false;
		}

		Session session = null;
		try {
			session = openSession(bean);
			ChannelSftp c = openChannel(session);

			int mode = ChannelSftp.OVERWRITE;
			if (!type) {
				mode = ChannelSftp.RESUME;
			}
			c.get(remoteFilePath, localFilePath, new FileProgressMonitor(), mode);
		} catch (Exception e) {
			logger.warn("SFTP下载文件失败，失败信息：", e);
			return false;
		} finally {
			if (session != null && session.isConnected()) {
				session.disconnect();
			}
		}
		logger.info("SFTP文件下载完成...");
		return true;
	}

	/**
	 * 判断是否存在路径，如果是文件会抛出异常
	 * 
	 * @param bean
	 *            登陆远程服务器配置信息
	 * @param remoteFilePath
	 *            远程文件地址，绝对地址
	 * @return 是否存在
	 * @throws IOException
	 *             操作异常/remoteFilePath对应的是个文件
	 */
	public static boolean isDir(ScpBean bean, String remoteFilePath) throws IOException {
		if (StringUtils.isBlank(remoteFilePath)) {
			logger.warn("远程文件地址不能为空");
			return false;
		}
		if (!bean.validate()) {
			logger.warn("登陆远程服务器配置信息校验失败");
			return false;
		}

		Session session = null;
		try {
			session = openSession(bean);
			ChannelSftp c = openChannel(session);

			SftpATTRS lstat = c.lstat(remoteFilePath);
			if (lstat.isDir()) {
				return true;
			} else {
				throw new IOException(remoteFilePath + "is file");
			}
		} catch (IOException ioe) {
			throw ioe;
		} catch (Exception e) {
			logger.warn("SFTP判断文件信息失败：", e);
			return false;
		} finally {
			if (session != null && session.isConnected()) {
				session.disconnect();
			}
		}
	}

	/**
	 * 判断是否存在文件
	 * 
	 * @param bean
	 *            登陆远程服务器配置信息
	 * @param remoteFilePath
	 *            远程文件地址，绝对地址
	 * @return 是否存在
	 * @throws IOException
	 *             操作异常/remoteFilePath对应的是个文件
	 */
	public static boolean exists(ScpBean bean, String remoteFilePath) {
		if (StringUtils.isBlank(remoteFilePath)) {
			logger.warn("远程文件地址不能为空");
			return false;
		}
		if (!bean.validate()) {
			logger.warn("登陆远程服务器配置信息校验失败");
			return false;
		}

		Session session = null;
		try {
			session = openSession(bean);
			ChannelSftp c = openChannel(session);

			SftpATTRS lstat = c.lstat(remoteFilePath);
			return lstat.getATime() != 0;
		} catch (Exception e) {
			logger.warn("SFTP判断文件信息失败：", e);
			return false;
		} finally {
			if (session != null && session.isConnected()) {
				session.disconnect();
			}
		}
	}

	/**
	 * 创建路径
	 * 
	 * @param bean
	 * @param remoteFilePath
	 * @return
	 */
	public static boolean create(ScpBean bean, String remoteFilePath) {
		if (StringUtils.isBlank(remoteFilePath)) {
			logger.warn("远程文件地址不能为空");
			return false;
		}
		if (!bean.validate()) {
			logger.warn("登陆远程服务器配置信息校验失败");
			return false;
		}
		if (remoteFilePath != null && !(remoteFilePath.endsWith("/") || remoteFilePath.endsWith("\\"))) {
			remoteFilePath = remoteFilePath + "/";
		}
		Session session = null;
		try {
			session = openSession(bean);
			ChannelSftp c = openChannel(session);
			createDir(c, remoteFilePath);
			return true;
		} catch (Exception e) {
			logger.warn("SFTP判断文件信息失败：", e);
			return false;
		} finally {
			if (session != null && session.isConnected()) {
				session.disconnect();
			}
		}
	}

	private static Session openSession(ScpBean bean) throws JSchException {
		Session session = null;
		JSch jsch = new JSch();
		session = jsch.getSession(bean.getUsername(), bean.getRemoteAddress(), bean.getRemotePort());
		ScpUserInfo userInfo = new ScpUserInfo(bean.getPassword());
		session.setUserInfo(userInfo);
		session.connect();
		return session;
	}

	private static ChannelSftp openChannel(Session session) throws JSchException {
		Channel channel = session.openChannel("sftp");
		channel.connect();
		ChannelSftp c = (ChannelSftp) channel;
		return c;
	}

	/**
	 * 创建一个文件目录
	 * 
	 * @throws IOException
	 */
	private static void createDir(ChannelSftp sftp, String createpath) throws IOException {
		try {
			if (isDirExist(sftp, createpath)) {
				sftp.cd(createpath);
				return;
			}
			String pathArry[] = createpath.split("/");
			StringBuffer filePath = new StringBuffer("/");
			for (String path : pathArry) {
				if (path.equals("")) {
					continue;
				}
				filePath.append(path + "/");
				if (isDirExist(sftp, filePath.toString())) {
					sftp.cd(filePath.toString());
				} else {
					sftp.mkdir(filePath.toString());
					sftp.cd(filePath.toString());
				}
			}
			sftp.cd(createpath);
		} catch (SftpException e) {
			throw new IOException("创建路径错误：" + createpath);
		}
	}

	/**
	 * 判断目录是否存在
	 */
	private static boolean isDirExist(ChannelSftp sftp, String directory) {
		boolean isDirExistFlag = false;
		try {
			SftpATTRS sftpATTRS = sftp.lstat(directory);
			isDirExistFlag = true;
			return sftpATTRS.isDir();
		} catch (Exception e) {
			if (e.getMessage().toLowerCase().equals("no such file")) {
				isDirExistFlag = false;
			}
		}
		return isDirExistFlag;
	}

	public static class FileProgressMonitor implements SftpProgressMonitor {
		long count = 1;
		long max = 1;

		public void init(int op, String src, String dest, long max) {
			this.max = max;
			count = 1;
			percent = -1;
		}

		private long percent = -1;

		public boolean count(long count) {
			this.count += count;

			if (percent >= this.count * 100 / max) {
				return true;
			}
			percent = this.count * 100 / max;
			logger.info("文件传输比率：" + percent);

			return !(percent == 100 ? true : false);
		}

		public void end() {
		}
	}
}
