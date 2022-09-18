package com.jfeatures.msg.sql;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;

import java.util.Map;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

class ReadFileFromResourcesTest {

    //todo CreateTable ddlStatement = (CreateTable) CCJSqlParserUtil.parse(ddl.toString(), parser -> parser.withSquareBracketQuotation(true)); Adwentureworks_ddls_for_test throws error on this
    @Test
    void readDDLsFromFile() throws Exception {
        Map<String, String> ddlStringPerDdlName = ReadFileFromResources.readDDLsFromFile("sample_ddl_for_tests.sql");
        Assertions.assertThat(ddlStringPerDdlName).hasSize(16);
        Assertions.assertThat(ddlStringPerDdlName.get("country"))
                .isEqualTo("CREATE TABLE country (  country_id INT NOT NULL IDENTITY ,  country VARCHAR(50) NOT NULL,  last_update DATETIME);");
    }

    @Test
    void readPropertiesFromFileWhenPassedValidFileNameShouldReturnProperties() throws IOException
    {
        Properties properties = ReadFileFromResources.readPropertiesFromFile("application_properties_file_for_tests.txt");
        properties.forEach((key, value) -> System.out.println(key + ": " + value));
        assertThat(properties).hasSize(8);
        assertThat(properties.getProperty("spring.datasource.driver-class-name")).isEqualTo("com.microsoft.sqlserver.jdbc.SQLServerDriver");
    }

    @Test
    @Disabled("Need to use tescontainers for Database tests")
    void readDDLFromDatabaseWhenPassedValidConnectionShouldReturnDDL() throws Exception {
        Map<String, Map<String, String>> tableDetailsFromDatabase = ReadFileFromResources.readTableDetailsFromDatabase("application_properties_file_for_tests.txt");
        assertThat(tableDetailsFromDatabase).hasSize(21);
        assertThat(tableDetailsFromDatabase.get("country")).containsEntry("country_id", "int identity");
    }
}
