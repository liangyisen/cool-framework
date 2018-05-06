package com.eiff.framework.log.cat.client.impl.trace;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.StringUtils;

import com.dianping.cat.Cat;
import com.dianping.cat.Cat.Context;
import com.dianping.cat.CatConstants;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.spi.MessageTree;
import com.eiff.framework.log.api.HdLogger;
import com.eiff.framework.log.api.trace.Span;
import com.eiff.framework.log.api.trace.TraceHelper;
import com.eiff.framework.log.api.trace.TraceLinker;
import com.eiff.framework.log.api.trace.Tracer;
import com.eiff.framework.log.cat.client.concurrent.InTraceCallable;
import com.eiff.framework.log.cat.client.concurrent.InTraceRunnable;

public class CatTracer implements Tracer {
	HdLogger logger = HdLogger.getLogger(Tracer.class);

	@Override
	public Span createSpan(String type, String name) {
		return new CatSpan(type, name);
	}
	@Override
	public Span createEmpty() {
		return new CatSpan();
	}
	
	@Override
	public Map<String, String> getContext() {
		try {
			MessageTree tree = Cat.getManager().getThreadLocalMessageTree();
			String messageId = tree.getMessageId();

			if (messageId == null) {
				messageId = Cat.createMessageId();
				tree.setMessageId(messageId);
			}

			String childId1 = Cat.createMessageId();
			Cat.logEvent(CatConstants.TYPE_REMOTE_CALL, "", Event.SUCCESS, childId1);

			String root = tree.getRootMessageId();

			if (root == null) {
				root = messageId;
			}

			Map<String, String> map = new HashMap<String, String>();
			map.put(Context.ROOT, root);
			map.put(Context.PARENT, messageId);
			map.put(Context.CHILD + 1, childId1);

			return map;
		} catch (Exception e) {
			logger.error("", e);
		}

		return null;
	}

	@Override
	public Map<String, String> getContext4Async() {

		try {
			MessageTree tree = Cat.getManager().getThreadLocalMessageTree();
			String messageId = tree.getMessageId();

			if (messageId == null) {
				messageId = Cat.createMessageId();
				tree.setMessageId(messageId);
			}

			String childId = Cat.createMessageId();
			Cat.logEvent(CatConstants.TYPE_REMOTE_CALL, "", Event.SUCCESS, childId);

			String childId1 = Cat.createMessageId();
			Cat.logEvent(CatConstants.TYPE_REMOTE_CALL, "", Event.SUCCESS, childId1);

			String root = tree.getRootMessageId();

			if (root == null) {
				root = messageId;
			}

			Map<String, String> map = new HashMap<String, String>();
			map.put(Context.ROOT, root);
			map.put(Context.PARENT, messageId);
			map.put(Context.CHILD, childId);
			map.put(Context.CHILD + 1, childId1);

			return map;
		} catch (Exception e) {
			logger.error("", e);
		}

		return null;

	}

	@Override
	public void buildContext(Map<String, String> context, boolean reJoin) {
		if (context == null) {
			return;
		}
		MessageTree tree = Cat.getManager().getThreadLocalMessageTree();
		String messageId = context.get(Cat.Context.CHILD + 1);
		String parentId = context.get(Cat.Context.PARENT);
		String rootId = context.get(Cat.Context.ROOT);
		if (!reJoin) {
			if (parentId.equals(tree.getMessageId()) || StringUtils.isNotBlank(tree.getParentMessageId())) {
				return;
			}
		}
		if (StringUtils.isNotBlank(messageId) && StringUtils.isNotBlank(parentId) && StringUtils.isNotBlank(rootId)) {
			buildContext(messageId, parentId, rootId);
		}
	}

	public void buildContext(String messageId, String parentId, String rootId) {
		try {
			MessageTree tree = Cat.getManager().getThreadLocalMessageTree();
			if (messageId != null) {
				tree.setMessageId(messageId);
			}
			if (parentId != null) {
				tree.setParentMessageId(parentId);
			}
			if (rootId != null) {
				tree.setRootMessageId(rootId);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public <V> Callable<V> wrap(final Callable<V> callable) {
		return new InTraceCallable() {
			@Override
			public Object traceAbleCall() throws Exception {
				return callable.call();
			}
		};
	}

	@Override
	public Runnable wrap(final Runnable runner) {
		return new InTraceRunnable(runner.getClass().getSimpleName()) {
			@Override
			public void traceAbleRun() {
				runner.run();
			}
		};
	}

	@Override
	public String getTraceId() {
		MessageTree messageTree = null;
		try {
			messageTree = Cat.getManager().getThreadLocalMessageTree();
		} catch (Throwable e) {
			e.printStackTrace();
		}

		if (messageTree != null) {
			try {
				String traceId = messageTree.getRootMessageId();
				if (traceId == null) {
					traceId = Cat.getCurrentMessageId();
				}

				if (StringUtils.isNotBlank(traceId)) {
					return traceId.replaceAll("-", StringUtils.EMPTY);
				}
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}

		return null;

	}
	@Override
	public TraceHelper create(TraceLinker linker) {
		return new CatTraceHelper(linker);
	}
	
	@Override
	public String getDomainName() {
		return Cat.getDomainName();
	}
}
