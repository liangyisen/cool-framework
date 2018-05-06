package com.eiff.framework.fs.fastdfs.client;

import java.io.IOException;
import java.lang.reflect.Proxy;

import com.eiff.framework.fs.fastdfs.FileInfo;
import com.eiff.framework.fs.fastdfs.common.MyException;
import com.eiff.framework.fs.fastdfs.common.NotOverridableException;
import com.eiff.framework.fs.fastdfs.pool.conf.BasePoolConfig;
import com.eiff.framework.fs.fastdfs.token.AntiStealToken;

public class HfsClient implements HfsClientInterface {

	private HfsClientInterface proxy;
	private HfsClientSource source;
	private HfsClientSourceWithPool poolSource;

	/**
	 * 
	 * @param connectTimeout
	 *            单位是ms
	 * @param networkTimeout
	 *            单位是ms
	 * @param charset
	 * @param trackerServers
	 * @param trackerPort
	 * @param antiStealToken
	 * @throws MyException
	 */
	public HfsClient(int connectTimeout, int networkTimeout, String charset, String[] trackerServers, int trackerPort,
			boolean antiStealToken) throws MyException {
		this.source = new HfsClientSource(connectTimeout, networkTimeout, charset, trackerServers, trackerPort,
				antiStealToken);
		this.poolSource = new HfsClientSourceWithPool(connectTimeout, networkTimeout, charset, trackerServers,
				trackerPort, antiStealToken);
		this.proxy = (HfsClientInterface) Proxy.newProxyInstance(source.getClass().getClassLoader(),
				source.getClass().getInterfaces(), new HfsClientProxy(source, poolSource));
	}

	public HfsClient(String[] trackerServers, int trackerPort, boolean antiStealToken) throws MyException {
		this.source = new HfsClientSource(trackerServers, trackerPort, antiStealToken);
		this.poolSource = new HfsClientSourceWithPool(trackerServers, trackerPort, antiStealToken);
		this.proxy = (HfsClientInterface) Proxy.newProxyInstance(source.getClass().getClassLoader(),
				source.getClass().getInterfaces(), new HfsClientProxy(source, poolSource));
	}

	@Override
	public String upload(byte[] fileBuff, String fileName, String sysId) throws Exception {
		return this.proxy.upload(fileBuff, fileName, sysId);
	}

	@Override
	public String upload(String fileName, String fileFullPath, String sysId) throws Exception {
		return this.proxy.upload(fileName, fileFullPath, sysId);
	}

	@Override
	public byte[] getFile(String storedfileName) throws IOException, MyException {
		return this.proxy.getFile(storedfileName);
	}

	@Override
	public void downloadFile(String storedfileName, String local_fileFullPath) throws IOException, MyException {
		this.proxy.downloadFile(storedfileName, local_fileFullPath);
	}

	@Override
	public boolean deleteFile(String storedfileName) throws IOException {
		return this.proxy.deleteFile(storedfileName);
	}

	@Override
	public FileInfo getFileInfo(String storedfileName) throws IOException {
		return this.proxy.getFileInfo(storedfileName);
	}

	@Override
	public String uploadOverridable(byte[] fileBuff, String fileName, String sysId) throws IOException {
		return this.proxy.uploadOverridable(fileBuff, fileName, sysId);
	}

	@Override
	public String uploadOverridable(String fileName, String fileFullPath, String sysId) throws IOException {
		return this.proxy.uploadOverridable(fileName, fileFullPath, sysId);
	}

	@Override
	public void modifyOverridable(String storedfileName, byte[] localFileContentBuff)
			throws IOException, NotOverridableException {
		this.proxy.modifyOverridable(storedfileName, localFileContentBuff);
	}

	@Override
	public AntiStealToken getAntiStealToken(String fileName) {
		return this.proxy.getAntiStealToken(fileName);
	}

	public void setAntiStealKey(String antiStealKey) {
		this.source.setAntiStealKey(antiStealKey);
		this.poolSource.setAntiStealKey(antiStealKey);
	}

	public void setTrackerPoolConfig(BasePoolConfig trackerPoolConfig) {
		this.poolSource.getConfig().setTrackerPoolConfig(trackerPoolConfig);
		this.poolSource.initTrackerPool();
	}

	public void setStorageReadPoolConfig(BasePoolConfig storagePoolConfig) {
		this.poolSource.getConfig().setStorageReadPoolConfig(storagePoolConfig);
	}

	public void setStorageWritePoolConfig(BasePoolConfig storagePoolConfig) {
		this.poolSource.getConfig().setStorageWritePoolConfig(storagePoolConfig);
		this.poolSource.initStoragePool();
	}

	public void setStorageReadWritePoolConfig(BasePoolConfig storagePoolConfig) {
		this.poolSource.getConfig().setStorageReadWritePoolConfig(storagePoolConfig);
		this.poolSource.initStoragePool();
	}
}
