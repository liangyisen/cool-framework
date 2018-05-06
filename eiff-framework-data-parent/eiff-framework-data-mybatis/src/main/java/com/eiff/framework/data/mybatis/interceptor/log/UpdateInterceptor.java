package com.eiff.framework.data.mybatis.interceptor.log;

import java.sql.Connection;
import java.sql.Statement;
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

@Intercepts({ @Signature(type = StatementHandler.class, method = "update", args = { Statement.class }) })
public class UpdateInterceptor implements Interceptor, Constants {

	private static HdLogger LOGGER = HdLogger.getLogger(UpdateInterceptor.class);

	@Override
	public Object intercept(Invocation invocation) throws Throwable {
		Tracer buildTracer = LOGGER.buildTracer();
		Span span = buildTracer.createEmpty();
		String sql = null;
		String url = null;
		Object inputData = null;
		String method = "update";
		try {
			StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
			sql = statementHandler.getBoundSql().getSql();
			char startChar = sql.charAt(0);
			startChar = Character.toLowerCase(startChar);
			switch (startChar) {
			case 'i':
				method = "insert";
				break;
			case 'd':
				method = "delete";
				break;
			default:
				break;
			}
			Statement statement = (Statement) invocation.getArgs()[0];

			Connection connection = statement.getConnection();
			url = QueryInterceptor.getUrl(connection.getMetaData().getURL());

			span = buildTracer.createSpan(TRANS_TYPE_SQL, method + ".sql");

			span.addEvent(EVENT_TYPE_SQL_METHOD, method);
			inputData = statementHandler.getParameterHandler().getParameterObject();
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug(LOG_JDBC_IN_MSG, sql, url);
			}
		} catch (Throwable e) {
			LOGGER.error(LOG_FAILED_TO_CREATE_TRANS, e);
		}

		Object object = null;
		try {
			object = invocation.proceed();
			span.success();
			return object;
		} catch (Throwable e) {
			String reflectionToString = "" + inputData;
			LOGGER.warn(LOG_JDBC_EX_MSG, sql, url, reflectionToString);
			span.failed(e);
			throw e;
		} finally {
			span.addEvent(EVENT_TYPE_SQL_DATABASE, url);
			long durationInMillis = span.getDurationInMillis();
			if (durationInMillis > QueryInterceptor.LONG_SQL) {
				span.addData("sql", sql);
				LOGGER.warn(LOG_JDBC_LONG_SQL, sql);
			}
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
