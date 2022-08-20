package com.jfeatures.msg.sql;

import net.sf.jsqlparser.JSQLParserException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
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
        Map<String, String> ddlStringPerDdlName = ReadFileFromResources.readDDLsFromFile("/sample_ddl_for_tests.sql");
        Assertions.assertThat(ddlStringPerDdlName).hasSize(16);
        Assertions.assertThat(ddlStringPerDdlName.get("country"))
                .isEqualTo("CREATE TABLE country (  country_id INT NOT NULL IDENTITY ,  country VARCHAR(50) NOT NULL,  last_update DATETIME);");
    }

    @Test
    void readPropertiesFromFileWhenPassedValidFileNameShouldReturnProperties() throws IOException
    {
        Properties properties = ReadFileFromResources.readPropertiesFromFile("application_properties_file_for_tests.txt");
        properties.forEach((key, value) -> System.out.println(key + ": " + value));
        Assertions.assertThat(properties.size()).isEqualTo(8);
        Assertions.assertThat(properties.getProperty("spring.datasource.driver-class-name")).isEqualTo("com.microsoft.sqlserver.jdbc.SQLServerDriver");
    }

    @Test
    @Disabled("Need to use tescontainers for Database tests")
    void readDDLFromDatabaseWhenPassedValidConnectionShouldReturnDDL() throws IOException, ClassNotFoundException, SQLException {
        Map<String, Map<String, String>> tableDetailsFromDatabase = ReadFileFromResources.readTableDetailsFromDatabase("application_properties_file_for_tests.txt");
        Assertions.assertThat(tableDetailsFromDatabase.size()).isEqualTo(21);
        Assertions.assertThat(tableDetailsFromDatabase.get("country").get("country_id")).isEqualTo("int identity");
    }
}
