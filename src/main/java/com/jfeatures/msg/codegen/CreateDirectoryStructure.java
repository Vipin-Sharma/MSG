package com.jfeatures.msg.codegen;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CreateDirectoryStructure {
    public static void createDirectoryStructure() throws IOException
    {
        Path srcPath = Paths.get( System.getProperty("java.io.tmpdir")
                + File.separator + "generated"
                + File.separator + "src"
                + File.separator + "main"
                + File.separator + "java"
                + File.separator + "com"
                + File.separator + "jfeatures"
                + File.separator + "service");
        Path testPath = Paths.get(System.getProperty("java.io.tmpdir")
                + File.separator + "generated"
                + File.separator + "src"
                + File.separator + "test"
                + File.separator + "java"
                + File.separator + "com"
                + File.separator + "jfeatures"
                + File.separator + "service");

        Path resourcesPath = Paths.get(System.getProperty("java.io.tmpdir")
                + File.separator + "generated"
                + File.separator + "src"
                + File.separator + "main"
                + File.separator + "resources");

        Files.createDirectories(srcPath);
        Files.createDirectories(testPath);
        Files.createDirectories(resourcesPath);
    }
}
