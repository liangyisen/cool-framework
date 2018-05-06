package com.eiff.framework.fs.sftp.utils;


import java.util.Properties;

public class SftpMeta {
	private Properties meta = new  Properties();
	
	private SftpMeta() {
	}
	
	public static SftpMeta build(){
		return new SftpMeta();
	}
	
	public SftpMeta addFileName(String fileName){
		meta.put("fileName", fileName);
		return this;
	}
	
	public synchronized SftpMeta addFileType(String fileType){
		meta.put("fileType", fileType);
		return this;
	}
	
	public synchronized SftpMeta addExpiredTime(long time){
		meta.put("expired", "" + time);
		return this;
	}
	
	Properties getMeta(){
		return this.meta;
	}
}
