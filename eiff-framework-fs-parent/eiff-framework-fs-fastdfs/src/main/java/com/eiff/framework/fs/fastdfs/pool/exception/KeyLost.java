package com.eiff.framework.fs.fastdfs.pool.exception;

public class KeyLost extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public KeyLost(String key) {
		super("key " + key + " can not be used");
	}
}
