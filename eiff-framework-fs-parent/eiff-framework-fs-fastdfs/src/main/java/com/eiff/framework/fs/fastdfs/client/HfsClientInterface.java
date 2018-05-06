package com.eiff.framework.fs.fastdfs.client;

import java.io.IOException;

import com.eiff.framework.fs.fastdfs.FileInfo;
import com.eiff.framework.fs.fastdfs.common.MyException;
import com.eiff.framework.fs.fastdfs.common.NotOverridableException;
import com.eiff.framework.fs.fastdfs.token.AntiStealToken;

public interface HfsClientInterface {

	/**
	 * 
	 * @param fileBuff
	 *            文件内容
	 * @param fileName
	 *            文件名一定要有文件后缀（.xxx后缀）
	 * @param sysId
	 *            系统名
	 * @return fastdfs上的文件名
	 * @throws Exception
	 */
	String upload(byte[] fileBuff, String fileName, String sysId) throws Exception;

	/**
	 * 
	 * @param fileName
	 *            文件名字，包括后缀
	 * @param fileFullPath
	 *            文件绝对路径
	 * @param sysId
	 *            系统名
	 * @return fastdfs上的文件名
	 * @throws Exception
	 */
	String upload(String fileName, String fileFullPath, String sysId) throws Exception;

	byte[] getFile(String storedfileName) throws IOException, MyException;

	void downloadFile(String storedfileName, String local_fileFullPath) throws IOException, MyException;

	boolean deleteFile(String storedfileName) throws IOException;

	FileInfo getFileInfo(String storedfileName) throws IOException;

	/**
	 * 如没有特殊需求请不要使用 请使用{@link HfsClient#upload(byte[], String, String)}
	 */
	String uploadOverridable(byte[] fileBuff, String fileName, String sysId) throws IOException;

	/**
	 * 如没有特殊需求请不要使用 请使用{@link HfsClient#upload(String, String, String)}
	 */
	String uploadOverridable(String fileName, String fileFullPath, String sysId) throws IOException;

	/**
	 * 只有使用uploadOverridable 创建的文件才可以update 请使用
	 * {@link HfsClient#deleteFile(String)}
	 * {@link HfsClient#upload(String, String, String)}
	 * 
	 * @throws IOException,
	 *             NotOverridableException
	 */
	void modifyOverridable(String storedfileName, byte[] localFileContentBuff)
			throws IOException, NotOverridableException;

	AntiStealToken getAntiStealToken(String fileName);
}