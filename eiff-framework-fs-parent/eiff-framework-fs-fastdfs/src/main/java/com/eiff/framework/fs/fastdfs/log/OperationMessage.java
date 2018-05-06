package com.eiff.framework.fs.fastdfs.log;

import java.io.File;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

public class OperationMessage {

	public static String FASTDFS = "fastdfs";
	public static String GLUSTERFS = "glusterfs";
	private static ThreadLocal<OperationMessage> THREADLOCALTRANSACTION = new ThreadLocal<OperationMessage>();

	private long fileSize;
	private String operation;
	private String fileId;
	private String fileName;
	private String fileSource;
	private String fsystemType = FASTDFS;

	private boolean failed = false;
	private String failedType;

	public OperationMessage() {
	}

	public OperationMessage(String operation) {
		super();
		this.operation = operation;
	}

	public long getFileSize() {
		return fileSize;
	}

	public void setFileSize(byte[] bytes) {
		if (ArrayUtils.isEmpty(bytes)) {
			this.fileSize = 0;
			return;
		}
		this.fileSize = bytes.length;
	}

	public void setFileSize(String path) {
		try {
			File file = new File(path);
			this.fileSize = file.length();
		} catch (Exception e) {
			this.fileSize = 0;
		}
	}

	public String getFileId() {
		return fileId;
	}

	public void setFileId(String fileId) {
		if(StringUtils.isEmpty(fileId)){
			this.failed = true;
		}
		this.fileId = fileId;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFileSource() {
		return fileSource;
	}

	public void setFileSource(String fileSource) {
		this.fileSource = fileSource;
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public static void init(String operationName) {
		if (THREADLOCALTRANSACTION.get() == null) {
			THREADLOCALTRANSACTION.set(new OperationMessage(operationName));
		}
	}

	public static OperationMessage get() {
		if (THREADLOCALTRANSACTION.get() == null)
			init("unknown");
		return THREADLOCALTRANSACTION.get();
	}

	public static void clear() {
		THREADLOCALTRANSACTION.remove();
	}

	public boolean isFailed() {
		return failed;
	}

	public void setFailed(boolean failed) {
		this.failed = failed;
	}

	public String getFailedType() {
		return failedType;
	}

	public void setFailedType(String failedType) {
		this.failed = true;
		this.failedType = failedType;
	}

	public String getFsystemType() {
		return fsystemType;
	}

	public void setFsystemType(String fsystemType) {
		this.fsystemType = fsystemType;
	}

	public String getOperationType() {
		if (this.operation.startsWith("upload")) {
			return "UPLOAD";
		} else if (this.operation.startsWith("get") || this.operation.startsWith("download")) {
			return "DOWNLOAD";
		} else if (this.operation.startsWith("modify")) {
			return "MODIFY";
		} else if (this.operation.startsWith("delete")) {
			return "DELETE";
		}
		return "NA";
	}
}
