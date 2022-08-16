package com.jfeatures.msg.sql;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.create.table.CreateTable;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class ReadFileFromResources {
    public static Map<String, String> readDDLsFromFile(String filePath) throws IOException, URISyntaxException, JSQLParserException {

        URL resource = ReadFileFromResources.class.getResource(filePath);
        assert resource != null;
        Path path = Path.of(resource.toURI());
        List<String> allLines = Files.readAllLines(path);

        Map<String, String> ddlPerTableName = new HashMap<>();

        StringBuilder ddl = new StringBuilder();

        for (String line : allLines) {
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

        String driverClassName = properties.getProperty("spring.datasource.driverClassName");
        Class.forName(driverClassName);

        Connection connection = DriverManager.getConnection(properties.getProperty("spring.datasource.url"), properties.getProperty("spring.datasource.username"), properties.getProperty("spring.datasource.password"));

        DatabaseMetaData databaseMetaData = connection.getMetaData();
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

    public static String readFileFromResources(String fileName) throws URISyntaxException
    {
        URL resource = ReadFileFromResources.class.getResource(fileName);
        assert resource != null;
        Path path = Path.of(resource.toURI());
        String fileContent = "";
        try {
            fileContent = new String(Files.readAllBytes(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileContent;
    }
}
