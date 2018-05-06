package com.eiff.framework.fs.glusterfs.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eiff.framework.fs.glusterfs.ProtoCommon;
import com.eiff.framework.fs.glusterfs.client.meta.Metadata;
import com.eiff.framework.fs.glusterfs.exception.CannotGetPoolException;
import com.eiff.framework.fs.glusterfs.pool.ConnectionPool;

public class HglusterfsClient {

	private static final Logger LOGGER = LoggerFactory.getLogger(HglusterfsClient.class);
	private String glusterfsRootPath;
	private ConnectionPool connectionPool;
	@SuppressWarnings("unused")
	private long readWriteLockTimeout;

	/**
	 * 
	 * @param rootPath
	 *            glusterfs的存储根路径
	 * @param timeout
	 *            超时时间
	 * @param maxPoolSize
	 *            连接池大小
	 */
	public HglusterfsClient(String rootPath, long timeout, int maxPoolSize) {
		if (StringUtils.isEmpty(rootPath)) {
			LOGGER.error("glusterfs root path should not be null");
		}
		connectionPool = new ConnectionPool(timeout, maxPoolSize);
		this.glusterfsRootPath = rootPath;
		this.readWriteLockTimeout = timeout;
		if (this.glusterfsRootPath.endsWith("/") || this.glusterfsRootPath.endsWith("\\")) {
			this.glusterfsRootPath = this.glusterfsRootPath.substring(0, this.glusterfsRootPath.length() - 1);
		}
	}

	/**
	 * 上传文件
	 * 
	 * @param fileBuff
	 *            需要上传的文件内容
	 * @param fileName
	 *            需要上传的文件名
	 * @param sysId
	 *            调用者所属系统ID
	 * @return 存储后的文件名
	 * @throws CannotGetPoolException
	 *             获取连接池异常
	 * @throws IOException
	 *             写入文件异常
	 */
	public String upload(byte[] fileBuff, String fileName, String sysId) throws CannotGetPoolException, IOException {
		return upload(fileBuff, fileName, sysId, Metadata.build().editFileName(fileName)
				.editFileType(com.eiff.framework.fs.glusterfs.util.File.getFileType(fileName)));
	}

	/**
	 * 上传文件
	 * 
	 * @param fileBuff
	 *            需要上传的文件内容
	 * @param fileName
	 *            需要上传的文件名
	 * @param sysId
	 *            调用者所属系统ID
	 * @return 存储后的文件名
	 * @param meta
	 *            文件元信息
	 * @throws CannotGetPoolException
	 *             获取连接池异常
	 * @throws IOException
	 *             写入文件异常
	 */
	public String upload(byte[] fileBuff, String fileName, String sysId, Metadata meta)
			throws CannotGetPoolException, IOException {
		connectionPool.getConnection();
		try {
			String filePathToBeWrite = this.getGlusterfsRootPath() + ProtoCommon.createFileId(sysId, fileName);
			String filePropToBeWrite = filePathToBeWrite + Metadata.METAFILE_SUFFIX;
			FileUtils.writeByteArrayToFile(new File(filePathToBeWrite), fileBuff);
			FileUtils.writeByteArrayToFile(new File(filePropToBeWrite), meta.getInfo().getBytes());
			return filePathToBeWrite;
		} finally {
			connectionPool.releaseConnection();
		}
	}

	/**
	 * 上传文件
	 * 
	 * @param fileName
	 *            需要上传的文件名
	 * @param fileFullPath
	 *            需要上传的文件全路径
	 * @param sysId
	 *            调用者所属系统ID
	 * @return 存储后的文件名
	 * @throws CannotGetPoolException
	 *             获取连接池异常
	 * @throws IOException
	 *             写入文件异常
	 */
	public String upload(String fileName, String fileFullPath, String sysId)
			throws CannotGetPoolException, IOException {
		return upload(fileFullPath, fileName, sysId, Metadata.build().editFileName(fileName)
				.editFileType(com.eiff.framework.fs.glusterfs.util.File.getFileType(fileName)));
	}

	/**
	 * 上传文件
	 * 
	 * @param fileName
	 *            需要上传的文件名
	 * @param fileFullPath
	 *            需要上传的文件全路径
	 * @param sysId
	 *            调用者所属系统ID
	 * @return 存储后的文件名
	 * @param meta
	 *            文件元信息
	 * @throws CannotGetPoolException
	 *             获取连接池异常
	 * @throws IOException
	 *             写入文件异常
	 */
	public String upload(String fileName, String fileFullPath, String sysId, Metadata meta)
			throws CannotGetPoolException, IOException {
		connectionPool.getConnection();
		try {
			String filePathToBeWrite = this.getGlusterfsRootPath() + ProtoCommon.createFileId(sysId, fileName);
			String filePropToBeWrite = filePathToBeWrite + Metadata.METAFILE_SUFFIX;
			FileUtils.touch(new File(filePathToBeWrite));
			FileUtils.copyFile(new File(fileFullPath), new File(filePathToBeWrite));
			FileUtils.writeByteArrayToFile(new File(filePropToBeWrite), meta.getInfo().getBytes());
			return filePathToBeWrite;
		} finally {
			connectionPool.releaseConnection();
		}
	}

	/**
	 * 下载文件
	 * 
	 * @param storedfileName
	 *            存储的文件名
	 * @param encoding
	 *            输出字符集
	 * @return 返回的是具体的文件内容
	 * @throws IOException
	 *             文件读取异常
	 * @throws CannotGetPoolException
	 *             获取连接池异常
	 */
	public byte[] getFile(String storedfileName, String encoding) throws IOException, CannotGetPoolException {
		connectionPool.getConnection();
		try {
			return FileUtils.readFileToString(new File(storedfileName), encoding).getBytes();
		} finally {
			connectionPool.releaseConnection();
		}
	}

	/**
	 * 下载文件
	 * 
	 * @param storedfileName
	 *            存储的文件名
	 * @return 返回默认的文件内容
	 * @throws IOException
	 *             文件读取异常
	 * @throws CannotGetPoolException
	 *             获取连接池异常
	 */
	public byte[] getFile(String storedfileName) throws IOException, CannotGetPoolException {
		connectionPool.getConnection();
		try {
			return FileUtils.readFileToByteArray(new File(storedfileName));
		} finally {
			connectionPool.releaseConnection();
		}
	}

	/**
	 * 下载文件
	 * 
	 * @param storedfileName
	 *            存储的文件名
	 * @return 文件的字节流
	 * @throws IOException
	 *             文件读取异常
	 * @throws CannotGetPoolException
	 *             获取连接池异常
	 */
	public FileInputStream downloadFile(String storedfileName) throws IOException, CannotGetPoolException {
		connectionPool.getConnection();
		try {
			return FileUtils.openInputStream(new File(storedfileName));
		} finally {
			connectionPool.releaseConnection();
		}
	}

	/**
	 * 删除文件
	 * 
	 * @param storedfileName
	 *            存储的文件名
	 * @throws IOException
	 *             文件读取异常
	 * @throws CannotGetPoolException
	 *             获取连接池异常
	 */
	public void deleteFile(String storedfileName) throws IOException, CannotGetPoolException {
		connectionPool.getConnection();
		try {
			FileUtils.forceDeleteOnExit(new File(storedfileName));
		} finally {
			connectionPool.releaseConnection();
		}
	}

	/**
	 * 判断文件是否存在；
	 * 
	 * @param storedfileName
	 *            存储的文件名
	 * @return 是否存在
	 * @throws CannotGetPoolException
	 *             获取连接池异常
	 */
	public boolean exist(String storedfileName) throws CannotGetPoolException {
		connectionPool.getConnection();
		try {
			return new File(storedfileName).exists();
		} finally {
			connectionPool.releaseConnection();
		}
	}

	public String getGlusterfsRootPath() {
		return glusterfsRootPath;
	}

}
