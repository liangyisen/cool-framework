package com.eiff.framework.fs.sftp.log;

import java.io.IOException;
import java.io.InputStream;

import com.eiff.framework.fs.sftp.utils.ScpBean;
import com.eiff.framework.fs.sftp.utils.SftpMeta;
import com.eiff.framework.fs.sftp.utils.SftpUtil;
import com.eiff.framework.log.api.HdLogger;
import com.eiff.framework.log.api.trace.Span;
import com.eiff.framework.log.api.trace.Tracer;

public class InTraceSftpService {
	private final static HdLogger LOGGER = HdLogger.getLogger(InTraceSftpService.class);

	private String rootPath;

	public InTraceSftpService(String rootPath) {
		if (!rootPath.endsWith("/") && !rootPath.endsWith("\\")) {
			rootPath = rootPath + "/";
		}
		this.rootPath = rootPath + LOGGER.buildTracer().getDomainName() + "/";
	}

	/**
	 * sftp上传文件
	 * 
	 * @param bean
	 *            登陆远程服务器配置信息
	 * @param remoteFilePath
	 *            远程文件路径，相对路径，系统自动会加上模块前缀，比如输入xxx文件会上传到/rootPath/mtp/xxx
	 * @param localFilePath
	 *            本地文件地址，绝对地址
	 * @param type
	 *            上传模式，true：OVERWRITE模式，false：RESUME
	 * @return 上传后的绝对路径地址/rootPath/mtp/xxx/aa.txt
	 * @throws IOException
	 */
	public String upload(ScpBean bean, String remoteFilePath, String localFilePath, boolean type) throws IOException {
		Tracer buildTracer = LOGGER.buildTracer();
		remoteFilePath = this.convertToAbsolutePath(remoteFilePath);
		Span span = buildTracer.createSpan("SFTP", "upload");
		if (SftpUtil.upload(bean, remoteFilePath, localFilePath, type)) {
			span.success();
		} else {
			throw new IOException("cannot upload to  " + remoteFilePath);
		}
		span.close();
		return remoteFilePath;
	}

	/**
	 * 
	 * @param bean
	 *            登陆远程服务器配置信息
	 * @param remoteFilePath
	 *            远程文件路径，相对路径，系统自动会加上模块前缀，比如输入xxx文件会上传到/rootPath/mtp/xxx
	 * @param remoteFileName
	 *            远程文件名
	 * @param inputStream
	 *            本地文件流
	 * @param type
	 *            上传模式，true：OVERWRITE模式，false：RESUME
	 * @return
	 * @throws IOException
	 */
	public String upload(ScpBean bean, String remoteFilePath, String remoteFileName, InputStream inputStream,
			boolean type) throws IOException {
		Tracer buildTracer = LOGGER.buildTracer();
		remoteFilePath = this.convertToAbsolutePath(remoteFilePath);
		Span span = buildTracer.createSpan("SFTP", "upload");
		if (SftpUtil.upload(bean, remoteFilePath, remoteFileName, inputStream, type)) {
			span.success();
		} else {
			throw new IOException("cannot upload to  " + remoteFilePath);
		}
		span.close();
		return remoteFilePath;
	}

	/**
	 * sftp上传文件
	 * 
	 * @param bean
	 *            登陆远程服务器配置信息
	 * @param remoteFilePath
	 *            远程文件路径，相对路径，系统自动会加上模块前缀，比如输入xxx文件会上传到/rootPath/mtp/xxx
	 * @param localFilePath
	 *            本地文件地址，绝对地址
	 * @param type
	 *            上传模式，true：OVERWRITE模式，false：RESUME
	 * @return 上传后的绝对路径地址/rootPath/mtp/xxx/aa.txt
	 * @throws IOException
	 */
	public String upload(ScpBean bean, String remoteFilePath, String localFilePath, boolean type, SftpMeta mata)
			throws IOException {
		Tracer buildTracer = LOGGER.buildTracer();
		remoteFilePath = this.convertToAbsolutePath(remoteFilePath);
		Span span = buildTracer.createSpan("SFTP", "upload");
		if (SftpUtil.upload(bean, remoteFilePath, localFilePath, type, mata)) {
			span.success();
		} else {
			throw new IOException("cannot upload to  " + remoteFilePath);
		}
		span.close();
		return remoteFilePath;
	}

	/**
	 * 
	 * @param bean
	 *            登陆远程服务器配置信息
	 * @param remoteFilePath
	 *            远程文件路径，相对路径，系统自动会加上模块前缀，比如输入xxx文件会上传到/rootPath/mtp/xxx
	 * @param remoteFileName
	 *            远程文件名
	 * @param inputStream
	 *            本地文件流
	 * @param type
	 *            上传模式，true：OVERWRITE模式，false：RESUME
	 * @return
	 * @throws IOException
	 */
	public String upload(ScpBean bean, String remoteFilePath, String remoteFileName, InputStream inputStream,
			boolean type, SftpMeta mata) throws IOException {
		Tracer buildTracer = LOGGER.buildTracer();
		remoteFilePath = this.convertToAbsolutePath(remoteFilePath);
		Span span = buildTracer.createSpan("SFTP", "upload");
		if (SftpUtil.upload(bean, remoteFilePath, remoteFileName, inputStream, type, mata)) {
			span.success();
		} else {
			throw new IOException("cannot upload to  " + remoteFilePath);
		}
		span.close();
		return remoteFilePath;
	}

	/**
	 * 判断remoteFilePath是否存在
	 * 
	 * @param bean
	 *            登陆远程服务器配置信息
	 * @param remoteFilePath
	 *            远程文件地址，绝对路径
	 * @return 存在返回true，不区分类型
	 */
	public boolean exists(ScpBean bean, String remoteFilePath) {
		Tracer buildTracer = LOGGER.buildTracer();
		Span span = buildTracer.createSpan("SFTP", "exists");
		boolean exists = SftpUtil.exists(bean, remoteFilePath);
		if (exists) {
			span.success();
		}
		span.close();
		return exists;
	}

	/**
	 * 判断是否是存在的路径
	 * 
	 * @param bean
	 *            登陆远程服务器配置信息
	 * @param remoteFilePath
	 *            远程文件地址，绝对路径
	 * @return 如果存在返回true如果不存在返回false
	 * @throws IOException
	 *             如果输入的地址对应的是个文件
	 */
	public boolean isDirExists(ScpBean bean, String remoteFilePath) throws IOException {
		Tracer buildTracer = LOGGER.buildTracer();
		Span span = buildTracer.createSpan("SFTP", "exists");
		boolean exists = SftpUtil.isDir(bean, remoteFilePath);
		if (exists) {
			span.success();
		}
		span.close();
		return exists;
	}

	/**
	 * 
	 * @param bean
	 *            登陆远程服务器配置信息
	 * @param remoteFilePath
	 *            远程文件地址，绝对路径
	 * @return
	 */
	public boolean createDir(ScpBean bean, String remoteFilePath) {
		Tracer buildTracer = LOGGER.buildTracer();
		Span span = buildTracer.createSpan("SFTP", "createDir");
		boolean exists = SftpUtil.create(bean, remoteFilePath);
		if (exists) {
			span.success();
		}
		span.close();
		return exists;
	}

	public String convertToAbsolutePath(String remoteFilePath) {
		if (remoteFilePath != null && remoteFilePath.startsWith("/")) {
			remoteFilePath = remoteFilePath.replaceFirst("/", "");
		}
		return this.rootPath + remoteFilePath;
	}
}
