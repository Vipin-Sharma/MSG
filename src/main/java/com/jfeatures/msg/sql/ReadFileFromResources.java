package com.jfeatures.msg.sql;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReadFileFromResources {
    public static Map<String, String> readDDLsFromFile(String filePath) throws IOException, URISyntaxException {

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
                //todo write method to extract table name from ddl
                //ddlPerTableName.put(ddl.split("\t")[0].split("].\\[")[1].split("]\\(")[0], ddl);
                //todo add better support `ddl.toString().split("CREATE TABLE ")[1].split(" \\( ")[0]` should also work
                String tableName = ddl.toString().split("CREATE TABLE ")[1].split("\\( ")[0];
                ddlPerTableName.put(StringUtils.upperCase(tableName), ddl.toString());
                ddl = new StringBuilder();
            }
            else
            {
                ddl.append(line);
            }
        }

        return ddlPerTableName;

    }

    public static Map<String, String> readDDLFromDatabase(String databaseName)
    {
        //todo
        return null;
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
