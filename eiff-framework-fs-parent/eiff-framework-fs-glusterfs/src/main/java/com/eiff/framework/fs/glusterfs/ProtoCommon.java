package com.eiff.framework.fs.glusterfs;

import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProtoCommon {
	
	private static int SUB_FILE_NUM = 32;
	private static String[] SUB_FILE_NAMES = new String[SUB_FILE_NUM];
	
	static{
		for (int i = 0; i < SUB_FILE_NAMES.length; i++) {
			SUB_FILE_NAMES[i] = String.valueOf(i);
		}
	}
	private static final Logger LOGGER = LoggerFactory.getLogger(ProtoCommon.class);
	
	public static String createFileId(String fileName){
		return createFileId("_default", fileName);
	}
	
	/**
	 * 
	 * @param bucketName 不能为空
	 * @param fileName 不能为空
	 * @return bucketName/xxx/{32位}-fileName
	 */
	public static String createFileId(String bucketName, String fileName){
		if(StringUtils.isEmpty(bucketName)){
			return createFileId(fileName);
		}
		if(StringUtils.isEmpty(fileName)){
			return StringUtils.EMPTY;
		}
		String uuFileName = UUID.randomUUID().toString().replace("-", "") + "-" + fileName;
		int hashCode = Math.abs(uuFileName.hashCode());
		String filePath = SUB_FILE_NAMES[hashCode % SUB_FILE_NUM];
		fileName = "/" + bucketName + "/" + filePath + "/" + uuFileName;
		LOGGER.info("create the file id for {} to {}", uuFileName, fileName);
		return fileName;
	}
}
