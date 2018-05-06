package com.eiff.framework.idgen.zk.impl;

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.atomic.AtomicValue;
import org.apache.curator.framework.recipes.atomic.DistributedAtomicLong;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.retry.RetryNTimes;

import com.eiff.framework.idgen.api.AbstractIncrementIdGenerator;
import com.eiff.framework.idgen.zk.exception.CannotIncreaseException;
import com.eiff.framework.idgen.zk.exception.CannotSetOffsetException;

/**
 * @author tangzhaowei
 */
public class IncrementZkIdGenerator extends AbstractIncrementIdGenerator {

	private Long offset;
	private String idNodeName;
	private String zkAddress;

	public IncrementZkIdGenerator(String prefix, int preFetchCount, int idLen, Long offset, String idNodeName, String zkAddress) {
		this.preFetchCount = preFetchCount;
		this.prefix = prefix;
		this.idLen = idLen;
		this.offset = offset;
		this.idNodeName = idNodeName;
		this.zkAddress = zkAddress;
		fillingIds();
	}

	@Override
	public String genId() {
		Long id = null;
		try {
			id = ids.poll(3000, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		if (id == null) {
			id = ids.poll();
		}

		if (id == null) {
			throw new RuntimeException("can not generate id");
		} else if (ids.isEmpty()) {
			reset();
		}

		return StringUtils.leftPad(String.valueOf(id), idLen, "0");
	}

	private synchronized void reset() {
		if (ids.isEmpty()) {
			long curValue = incDistributedValue();
			for (long i = curValue; i < curValue + preFetchCount; i++) {
				ids.offer(i);
			}
		}
	}

	@Override
	protected void fillingIds() {
		long curValue = initDistributedValue();
		for (long i = curValue; i < curValue + preFetchCount; i++) {
			ids.offer(i);
		}
	}

	private long incDistributedValue() {
		CuratorFramework client = null;
		try {
			client = CuratorFrameworkFactory.builder().connectString(zkAddress).retryPolicy(new ExponentialBackoffRetry(1000, 3)).sessionTimeoutMs(5000)
					.build();
			client.start();
			DistributedAtomicLong count = new DistributedAtomicLong(client, getRootPath(), new RetryNTimes(10, 10));

			AtomicValue<Long> ac = null;
			for (int i = 0; i < 5; i++) {
				ac = count.add((long) preFetchCount);
				if (ac.succeeded()) {
					return ac.preValue();
				}
			}
			throw new CannotIncreaseException();
		} catch (Throwable e) {
			throw new CannotIncreaseException();
		} finally {
			if (client != null) {
				client.close();
			}
		}
	}

	private long initDistributedValue() {
		CuratorFramework client = null;
		try {
			client = CuratorFrameworkFactory.builder().connectString(zkAddress).retryPolicy(new ExponentialBackoffRetry(1000, 3)).sessionTimeoutMs(5000)
					.build();
			client.start();
			DistributedAtomicLong count = new DistributedAtomicLong(client, getRootPath(), new RetryNTimes(10, 10));
			if (offset != null) {
				AtomicValue<Long> av = null;
				for (int i = 0; i < 5; i++) {
					av = count.trySet(offset);
					if (av.succeeded()) {
						break;
					} else {
						if (i == 4) {
							throw new CannotSetOffsetException();
						} else {
							continue;
						}
					}
				}
			}

			AtomicValue<Long> av = null;
			for (int i = 0; i < 5; i++) {
				av = count.add((long) preFetchCount);
				if (av.succeeded()) {
					return av.preValue();
				}
			}
			throw new CannotIncreaseException();
		} catch (Throwable e) {
			throw new CannotIncreaseException();
		} finally {
			if (client != null) {
				client.close();
			}
		}
	}

	private String getRootPath() {
		return "/eif/" + prefix + "/" + idNodeName;
	}

	public String getIdNodeName() {
		return idNodeName;
	}

	public void setIdNodeName(String idNodeName) {
		this.idNodeName = idNodeName;
	}

	public String getZkAddress() {
		return zkAddress;
	}

	public void setZkAddress(String zkAddress) {
		this.zkAddress = zkAddress;
	}

	public Long getOffset() {
		return offset;
	}

	public void setOffset(Long offset) {
		if (offset != null && offset < 0) {
			throw new IllegalArgumentException("offset must be greater than or equal to 0");
		}
		this.offset = offset;
	}
}