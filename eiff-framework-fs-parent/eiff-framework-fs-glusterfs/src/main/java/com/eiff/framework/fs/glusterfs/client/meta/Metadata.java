package com.eiff.framework.fs.glusterfs.client.meta;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;

import com.eiff.framework.log.api.HdLogger;

public class Metadata {
	final static HdLogger LOGGER = HdLogger.getLogger(Metadata.class);
	final static String EXPIRE_TIME = "expireTimems";
	final static String FILE_NAME = "fileName";
	final static String FILE_EXT_NAME = "fileExtName";
	final static String MODULE = "module";

	public static String METAFILE_SUFFIX = ".properties";

	private Properties info = new Properties();

	private Metadata() {
	}

	public static Metadata build() {
		Metadata metadata = new Metadata();
		metadata.info.setProperty(MODULE, LOGGER.buildTracer().getDomainName());
		return metadata;
	}

	/**
	 * 新增文件有效期参数
	 * 
	 * @param expireTime
	 *            单位ms，达到这个ms后文件会被转移
	 * @return
	 */
	public Metadata editExpire(long expireTime) {
		info.put(EXPIRE_TIME, String.valueOf(expireTime));
		return this;
	}

	public Metadata editFileName(String fileName) {
		info.put(FILE_NAME, fileName);
		return this;
	}

	public Metadata editFileType(String fileType) {
		info.put(FILE_EXT_NAME, fileType);
		return this;
	}

	public String getInfo() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		String returnString = "";
		try {
			info.store(baos, "");
			returnString = baos.toString();
			baos.close();
		} catch (IOException e) {
			LOGGER.error("", e);
		}

		return returnString;
	}
}
