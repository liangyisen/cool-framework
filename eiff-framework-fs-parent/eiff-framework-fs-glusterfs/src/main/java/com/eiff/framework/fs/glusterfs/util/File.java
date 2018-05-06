package com.eiff.framework.fs.glusterfs.util;

public class File {
	public static String getFileType(String fileName){
		String fileExtName = "";
		if (fileName.contains(".")) {
			fileExtName = fileName.substring(fileName.lastIndexOf(".") + 1);
		} 
		return fileExtName;
	}
}
