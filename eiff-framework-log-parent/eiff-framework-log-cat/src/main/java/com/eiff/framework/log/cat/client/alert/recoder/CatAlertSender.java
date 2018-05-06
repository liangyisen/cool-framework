package com.eiff.framework.log.cat.client.alert.recoder;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;
import com.eiff.framework.log.api.Constants;
import com.eiff.framework.log.api.alert.AlertSender;

public class CatAlertSender implements AlertSender {

	private CatAlertSender() {
	}

	/**
	 * 每次调用都会触发一次告警，需要慎重使用是否会造成太多的告警
	 * 
	 * @param alertKey
	 *            告警key
	 * @param alertComments
	 *            告警详细内容
	 */
	public void logError(String alertKey, String alertComments) {
		Cat.logEvent(Constants.ALERT_AT_ONCE, alertKey, "1", alertComments);
	}

	/**
	 * 可以按照一定时间段发生的次数配置告警
	 * 
	 * @param alertKey
	 *            告警key
	 * @param alertComments
	 *            告警详细内容
	 */
	public void logWarning(String alertKey, String alertComments) {
		Cat.logEvent(Constants.ALERT_NOT_AT_ONCE, alertKey, "1", alertComments);
	}

	public void logError(String domain, String alertKey, String alertComments) {
		Transaction transaction = Cat.newTransaction(Constants.ALERT_NOT_AT_ONCE, Constants.ALERT_NOT_AT_ONCE);
		DefaultMessageTree tree = (DefaultMessageTree) Cat.getManager().getThreadLocalMessageTree();
		String currentDomain = "";
		if (tree != null) {
			currentDomain = tree.getDomain();
			tree.setDomain(domain);
		}
		Cat.logEvent(Constants.ALERT_AT_ONCE, alertKey, "1", alertComments);
		transaction.complete();
		if (tree != null) {
			tree.setDomain(currentDomain);
		}
	}

	public void logWarning(String domain, String alertKey, String alertComments) {
		Transaction transaction = Cat.newTransaction(Constants.ALERT_NOT_AT_ONCE, Constants.ALERT_NOT_AT_ONCE);
		DefaultMessageTree tree = (DefaultMessageTree) Cat.getManager().getThreadLocalMessageTree();
		String currentDomain = "";
		if (tree != null) {
			currentDomain = tree.getDomain();
			tree.setDomain(domain);
		}
		Cat.logEvent(Constants.ALERT_NOT_AT_ONCE, alertKey, "1", alertComments);
		transaction.complete();
		if (tree != null) {
			tree.setDomain(currentDomain);
		}
	}
}
