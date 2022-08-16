package com.jfeatures.msg.sql;

import net.sf.jsqlparser.JSQLParserException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

class ReadFileFromResourcesTest {

    //todo CreateTable ddlStatement = (CreateTable) CCJSqlParserUtil.parse(ddl.toString(), parser -> parser.withSquareBracketQuotation(true)); Adwentureworks_ddls_for_test throws error on this
    @Test
    void readDDLsFromFile() throws IOException, URISyntaxException, JSQLParserException
    {
        Map<String, String> stringStringMap = ReadFileFromResources.readDDLsFromFile("/sample_ddl.sql");
        stringStringMap.forEach((key, value) -> System.out.println(key + ": " + value));
    }

    @Test
    void readPropertiesFromFileWhenPassedValidFileNameShouldReturnProperties() throws IOException
    {
        Properties properties = ReadFileFromResources.readPropertiesFromFile("application_properties_file.txt");
        properties.forEach((key, value) -> System.out.println(key + ": " + value));
    }

    @Test
    void readDDLFromDatabaseWhenPassedValidConnectionShouldReturnDDL() throws IOException, ClassNotFoundException, SQLException {
        Map<String, Map<String, String>> tableDetailsFromDatabase = ReadFileFromResources.readTableDetailsFromDatabase("application_properties_file.txt");
        tableDetailsFromDatabase.forEach((key, value) ->
        {
            System.out.println("Table name : " + key);
            value.forEach((key1, value1) -> System.out.println(key1 + ": " + value1));
        });
    }
}
