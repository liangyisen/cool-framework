package com.eiff.framework.log.api.alert;

public interface AlertSender {

	/**
	 * 每次调用都会触发一次告警，需要慎重使用是否会造成太多的告警
	 * 
	 * @param alertKey
	 *            告警key
	 * @param alertComments
	 *            告警详细内容
	 */
	void logError(String alertKey, String alertComments);

	/**
	 * 可以按照一定时间段发生的次数配置告警
	 * 
	 * @param alertKey
	 *            告警key
	 * @param alertComments
	 *            告警详细内容
	 */
	void logWarning(String alertKey, String alertComments);

	void logError(String domain, String alertKey, String alertComments);

	void logWarning(String domain, String alertKey, String alertComments);
}
