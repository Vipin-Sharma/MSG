package com.jfeatures.msg.sql;

import net.sf.jsqlparser.JSQLParserException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

class ReadFileFromResourcesTest {

    //todo CreateTable ddlStatement = (CreateTable) CCJSqlParserUtil.parse(ddl.toString(), parser -> parser.withSquareBracketQuotation(true)); Adwentureworks_ddls_for_test throws error on this
    @Test
    void readDDLsFromFile() throws IOException, URISyntaxException, JSQLParserException
    {
        Map<String, String> stringStringMap = ReadFileFromResources.readDDLsFromFile("/sample_ddl.sql");
        stringStringMap.forEach((key, value) -> System.out.println(key + ": " + value));
    }
}
