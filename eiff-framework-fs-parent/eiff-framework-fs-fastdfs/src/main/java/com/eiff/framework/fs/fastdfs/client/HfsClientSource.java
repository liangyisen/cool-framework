package com.eiff.framework.fs.fastdfs.client;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eiff.framework.fs.fastdfs.FileInfo;
import com.eiff.framework.fs.fastdfs.ProtoCommon;
import com.eiff.framework.fs.fastdfs.StorageCommand;
import com.eiff.framework.fs.fastdfs.StorageServer;
import com.eiff.framework.fs.fastdfs.TrackerCommand;
import com.eiff.framework.fs.fastdfs.TrackerServer;
import com.eiff.framework.fs.fastdfs.common.MyException;
import com.eiff.framework.fs.fastdfs.common.NameValuePair;
import com.eiff.framework.fs.fastdfs.common.NotOverridableException;
import com.eiff.framework.fs.fastdfs.conf.NonGlobalConfig;
import com.eiff.framework.fs.fastdfs.log.OperationMessage;
import com.eiff.framework.fs.fastdfs.token.AntiStealToken;

public class HfsClientSource implements HfsClientInterface {

	NonGlobalConfig config;
	private String antiStealKey;
	private static Logger LOGGER = LoggerFactory.getLogger(HfsClient.class);

	public HfsClientSource(int connectTimeout, int networkTimeout, String charset, String[] trackerServers,
			int trackerPort, boolean antiStealToken) throws MyException {
		this.config = new NonGlobalConfig(connectTimeout, networkTimeout, charset, trackerServers, trackerPort,
				antiStealToken);
	}

	public HfsClientSource(String[] trackerServers, int trackerPort, boolean antiStealToken) throws MyException {
		this.config = new NonGlobalConfig(-1, -1, null, trackerServers, trackerPort, antiStealToken);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.eiff.framework.fs.client.HfsClientInterface#upload(byte[],
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public String upload(byte[] fileBuff, String fileName, String sysId) throws Exception {
		TrackerCommand tracker = new TrackerCommand(this.config);
		TrackerServer trackerServer = null;
		try {
			OperationMessage.get().setFileSize(fileBuff);
			OperationMessage.get().setFileName(fileName);
			String fileExtName = "";
			if (fileName.contains(".")) {
				fileExtName = fileName.substring(fileName.lastIndexOf(".") + 1);
			} else {
				throw new Exception("The format of filename is illegal.");
			}

			trackerServer = tracker.getConnection();
			StorageServer storageServer = null;
			StorageCommand client = new StorageCommand(trackerServer, storageServer, this.config);

			NameValuePair[] metaList = new NameValuePair[3];
			metaList[0] = new NameValuePair("fileName", fileName);
			metaList[1] = new NameValuePair("fileExtName", fileExtName);
			metaList[2] = new NameValuePair("sysId", sysId);
			String fileId = client.uploadFile(null, fileBuff, fileExtName, metaList);
			OperationMessage.get().setFileId(fileId);
			return fileId;
		} catch (Exception e) {
			throw e;
		} finally {
			if (trackerServer != null) {
				try {
					trackerServer.close();
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
		TrackerCommand tracker = new TrackerCommand(this.config);
		TrackerServer trackerServer = null;
		try {
			OperationMessage.get().setFileName(fileName);
			OperationMessage.get().setFileSize(fileFullPath);
			String fileExtName = "";
			if (fileName.contains(".")) {
				fileExtName = fileName.substring(fileName.lastIndexOf(".") + 1);
			} else {
				throw new Exception("The format of filename is illegal.");
			}

			trackerServer = tracker.getConnection();
			StorageServer storageServer = null;
			StorageCommand client = new StorageCommand(trackerServer, storageServer, this.config);

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
			if (trackerServer != null) {
				try {
					trackerServer.close();
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
		TrackerServer trackerServer = null;
		try {
			trackerServer = tracker.getConnection();
			StorageServer storageServer = null;

			StorageCommand storageClient = new StorageCommand(trackerServer, storageServer, this.config);
			byte[] downLoadBytes = storageClient.downloadFile(storedfileName);
			OperationMessage.get().setFileSize(downLoadBytes);
			OperationMessage.get().setFileId(storedfileName);
			return downLoadBytes;
		} catch (Exception e) {
			throw e;
		} finally {
			if (trackerServer != null) {
				try {
					trackerServer.close();
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
		TrackerServer trackerServer = null;
		try {
			trackerServer = tracker.getConnection();
			StorageServer storageServer = null;

			StorageCommand storageClient = new StorageCommand(trackerServer, storageServer, this.config);
			int download_file1 = storageClient.downloadFile(storedfileName, local_fileFullPath);
			if (download_file1 != 0) {
				throw new MyException("downloadFile failed with flag: " + download_file1);
			}
			OperationMessage.get().setFileSize(local_fileFullPath);
			OperationMessage.get().setFileId(storedfileName);
		} catch (Exception e) {
			throw e;
		} finally {
			if (trackerServer != null) {
				try {
					trackerServer.close();
				} catch (Exception e2) {
					e2.printStackTrace();
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
		TrackerServer trackerServer = null;
		try {
			trackerServer = tracker.getConnection();
			StorageServer storageServer = null;

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
					trackerServer.close();
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
		TrackerCommand tracker = new TrackerCommand(this.config);
		TrackerServer trackerServer = null;
		try {
			trackerServer = tracker.getConnection();
			StorageServer storageServer = null;

			StorageCommand storageClient = new StorageCommand(trackerServer, storageServer, this.config);
			return storageClient.getFileInfo(storedfileName);
		} catch (IOException e) {
			throw e;
		} catch (Exception ex) {
			throw new IOException(ex.getMessage(), ex);
		} finally {
			if (trackerServer != null) {
				try {
					trackerServer.close();
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
	 * com.eiff.framework.fs.client.HfsClientInterface#uploadOverridable(byte[],
	 * java.lang.String, java.lang.String)
	 */
	@Override
	@Deprecated
	public String uploadOverridable(byte[] fileBuff, String fileName, String sysId) throws IOException {
		TrackerCommand tracker = new TrackerCommand(this.config);
		TrackerServer trackerServer = null;
		try {
			OperationMessage.get().setFileSize(fileBuff);
			OperationMessage.get().setFileName(fileName);
			String fileExtName = "";
			if (fileName.contains(".")) {
				fileExtName = fileName.substring(fileName.lastIndexOf(".") + 1);
			} else {
				throw new Exception("The format of filename is illegal.");
			}

			trackerServer = tracker.getConnection();
			StorageServer storageServer = null;
			StorageCommand client = new StorageCommand(trackerServer, storageServer, this.config);

			NameValuePair[] metaList = new NameValuePair[4];
			metaList[0] = new NameValuePair("fileName", fileName);
			metaList[1] = new NameValuePair("fileExtName", fileExtName);
			metaList[2] = new NameValuePair("sysId", sysId);
			metaList[3] = new NameValuePair(NotOverridableException.OVERRIDABLEFLAG, "true");

			String fileId = client.uploadAppenderFile(null, fileBuff, fileExtName, metaList);
			OperationMessage.get().setFileId(fileId);
			return fileId;
		} catch (IOException e) {
			throw e;
		} catch (Exception ex) {
			throw new IOException(ex.getMessage(), ex);
		} finally {
			if (trackerServer != null) {
				try {
					trackerServer.close();
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
		TrackerCommand tracker = new TrackerCommand(this.config);
		TrackerServer trackerServer = null;
		try {
			OperationMessage.get().setFileName(fileName);
			OperationMessage.get().setFileSize(fileFullPath);
			String fileExtName = "";
			if (fileName.contains(".")) {
				fileExtName = fileName.substring(fileName.lastIndexOf(".") + 1);
			} else {
				throw new Exception("The format of filename is illegal.");
			}

			trackerServer = tracker.getConnection();
			StorageServer storageServer = null;
			StorageCommand client = new StorageCommand(trackerServer, storageServer, this.config);

			NameValuePair[] metaList = new NameValuePair[4];
			metaList[0] = new NameValuePair("fileName", fileName);
			metaList[1] = new NameValuePair("fileExtName", fileExtName);
			metaList[2] = new NameValuePair("sysId", sysId);
			metaList[3] = new NameValuePair(NotOverridableException.OVERRIDABLEFLAG, "true");
			String fileId = client.uploadAppenderFile(null, fileFullPath, fileExtName, metaList);
			OperationMessage.get().setFileId(fileId);
			return fileId;
		} catch (IOException e) {
			throw e;
		} catch (Exception ex) {
			throw new IOException(ex.getMessage(), ex);
		} finally {
			if (trackerServer != null) {
				try {
					trackerServer.close();
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
		TrackerServer trackerServer = null;
		boolean overridable = false;
		try {
			OperationMessage.get().setFileId(storedfileName);
			OperationMessage.get().setFileSize(localFileContentBuff);
			trackerServer = tracker.getConnection();
			StorageServer storageServer = null;
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
			int erase = truncate(storedfileName);
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
					trackerServer.close();
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			}
		}
	}

	private int truncate(String storedfileName) throws IOException, NotOverridableException {
		TrackerCommand tracker = new TrackerCommand(this.config);
		TrackerServer trackerServer = null;
		try {
			trackerServer = tracker.getConnection();
			StorageServer storageServer = null;
			StorageCommand client = new StorageCommand(trackerServer, storageServer, this.config);
			int returnFlag = client.truncateFile(storedfileName);
			return returnFlag;
		} catch (IOException e) {
			throw e;
		} catch (Exception exx) {
			throw new IOException(exx.getMessage(), exx);
		} finally {
			if (trackerServer != null) {
				try {
					trackerServer.close();
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			}
		}
	}

	@Override
	public AntiStealToken getAntiStealToken(String fileName) {
		OperationMessage.get().setFileId(fileName);
		int ts = (int) (System.currentTimeMillis() / 1000);
		String[] fileNamePart = new String[2];
		try {
			if (0 != StorageCommand.splitFileId(fileName, fileNamePart)) {
				throw new MyException("please check the input file name " + fileName);
			}
			if (StringUtils.isEmpty(this.antiStealKey)) {
				throw new MyException("No antiStealKey");
			}
			String token = ProtoCommon.getToken(fileNamePart[1].replaceFirst(fileNamePart[0], ""), ts,
					this.antiStealKey, this.config);
			return new AntiStealToken(ts, token);
		} catch (UnsupportedEncodingException | NoSuchAlgorithmException | MyException e) {
			LOGGER.error("", e);
			return AntiStealToken.emptyToken();
		}
	}

	public void setAntiStealKey(String antiStealKey) {
		this.antiStealKey = antiStealKey;
	}

	public NonGlobalConfig getConfig() {
		return config;
	}
}
