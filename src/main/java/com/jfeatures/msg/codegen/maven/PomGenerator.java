package com.jfeatures.msg.codegen.maven;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PomGenerator {
    public static Path generatePomFile() throws IOException {
        //Path pomPath = Paths.get("pom.xml");
        Path pomPath = Paths.get(System.getProperty("java.io.tmpdir")
                + File.separator + "generated"
                + File.separator + "pom.xml");
        //InputStream inputStream = PomGenerator.class.getResourceAsStream("/pom_file.xml");
        InputStream inputStream = PomGenerator.class.getClassLoader().getResourceAsStream("pom_file.xml");
        assert inputStream != null;
        return Files.write(pomPath, inputStream.readAllBytes());
    }
}
