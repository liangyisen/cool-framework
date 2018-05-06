package com.eiff.framework.data.mybatis.interceptor.log;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;

import com.eiff.framework.log.api.Constants;
import com.eiff.framework.log.api.HdLogger;
import com.eiff.framework.log.api.trace.Span;
import com.eiff.framework.log.api.trace.Tracer;

@Intercepts({
		@Signature(type = StatementHandler.class, method = "query", args = { Statement.class, ResultHandler.class }) })
public class QueryInterceptor implements Interceptor, Constants {

	// ms
	public static long LONG_SQL = 450l;
	public final static Map<String, String> DB_URL_MAPPER = new ConcurrentHashMap<>();

	private static HdLogger LOGGER = HdLogger.getLogger(QueryInterceptor.class);

	@Override
	public Object intercept(Invocation invocation) throws Throwable {
		Tracer buildTracer = LOGGER.buildTracer();
		Span span = buildTracer.createEmpty();
		String sql = null;
		String url = null;
		Object inputData = null;
		try {
			StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
			sql = statementHandler.getBoundSql().getSql();
			Statement statement = (Statement) invocation.getArgs()[0];
			Connection connection = statement.getConnection();
			url = getUrl(connection.getMetaData().getURL());

			span = buildTracer.createSpan(TRANS_TYPE_SQL, "select.sql");
			span.addEvent(EVENT_TYPE_SQL_METHOD, "select");
			inputData = statementHandler.getParameterHandler().getParameterObject();
			if (LOGGER.isDebugEnabled()) {
				String reflectionToString = "" + inputData;
				LOGGER.debug(LOG_JDBC_IN_MSG, sql, url, reflectionToString);
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
			if (durationInMillis > LONG_SQL) {
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

	public static String getUrl(String orUrl) {
		if (DB_URL_MAPPER.get(orUrl) == null) {
			if (DB_URL_MAPPER.size() > 100) {
				return orUrl.split("\\?")[0];
			}
			DB_URL_MAPPER.put(orUrl, orUrl.split("\\?")[0]);
		}
		return DB_URL_MAPPER.get(orUrl);
	}
}
