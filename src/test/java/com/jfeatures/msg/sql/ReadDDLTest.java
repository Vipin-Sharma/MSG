package com.jfeatures.msg.sql;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ReadDDLTest {

    @Test
    void readDDLsFromFile() throws IOException, URISyntaxException {
        Map<String, String> stringStringMap = ReadDDL.readDDLsFromFile("/Adwentureworks_ddls_for_test.txt");
        stringStringMap.forEach((key, value) -> System.out.println(key + ": " + value));
    }
}
