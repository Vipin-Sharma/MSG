package com.jfeatures.msg.sql;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReadDDL {
    public static Map<String, String> readDDLsFromFile(String filePath) throws IOException, URISyntaxException {

        URL resource = ReadDDL.class.getResource(filePath);
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
                ddlPerTableName.put(ddl.toString().split("CREATE TABLE ")[1].split(" \\( ")[0], ddl.toString());
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
}
