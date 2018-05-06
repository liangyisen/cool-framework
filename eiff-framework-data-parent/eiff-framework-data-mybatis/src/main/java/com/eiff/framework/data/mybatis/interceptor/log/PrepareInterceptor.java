package com.eiff.framework.data.mybatis.interceptor.log;

import java.sql.Connection;
import java.util.Properties;

import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;

import com.eiff.framework.log.api.Constants;
import com.eiff.framework.log.api.HdLogger;
import com.eiff.framework.log.api.trace.Span;
import com.eiff.framework.log.api.trace.Tracer;

@Intercepts({ @Signature(type = StatementHandler.class, method = "prepare", args = { Connection.class }) })
public class PrepareInterceptor implements Interceptor, Constants {

	private static HdLogger LOGGER = HdLogger.getLogger(PrepareInterceptor.class);

	@Override
	public Object intercept(Invocation invocation) throws Throwable {
		Tracer buildTracer = LOGGER.buildTracer();
		Span span = buildTracer.createEmpty();
		String sql = null;
		String url = null;
		try {
			StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
			sql = statementHandler.getBoundSql().getSql();
			Connection connection = (Connection) invocation.getArgs()[0];
			url = connection.getMetaData().getURL();

			span	 = buildTracer.createSpan(TRANS_TYPE_JDBC, sql);
			LOGGER.info(LOG_JDBC_IN_MSG, sql, url);
			span.addEvent(TRANS_TYPE_JDBC + ".url", url);
		} catch (Throwable e) {
			LOGGER.error(LOG_FAILED_TO_CREATE_TRANS, e);
		}

		Object object = null;
		try {
			object = invocation.proceed();
			span.success();
			return object;
		} catch (Throwable e) {
			span.failed(e);
			throw e;
		} finally {
			LOGGER.info(LOG_JDBC_OUT_MSG, sql, url);
			span.close();
		}
	}

	@Override
	public Object plugin(Object target) {
		return Plugin.wrap(target, this);
	}

	@Override
	public void setProperties(Properties properties) {
	}
}
