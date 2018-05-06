package com.eiff.framework.mq.rocketmq.listener.support;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.InitializingBean;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;

import com.eiff.framework.log.api.Constants;
import com.eiff.framework.log.api.HdLogger;
import com.eiff.framework.mq.rocketmq.common.MQConstants;

/**
 * @author tangzhaowei
 */
public class JedisHelper implements Constants, InitializingBean {

	private static HdLogger LOGGER = HdLogger.getLogger(JedisHelper.class);

	private JedisCluster jedisCluster;

	private String clusterNodes;

	private int connectionTimeout;

	private int soTimeout;

	private int maxAttempts;

	private String password;

	private GenericObjectPoolConfig poolConfig;

	private String consumerGroup;

	public void setClusterNodes(String clusterNodes) {
		this.clusterNodes = clusterNodes;
	}

	public void setConnectionTimeout(int connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}

	public void setSoTimeout(int soTimeout) {
		this.soTimeout = soTimeout;
	}

	public void setMaxAttempts(int maxAttempts) {
		this.maxAttempts = maxAttempts;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setPoolConfig(GenericObjectPoolConfig poolConfig) {
		this.poolConfig = poolConfig;
	}

	public String getConsumerGroup() {
		return consumerGroup;
	}

	public void setConsumerGroup(String consumerGroup) {
		this.consumerGroup = consumerGroup;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		try {
			Validate.notNull(clusterNodes);
			String[] clusterNodesArray = clusterNodes.split(",");
			Set<HostAndPort> hostAndPorts = new HashSet<>();
			for (String clusterNode : clusterNodesArray) {
				hostAndPorts.add(HostAndPort.parseString(clusterNode));
			}
			this.jedisCluster = new JedisCluster(hostAndPorts, connectionTimeout, soTimeout, maxAttempts, password, poolConfig);
		} catch (Exception e) {
			LOGGER.warn("Initialize JedisCluster error", e);
		}
	}

	/**
	 * 根据消息体中的hashcode和_catParentMessageId，以及consumerGroup进行重复消息判断，在一个小时内去重判断
	 * 
	 * @param messageExt
	 * @return
	 */
	public boolean isRepeatMessage(final MessageExt messageExt) {
		if (StringUtils.isBlank(consumerGroup)) {
			return false;
		}

		if (null == jedisCluster) {
			LOGGER.warn(LOG_MQ_CONSUMER_NOT_FILTER_REPEAT_MSG + ", can not filter repeat msgs.");
			return false;
		}

		try {
			if (messageExt.getReconsumeTimes() == 0) {
				String msgHashCode = messageExt.getUserProperty(MQConstants.HASH_CODE);
				String _catParentMessageId = messageExt.getUserProperty(TRACE_PARENT);
				if (StringUtils.isNotBlank(msgHashCode) && StringUtils.isNotBlank(_catParentMessageId)) {
					String key = buildMsgRepeatCheckKey(consumerGroup, msgHashCode, _catParentMessageId);
					final Long repeatCounter = jedisCluster.incr(key);
					jedisCluster.expire(key, MQConstants.ONE_HOUR);
					if (repeatCounter > 1) {
						LOGGER.info("msgHashCode:" + msgHashCode + ",_catParentMessageId:" + _catParentMessageId
								+ ",consumerGroup:" + consumerGroup + ",repeatCounter:" + repeatCounter + ",msgId:"
								+ messageExt.getMsgId());
						return true;
					}
				}
			}
		} catch (Throwable e) {
			LOGGER.error("isRepeatMessage error " + e.getMessage());
		}

		return false;
	}

	public void cleanRepeatMessageFlag(final MessageExt messageExt) {
		if (StringUtils.isBlank(consumerGroup)) {
			return;
		}

		if (null == jedisCluster) {
			return;
		}

		try {
			if (messageExt.getReconsumeTimes() == 0) {
				String msgHashCode = messageExt.getUserProperty(MQConstants.HASH_CODE);
				String _catParentMessageId = messageExt.getUserProperty(TRACE_PARENT);
				if (StringUtils.isNotBlank(msgHashCode) && StringUtils.isNotBlank(_catParentMessageId)) {
					jedisCluster.del(buildMsgRepeatCheckKey(consumerGroup, msgHashCode, _catParentMessageId));
				}
			}
		} catch (Throwable e) {
			LOGGER.error("cleanRepeatMessageFlag error " + e.getMessage());
		}
	}

	private static String buildMsgRepeatCheckKey(final String consumerGroup, String msgHashCode,
			String _catParentMessageId) {
		return "MQ:" + consumerGroup + ":" + msgHashCode + ":" + _catParentMessageId;
	}

}
