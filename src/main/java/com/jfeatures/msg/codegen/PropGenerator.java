package com.jfeatures.msg.codegen;

import com.jfeatures.msg.codegen.maven.PomGenerator;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PropGenerator {
    public static Path generatePropertiesFile() throws IOException {

        Path resourcesPath = Paths.get(System.getProperty("java.io.tmpdir")
                + File.separator + "generated"
                + File.separator + "src"
                + File.separator + "main"
                + File.separator + "resources"
                + File.separator + "application.properties");

        InputStream inputStream = PomGenerator.class.getClassLoader().getResourceAsStream("application_properties_file.txt");
        assert inputStream != null;
        return Files.write(resourcesPath, inputStream.readAllBytes());
    }
}
