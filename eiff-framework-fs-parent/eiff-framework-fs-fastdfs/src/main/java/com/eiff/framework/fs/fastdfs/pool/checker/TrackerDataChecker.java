package com.eiff.framework.fs.fastdfs.pool.checker;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eiff.framework.fs.fastdfs.ProtoCommon;
import com.eiff.framework.fs.fastdfs.TrackerServer;
import com.eiff.framework.fs.fastdfs.conf.NonGlobalConfig;

public class TrackerDataChecker extends Thread {
	private static Logger LOGGER = LoggerFactory.getLogger(TrackerDataChecker.class);

	private NonGlobalConfig config;
	private CopyOnWriteArrayList<String> inusedTracker;
	private final List<String> configedTracker;

	public TrackerDataChecker(NonGlobalConfig config, CopyOnWriteArrayList<String> keys) {
		this.config = config;
		this.inusedTracker = keys;
		CollectionUtils.addAll(this.inusedTracker, this.config.getTrackerServers());	
		this.configedTracker = new ArrayList<>();
		CollectionUtils.addAll(this.configedTracker, this.config.getTrackerServers());	
	}

	@Override
	public void run() {
		while (true) {
			List<TrackerServer> checkServers = new ArrayList<>();
			try {
				for (int i = 0; i < configedTracker.size(); i++) {
					TrackerServer connection = this.config.getTrackerGroup().getConnection(i);
					checkServers.add(connection);
					if (ProtoCommon.activeTest(connection.getSocket())) {
						if (this.inusedTracker.contains(configedTracker.get(i))) {
							continue;
						} else {
							this.inusedTracker.add(configedTracker.get(i));
						}
					} else {
						this.inusedTracker.remove(configedTracker.get(i));
					}
				}
			} catch (Exception e) {
				LOGGER.error("checkfailed", e);
			} finally {
				if (checkServers.size() > 0) {
					for (TrackerServer trackerServer : checkServers) {
						try {
							if (trackerServer != null) {
								trackerServer.close();
							}
						} catch (Exception e) {
							LOGGER.error("close failed", e);
						}
					}
					checkServers.clear();
				}
			}

			try {
				TimeUnit.SECONDS.sleep(60);
			} catch (InterruptedException e) {
			}
		}
	}
}
