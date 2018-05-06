package com.eiff.framework.fs.fastdfs.client;

import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eiff.framework.fs.fastdfs.FileInfo;
import com.eiff.framework.fs.fastdfs.StorageCommand;
import com.eiff.framework.fs.fastdfs.StorageServer;
import com.eiff.framework.fs.fastdfs.TrackerCommand;
import com.eiff.framework.fs.fastdfs.TrackerServer;
import com.eiff.framework.fs.fastdfs.common.BaseTool;
import com.eiff.framework.fs.fastdfs.common.MyException;
import com.eiff.framework.fs.fastdfs.common.NameValuePair;
import com.eiff.framework.fs.fastdfs.common.NotOverridableException;
import com.eiff.framework.fs.fastdfs.log.OperationMessage;
import com.eiff.framework.fs.fastdfs.pool.StoragePool;
import com.eiff.framework.fs.fastdfs.pool.TrackerPool;
import com.eiff.framework.fs.fastdfs.pool.factory.StorageFactory;
import com.eiff.framework.fs.fastdfs.pool.factory.TrackerFactory;

public class HfsClientSourceWithPool extends HfsClientSource {

	private StoragePool storageReadPool;
	private StoragePool storageWritePool;
	private TrackerPool trackerPool;
	private boolean configed;
	private static Logger LOGGER = LoggerFactory.getLogger(HfsClientSourceWithPool.class);

	public HfsClientSourceWithPool(int connectTimeout, int networkTimeout, String charset, String[] trackerServers,
			int trackerPort, boolean antiStealToken) throws MyException {
		super(connectTimeout, networkTimeout, charset, trackerServers, trackerPort, antiStealToken);
	}

	public HfsClientSourceWithPool(String[] trackerServers, int trackerPort, boolean antiStealToken)
			throws MyException {
		super(trackerServers, trackerPort, antiStealToken);
	}

	@Override
	public String upload(byte[] fileBuff, String fileName, String sysId) throws Exception {
		StorageServer storageServer = null;
		try {
			OperationMessage.get().setFileSize(fileBuff);
			OperationMessage.get().setFileName(fileName);
			String fileExtName = "";
			if (fileName.contains(".")) {
				fileExtName = fileName.substring(fileName.lastIndexOf(".") + 1);
			} else {
				throw new Exception("The format of filename is illegal.");
			}

			storageServer = storageWritePool.borrowObject();
			StorageCommand client = new StorageCommand(null, storageServer, this.config);

			String fileId = client.uploadFile(null, fileBuff, fileExtName,
					BaseTool.creatNameValuePairs(fileName, fileExtName, sysId));
			OperationMessage.get().setFileId(fileId);
			return fileId;
		} catch (Exception e) {
			throw e;
		} finally {
			if (storageServer != null) {
				try {
					storageServer.returnObject();
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.eiff.framework.fs.client.HfsClientInterface#upload(java.lang.String,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public String upload(String fileName, String fileFullPath, String sysId) throws Exception {
		StorageServer storageServer = null;
		try {
			OperationMessage.get().setFileName(fileName);
			OperationMessage.get().setFileSize(fileFullPath);
			String fileExtName = "";
			if (fileName.contains(".")) {
				fileExtName = fileName.substring(fileName.lastIndexOf(".") + 1);
			} else {
				throw new Exception("The format of filename is illegal.");
			}
			storageServer = storageWritePool.borrowObject();
			StorageCommand client = new StorageCommand(null, storageServer, this.config);

			NameValuePair[] metaList = new NameValuePair[3];
			metaList[0] = new NameValuePair("fileName", fileName);
			metaList[1] = new NameValuePair("fileExtName", fileExtName);
			metaList[2] = new NameValuePair("sysId", sysId);
			String fileId = client.uploadFile(null, fileFullPath, fileExtName, metaList);
			OperationMessage.get().setFileId(fileId);
			return fileId;
		} catch (Exception e) {
			throw e;
		} finally {
			if (storageServer != null) {
				try {
					storageServer.returnObject();
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.eiff.framework.fs.client.HfsClientInterface#getFile(java.lang.String)
	 */
	@Override
	public byte[] getFile(String storedfileName) throws IOException, MyException {
		TrackerCommand tracker = new TrackerCommand(this.config);
		StorageServer storageServer = null;
		TrackerServer trackerServer = null;
		try {
			trackerServer = trackerPool.borrowObject();
			String storageServerInfo = tracker.getFetchStorageInfo(trackerServer, storedfileName);
			if (StringUtils.isEmpty(storageServerInfo)) {
				LOGGER.info("fetchfailed " + storedfileName);
			} else {
				storageServer = storageReadPool.borrowObject(storageServerInfo);
			}
			StorageCommand storageClient = new StorageCommand(trackerServer, storageServer, this.config);
			byte[] downLoadBytes = storageClient.downloadFile(storedfileName);
			OperationMessage.get().setFileSize(downLoadBytes);
			OperationMessage.get().setFileId(storedfileName);
			return downLoadBytes;
		} catch (Exception e) {
			throw new MyException(e);
		} finally {
			if (trackerServer != null) {
				try {
					trackerServer.returnObject();
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			}
			if (storageServer != null) {
				try {
					storageServer.returnObject();
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.eiff.framework.fs.client.HfsClientInterface#downloadFile(java.lang.
	 * String, java.lang.String)
	 */
	@Override
	public void downloadFile(String storedfileName, String local_fileFullPath) throws IOException, MyException {
		TrackerCommand tracker = new TrackerCommand(this.config);
		StorageServer storageServer = null;
		TrackerServer trackerServer = null;
		try {
			trackerServer = trackerPool.borrowObject();
			String storageServerInfo = tracker.getFetchStorageInfo(trackerServer, storedfileName);
			if (StringUtils.isEmpty(storageServerInfo)) {
				LOGGER.info("fetchfailed " + storedfileName);
			} else {
				storageServer = storageReadPool.borrowObject(storageServerInfo);
			}
			StorageCommand storageClient = new StorageCommand(trackerServer, storageServer, this.config);
			int download_file1 = storageClient.downloadFile(storedfileName, local_fileFullPath);
			if (download_file1 != 0) {
				throw new MyException("downloadFile failed with flag: " + download_file1);
			}
			OperationMessage.get().setFileSize(local_fileFullPath);
			OperationMessage.get().setFileId(storedfileName);
		} catch (Exception e) {
			throw new MyException(e);
		} finally {
			if (trackerServer != null) {
				try {
					trackerServer.returnObject();
				} catch (Exception e2) {
					LOGGER.error("", e2);
				}
			}
			if (storageServer != null) {
				try {
					storageServer.returnObject();
				} catch (Exception e2) {
					LOGGER.error("", e2);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.eiff.framework.fs.client.HfsClientInterface#deleteFile(java.lang.
	 * String)
	 */
	@Override
	public boolean deleteFile(String storedfileName) throws IOException {
		OperationMessage.get().setFileId(storedfileName);
		TrackerCommand tracker = new TrackerCommand(this.config);
		StorageServer storageServer = null;
		TrackerServer trackerServer = null;
		try {
			trackerServer = trackerPool.borrowObject();
			String storageServerInfo = tracker.getUpdateStorageInfo(trackerServer, storedfileName);
			if (StringUtils.isEmpty(storageServerInfo)) {
				LOGGER.info("fetchfailed " + storedfileName);
			} else {
				storageServer = storageReadPool.borrowObject(storageServerInfo);
			}
			StorageCommand storageClient = new StorageCommand(trackerServer, storageServer, this.config);
			int returnFlag = storageClient.deleteFile(storedfileName);
			return returnFlag == 0;
		} catch (IOException e) {
			throw e;
		} catch (Exception ex) {
			throw new IOException(ex.getMessage(), ex);
		} finally {
			if (trackerServer != null) {
				try {
					trackerServer.returnObject();
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			}
			if (storageServer != null) {
				try {
					storageServer.returnObject();
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.eiff.framework.fs.client.HfsClientInterface#getFileInfo(java.lang.
	 * String)
	 */
	@Override
	public FileInfo getFileInfo(String storedfileName) throws IOException {
		OperationMessage.get().setFileId(storedfileName);
		try {
			StorageCommand storageClient = new StorageCommand(this.config);
			return storageClient.getFileInfo(storedfileName);
		} catch (Exception ex) {
			throw new IOException(ex.getMessage(), ex);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.eiff.framework.fs.client.HfsClientInterface#uploadOverridable(byte[],
	 * java.lang.String, java.lang.String)
	 */
	@Override
	@Deprecated
	public String uploadOverridable(byte[] fileBuff, String fileName, String sysId) throws IOException {
		StorageServer storageServer = null;
		try {
			OperationMessage.get().setFileSize(fileBuff);
			OperationMessage.get().setFileName(fileName);
			String fileExtName = "";
			if (fileName.contains(".")) {
				fileExtName = fileName.substring(fileName.lastIndexOf(".") + 1);
			} else {
				throw new Exception("The format of filename is illegal.");
			}
			storageServer = storageWritePool.borrowObject();
			StorageCommand client = new StorageCommand(null, storageServer, this.config);

			String fileId = client.uploadAppenderFile(null, fileBuff, fileExtName,
					BaseTool.creatNameValuePairs(fileName, fileExtName, sysId));
			OperationMessage.get().setFileId(fileId);
			return fileId;
		} catch (IOException e) {
			throw e;
		} catch (Exception ex) {
			throw new IOException(ex.getMessage(), ex);
		} finally {
			if (storageServer != null) {
				try {
					storageServer.returnObject();
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.eiff.framework.fs.client.HfsClientInterface#uploadOverridable(java.
	 * lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	@Deprecated
	public String uploadOverridable(String fileName, String fileFullPath, String sysId) throws IOException {
		StorageServer storageServer = null;
		try {
			OperationMessage.get().setFileName(fileName);
			OperationMessage.get().setFileSize(fileFullPath);
			String fileExtName = "";
			if (fileName.contains(".")) {
				fileExtName = fileName.substring(fileName.lastIndexOf(".") + 1);
			} else {
				throw new Exception("The format of filename is illegal.");
			}

			storageServer = storageWritePool.borrowObject();
			StorageCommand client = new StorageCommand(null, storageServer, this.config);

			NameValuePair[] metaList = BaseTool.creatNameValuePairs(fileName, fileExtName, sysId);
			metaList = Arrays.copyOf(metaList, metaList.length + 1);
			metaList[metaList.length - 1] = new NameValuePair(NotOverridableException.OVERRIDABLEFLAG, "true");
			String fileId = client.uploadAppenderFile(null, fileFullPath, fileExtName, metaList);
			OperationMessage.get().setFileId(fileId);
			return fileId;
		} catch (IOException e) {
			throw e;
		} catch (Exception ex) {
			throw new IOException(ex.getMessage(), ex);
		} finally {
			if (storageServer != null) {
				try {
					storageServer.returnObject();
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.eiff.framework.fs.client.HfsClientInterface#modifyOverridable(java.
	 * lang.String, byte[])
	 */
	@Override
	@Deprecated
	public void modifyOverridable(String storedfileName, byte[] localFileContentBuff)
			throws IOException, NotOverridableException {
		TrackerCommand tracker = new TrackerCommand(this.config);
		StorageServer storageServer = null;
		TrackerServer trackerServer = null;
		boolean overridable = false;
		try {
			OperationMessage.get().setFileId(storedfileName);
			OperationMessage.get().setFileSize(localFileContentBuff);
			trackerServer = trackerPool.borrowObject();
			String storageServerInfo = tracker.getUpdateStorageInfo(trackerServer, storedfileName);
			if (StringUtils.isEmpty(storageServerInfo)) {
				LOGGER.info("fetchfailed " + storedfileName);
			} else {
				storageServer = storageReadPool.borrowObject(storageServerInfo);
			}
			StorageCommand client = new StorageCommand(trackerServer, storageServer, this.config);
			NameValuePair[] metadata = client.getMetadata(storedfileName);
			if (metadata == null || metadata.length < 1) {
				throw new NotOverridableException();
			}
			for (NameValuePair nameValuePair : metadata) {
				if (nameValuePair.getName().equals(NotOverridableException.OVERRIDABLEFLAG)) {
					overridable = Boolean.valueOf(nameValuePair.getValue());
					break;
				}
			}
			if (!overridable) {
				throw new NotOverridableException();
			}
			int erase = truncate(client, storedfileName);
			int returnFlag;
			if (erase == 0) {
				returnFlag = client.modifyFile(storedfileName, 0, localFileContentBuff);
			} else {
				throw new NotOverridableException("666 " + erase);
			}

			if (returnFlag != 0) {
				throw new NotOverridableException("777 " + returnFlag);
			}
		} catch (IOException e) {
			throw e;
		} catch (NotOverridableException ex) {
			throw ex;
		} catch (Exception exx) {
			throw new IOException(exx.getMessage(), exx);
		} finally {
			if (trackerServer != null) {
				try {
					trackerServer.returnObject();
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			}
			if (storageServer != null) {
				try {
					storageServer.returnObject();
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			}
		}
	}

	private int truncate(StorageCommand client, String storedfileName) throws IOException, NotOverridableException {
		int returnFlag;
		try {
			returnFlag = client.truncateFile(storedfileName);
		} catch (MyException e) {
			LOGGER.error("truncate failed", e);
			returnFlag = -1;
		}
		return returnFlag;
	}

	public void initStoragePool() {
		if (this.config.getStorageReadPoolConfig() == null && this.config.getStorageWritePoolConfig() == null
				&& this.config.getStorageReadWritePoolConfig() == null) {
			LOGGER.error("no storage pool in used", new NullPointerException());
			return;
		}
		if (this.config.getStorageReadWritePoolConfig() != null) {
			StoragePool readWritePool = new StoragePool(new StorageFactory(this.config),
					this.config.getStorageReadWritePoolConfig());
			this.storageReadPool = readWritePool;
			this.storageWritePool = readWritePool;
			return;
		} else {
			if (this.config.getStorageReadPoolConfig() == null || this.config.getStorageWritePoolConfig() == null) {
				LOGGER.error("storage pool configed error", new NullPointerException());
				return;
			}
			this.storageReadPool = new StoragePool(new StorageFactory(this.config),
					this.config.getStorageReadPoolConfig());
			this.storageWritePool = new StoragePool(new StorageFactory(this.config),
					this.config.getStorageWritePoolConfig());
		}
		this.configed = true;
	}

	public void initTrackerPool() {
		if (this.config.getTrackerPoolConfig() == null)
			return;
		this.trackerPool = new TrackerPool(new TrackerFactory(this.config), this.config.getTrackerPoolConfig());
		this.configed = true;
	}

	public boolean isConfiged() {
		return configed;
	}
}
