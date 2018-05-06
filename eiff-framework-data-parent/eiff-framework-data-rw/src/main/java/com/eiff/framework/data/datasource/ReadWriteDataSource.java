package com.eiff.framework.data.datasource;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import com.eiff.framework.data.common.ReadWriteKey;

public class ReadWriteDataSource extends AbstractRoutingDataSource {

	// private static final Logger LOGGER =
	// LoggerFactory.getLogger(ReadWriteDataSource.class);

	private ReadWriteKey readWriteKey;

	@Override
	protected Object determineCurrentLookupKey() {
		String key = readWriteKey.getKey();
		// LOGGER.info("RW_KEY={}", key);
		return key;
	}

	// getter and setter

	public ReadWriteKey getReadWriteKey() {
		return readWriteKey;
	}

	public void setReadWriteKey(ReadWriteKey readWriteKey) {
		this.readWriteKey = readWriteKey;
	}
}
