package com.eiff.framework.fs.fastdfs.vo;

public class FileId {
	private String groupName;
	private String remoteFileName;

	public FileId(String groupName, String remoteFileName) {
		super();
		this.groupName = groupName;
		this.remoteFileName = remoteFileName;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public String getRemoteFileName() {
		return remoteFileName;
	}

	public void setRemoteFileName(String remoteFileName) {
		this.remoteFileName = remoteFileName;
	}
}
