package com.eiff.framework.idgen.api;

import java.util.List;

/**
 * @author tangzhaowei
 */
public interface IIdGenerator {

	/**
	 * generate global uid
	 * 
	 * @return global uid
	 */
	String genId();

	List<String> genIds(int num);
}
