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

        String ddl = "";

        for (String line : allLines) {
            if(line.contains("CREATE TABLE"))
            {
                ddl = line;
            }
            else if(line.contains(";"))
            {
                ddl += line;
                //todo write method to extract table name from ddl
                //ddlPerTableName.put(ddl.split("\t")[0].split("].\\[")[1].split("]\\(")[0], ddl);
                ddlPerTableName.put(ddl.split("CREATE TABLE ")[1].split(" \\( ")[0], ddl);
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
