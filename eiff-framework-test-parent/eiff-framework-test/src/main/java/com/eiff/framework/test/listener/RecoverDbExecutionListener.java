package com.eiff.framework.test.listener;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.AbstractDriverBasedDataSource;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;
import org.springframework.test.jdbc.JdbcTestUtils;

import com.eiff.framework.test.annotation.RecoverDb;

public class RecoverDbExecutionListener extends AbstractTestExecutionListener {

	@Override
	public void beforeTestMethod(TestContext testContext) throws Exception {
		super.beforeTestMethod(testContext);

		ApplicationContext applicationContext = testContext.getApplicationContext();
		RecoverDb annotationOnClass = testContext.getTestClass().getAnnotation(RecoverDb.class);
		if (annotationOnClass == null) {
			RecoverDb annotationOnMethod = testContext.getTestMethod().getAnnotation(RecoverDb.class);
			handleOnAnnotation(applicationContext, annotationOnMethod);
		} else {
			handleOnAnnotation(applicationContext, annotationOnClass);
		}
	}

	@SuppressWarnings("deprecation")
	private void handleOnAnnotation(ApplicationContext applicationContext, RecoverDb annotationOnClass)
			throws Exception {
		if (annotationOnClass == null)
			return;
		String datasource = annotationOnClass.datasource();
		String[] scripts = annotationOnClass.scripts();
		AbstractDriverBasedDataSource dataSource = (AbstractDriverBasedDataSource) applicationContext
				.getBean(datasource);
		dropAllTheTable(dataSource.getConnection());
		for (String string : scripts) {
			Resource resource = applicationContext.getResource(string);
			JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
			JdbcTestUtils.executeSqlScript(jdbcTemplate, new EncodedResource(resource, "utf8"), false);
		}
	}

	private void dropAllTheTable(Connection connection) throws Exception {
		Statement statement = connection.createStatement();
		String sql = "show tables";
		ResultSet executeQuery = statement.executeQuery(sql);
		while (executeQuery.next()) {
			String tableName = executeQuery.getString(1);
			String dropTableSql = "drop table " + tableName;
			Statement dropTableStatement = connection.createStatement();
			dropTableStatement.executeUpdate(dropTableSql);
			dropTableStatement.close();
		}
	}
}
