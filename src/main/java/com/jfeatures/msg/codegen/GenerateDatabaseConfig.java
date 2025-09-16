package com.jfeatures.msg.codegen;

import com.jfeatures.msg.codegen.util.JavaPackageNameBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Generates DatabaseConfig class for Spring Boot application using template-based approach.
 * This class provides the necessary beans for database connectivity.
 */
public class GenerateDatabaseConfig {

    private GenerateDatabaseConfig() {
        throw new UnsupportedOperationException("Utility class");
    }

    private static final String TEMPLATE_PATH = "/templates/DatabaseConfig.java.template";
    
    public static String createDatabaseConfig(String businessPurposeOfSQL) {
        try (InputStream inputStream = GenerateDatabaseConfig.class.getResourceAsStream(TEMPLATE_PATH)) {
            if (inputStream == null) {
                throw new IllegalStateException("Template file not found: " + TEMPLATE_PATH);
            }
            
            String template = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            String packageName = JavaPackageNameBuilder.buildJavaPackageName(businessPurposeOfSQL, "config");
            
            return template
                    .replace("${packageName}", packageName)
                    .replace("${businessPurpose}", businessPurposeOfSQL);
                    
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read template file: " + TEMPLATE_PATH, e);
        }
    }
}
