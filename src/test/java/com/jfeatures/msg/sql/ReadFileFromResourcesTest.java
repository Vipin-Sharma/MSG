package com.jfeatures.msg.sql;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

class ReadFileFromResourcesTest {

    @Test
    void readDDLsFromFile() throws IOException, URISyntaxException {
        Map<String, String> stringStringMap = ReadFileFromResources.readDDLsFromFile("/Adwentureworks_ddls_for_test.txt");
        stringStringMap.forEach((key, value) -> System.out.println(key + ": " + value));
    }
}
