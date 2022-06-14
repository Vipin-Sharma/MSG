package com.jfeatures.msg.sql;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReadDDL {
    public static Map<String, String> readDDLsFromFile(String filePath) throws IOException, URISyntaxException {

        List<String> allLines = Files.readAllLines(Path.of(ReadDDL.class.getResource(filePath).toURI()));

        Map<String, String> ddlPerTableName = new HashMap<>();

        String ddl = "";

        for (String line : allLines) {
            if(line.contains("CREATE TABLE"))
            {
                ddl = line;
            }
            else if(line.contains(";"))
            {
                ddlPerTableName.put(ddl.split(" ")[2], ddl);
                ddl = "";
            }
            else
            {
                ddl += line;
            }
        }

        return ddlPerTableName;

    }
}
