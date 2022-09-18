package com.jfeatures.msg.sql;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBVendorInfo {
    public static String getDatabaseProductName(String propertyFileName) throws SQLException, IOException {
        Properties properties = ReadFileFromResources.readPropertiesFromFile(propertyFileName);
        try (Connection connection = DriverManager.getConnection(properties.getProperty("spring.datasource.url"), properties.getProperty("spring.datasource.username"), properties.getProperty("spring.datasource.password"))) {
            DatabaseMetaData metaData = connection.getMetaData();
            return metaData.getDatabaseProductName();
        }
    }
}
