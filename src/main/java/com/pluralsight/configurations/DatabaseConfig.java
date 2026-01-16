package com.pluralsight.configurations;

import org.apache.commons.dbcp2.BasicDataSource;

import javax.sql.DataSource;

public class DatabaseConfig {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/financial_tracker";
    private static final String DB_USERNAME = "root";
    private static final String DB_PASSWORD = "yearup";

    private static BasicDataSource dataSource;

    // Private constructor to prevent instantiation
    private DatabaseConfig() {}

    public static DataSource getDataSource() {
        if (dataSource == null) {
            dataSource = new BasicDataSource();
            dataSource.setUrl(DB_URL);
            dataSource.setUsername(DB_USERNAME);
            dataSource.setPassword(DB_PASSWORD);
        }
        return dataSource;
    }
}