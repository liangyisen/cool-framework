package com.eiff.framework.test.utils;

import javax.sql.DataSource;

import org.dbunit.database.DatabaseConnection;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.DataSourceUtils;

public class DbUnitTool {

	public static void prepare(DataSource dataSource, String xmlFile) {
		FlatXmlDataSetBuilder builder = new FlatXmlDataSetBuilder();
		ClassPathResource classPathResource = new ClassPathResource(xmlFile);
		try {
			FlatXmlDataSet set = builder.build(classPathResource.getFile());
			DatabaseOperation.CLEAN_INSERT.execute(new DatabaseConnection(DataSourceUtils.getConnection(dataSource)),
					set);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void teardown(DataSource dataSource, String xmlFile) {
		FlatXmlDataSetBuilder builder = new FlatXmlDataSetBuilder();
		ClassPathResource classPathResource = new ClassPathResource(xmlFile);
		try {
			FlatXmlDataSet set = builder.build(classPathResource.getFile());
			DatabaseOperation.DELETE.execute(new DatabaseConnection(DataSourceUtils.getConnection(dataSource)), set);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void teardown(DataSource dataSource, String xmlFile, boolean removeAll) {
		if (removeAll) {
			FlatXmlDataSetBuilder builder = new FlatXmlDataSetBuilder();
			ClassPathResource classPathResource = new ClassPathResource(xmlFile);
			try {
				FlatXmlDataSet set = builder.build(classPathResource.getFile());
				DatabaseOperation.DELETE_ALL.execute(new DatabaseConnection(DataSourceUtils.getConnection(dataSource)),
						set);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			teardown(dataSource, xmlFile);
		}
	}
}
