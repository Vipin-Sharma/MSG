package com.jfeatures.msg.sql;

import lombok.extern.java.Log;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.create.table.CreateTable;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Log
public class ReadFileFromResources {
    public static Map<String, String> readDDLsFromFile(String filePath) throws IOException, JSQLParserException {
        String content;
        try (var inputStream = ReadFileFromResources.class.getResourceAsStream("/" + filePath)) {
            content = new String(inputStream.readAllBytes());
        }
        Map<String, String> ddlPerTableName = new HashMap<>();

        StringBuilder ddl = new StringBuilder();

        for (String line : content.split(System.lineSeparator())) {
            if(line.contains("CREATE TABLE"))
            {
                ddl = new StringBuilder(line);
            }
            else if(line.contains(";"))
            {
                ddl.append(line);
                CreateTable ddlStatement = (CreateTable) CCJSqlParserUtil.parse(ddl.toString(), parser -> parser.withSquareBracketQuotation(true));
                String tableName = ddlStatement.getTable().getName();
                tableName = tableName.trim();
                tableName = tableName.replace("[", "");
                tableName = tableName.replace("]", "");

                String ddlString = ddl.toString();
                ddlString = ddlString.replace("[", "");
                ddlString = ddlString.replace("]", "");

                ddlPerTableName.put(tableName, ddlString);
                ddl = new StringBuilder();
            }
            else
            {
                ddl.append(line);
            }
        }

        return ddlPerTableName;
    }

    public static Map<String, Map<String, String>> readTableDetailsFromDatabase(String propertyFileName) throws IOException, ClassNotFoundException, SQLException {
        Properties properties = readPropertiesFromFile(propertyFileName);

        String driverClassName = properties.getProperty("spring.datasource.driver-class-name");
        Class.forName(driverClassName);

        DatabaseMetaData databaseMetaData;
        try (Connection connection = DriverManager.getConnection(properties.getProperty("spring.datasource.url"), properties.getProperty("spring.datasource.username"), properties.getProperty("spring.datasource.password"))) {
             databaseMetaData = connection.getMetaData();
        }
        ResultSet metaDataColumns = databaseMetaData.getColumns(null, properties.getProperty("msg.currentSchema"), "%", "%");
        Map<String, Map<String, String>> ddlPerTableName = new HashMap<>();
        while (metaDataColumns.next()) {
            String tableName = metaDataColumns.getString("TABLE_NAME");
            ResultSet columnResult = databaseMetaData.getColumns(null, null, tableName, "%");

            Map<String, String> columnNameVsType = new HashMap<>();

            while (columnResult.next()) {
                String columnName = columnResult.getString("COLUMN_NAME");
                String columnType = columnResult.getString("TYPE_NAME");
                columnNameVsType.put(columnName, columnType);
            }
            ddlPerTableName.put(tableName, columnNameVsType);
        }

        return ddlPerTableName;
    }

    public static Properties readPropertiesFromFile(String propertyFileName) throws IOException {
        System.out.println("Loading properties from resources directory");
        Properties prop = new Properties();
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        InputStream stream = loader.getResourceAsStream(propertyFileName);
        prop.load(stream);
        return prop;
    }

    public static String readFileFromResources(String fileName) {
        try (var inputStream = ReadFileFromResources.class.getClassLoader().getResourceAsStream(fileName)) {
            return new String(inputStream.readAllBytes());
        } catch (IOException e) {
            throw new UncheckedIOException("Error reading file from resources: " + e.getMessage(), e);
        }
    }
}
