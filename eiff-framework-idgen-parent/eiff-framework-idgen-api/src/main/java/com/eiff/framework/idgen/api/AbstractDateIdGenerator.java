package com.eiff.framework.idgen.api;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

/**
 * @author tangzhaowei
 */
public abstract class AbstractDateIdGenerator implements IIdGenerator {

	protected int preFetchCount = 1000;

	protected String prefix = StringUtils.EMPTY;

	protected BlockingQueue<Long> ids = new ArrayBlockingQueue<>(preFetchCount);

	protected String machineId;
	protected String dateFormat;

	public SimpleDateFormat getDateFormat() {
		Validate.notBlank(dateFormat);
		return new SimpleDateFormat(dateFormat);
	}

	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}

	/**
	 * set default date format
	 */
	protected abstract void setDefaultDateFormat();

	@Override
	public String genId() {
		Validate.notBlank(machineId);

		Long id = null;
		try {
			id = ids.poll(1000, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		if (id == null) {
			id = ids.poll();
		}

		if (id == null) {
			throw new RuntimeException("can not generate id");
		} else if (id == preFetchCount) {
			fillingIds();
		}

		return prefix + getDateFormat().format(new Date()) + StringUtils.leftPad(machineId, 2, "0") + StringUtils.leftPad(String.valueOf(id), 3, "0");
	}

	@Override
	public List<String> genIds(int num) {
		List<String> ids = new ArrayList<>(num);
		for (int i = 0; i < num; i++) {
			ids.add(genId());
		}
		return ids;
	}

	public void setMachineId(String machineId) {
		Validate.notBlank(machineId);
		if (machineId.length() > 2) {
			this.machineId = StringUtils.right(machineId, 2);
		} else {
			this.machineId = machineId;
		}
	}

	protected void fillingIds() {
		for (int i = 0; i < preFetchCount; i++) {
			ids.offer((long) i);
		}
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
}