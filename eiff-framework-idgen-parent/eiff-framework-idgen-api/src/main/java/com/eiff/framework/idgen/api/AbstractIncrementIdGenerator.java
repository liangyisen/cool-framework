package com.eiff.framework.idgen.api;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author tangzhaowei
 */
public abstract class AbstractIncrementIdGenerator implements IIdGenerator {

	private static Logger LOGGER = LoggerFactory.getLogger(AbstractIncrementIdGenerator.class);

	protected int preFetchCount = 1000;

	protected int idLen = 8;

	protected String prefix = StringUtils.EMPTY;

	protected BlockingQueue<Long> ids = new ArrayBlockingQueue<>(preFetchCount);

	/**
	 * fill blocking queue with prefetch ids
	 */
	protected abstract void fillingIds();

	@Override
	public String genId() {
		Long id = ids.poll();

		synchronized (this) {
			if (id == null) {
				id = ids.poll();
			}

			if (id == null) {
				fillingIds();
				try {
					id = ids.poll(1000, TimeUnit.MILLISECONDS);
				} catch (InterruptedException e) {
					LOGGER.error("Can not get id from blocking queue.", e);
				}

				if (id == null) {
					LOGGER.error("Can not generate id");
					return null;
				}
			}
		}

		if (String.valueOf(id).length() > (idLen - prefix.length())) {
			LOGGER.error(prefix + id + "'s length will larger than " + idLen);
			return null;
		}

		return prefix + StringUtils.leftPad(String.valueOf(id), idLen - prefix.length(), "0");
	}

	@Override
	public List<String> genIds(int num) {
		List<String> ids = new ArrayList<>(num);
		for (int i = 0; i < num; i++) {
			ids.add(genId());
		}
		return ids;
	}

	public void setPreFetchCount(int preFetchCount) {
		this.preFetchCount = preFetchCount;
	}

	public void setIdLen(int idLen) {
		this.idLen = idLen;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
}
