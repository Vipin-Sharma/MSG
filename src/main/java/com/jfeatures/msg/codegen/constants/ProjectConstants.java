package com.jfeatures.msg.codegen.constants;

import java.io.File;

/**
 * Constants used throughout the microservice generation process.
 * Centralizes all project-related constants to avoid magic strings and numbers.
 */
public final class ProjectConstants {
    
    // Private constructor to prevent instantiation of utility class
    private ProjectConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
    
    // Directory structure constants
    public static final String SRC = "src";
    public static final String MAIN = "main";
    public static final String TEST = "test";
    public static final String JAVA = "java";
    public static final String RESOURCES = "resources";
    public static final String COM = "com";
    public static final String JFEATURES = "jfeatures";
    
    // Maven standard directory paths
    public static final String SRC_MAIN_JAVA_PATH = SRC + File.separator + MAIN + File.separator + JAVA;
    public static final String SRC_TEST_JAVA_PATH = SRC + File.separator + TEST + File.separator + JAVA;
    public static final String SRC_MAIN_RESOURCES_PATH = SRC + File.separator + MAIN + File.separator + RESOURCES;
    
    // Default configuration values
    public static final String DEFAULT_BUSINESS_DOMAIN = "Customer";
    public static final String DEFAULT_DESTINATION_DIRECTORY = "/home/vipin/BusinessData";
    
    // Template file names
    public static final String POM_TEMPLATE_FILE = "pom_file.xml";
    public static final String APPLICATION_PROPERTIES_TEMPLATE_FILE = "application_properties_file.txt";
    
    // Generated file names
    public static final String POM_FILE_NAME = "pom.xml";
    public static final String APPLICATION_PROPERTIES_FILE_NAME = "application.properties";
    public static final String DATABASE_CONFIG_FILE_NAME = "DatabaseConfig.java";
    
    // SQL file names
    public static final String DEFAULT_UPDATE_SQL_FILE = "sample_update_parameterized.sql";
    public static final String DEFAULT_SELECT_SQL_FILE = "sample_parameterized_sql.sql";
    public static final String DEFAULT_INSERT_SQL_FILE = "sample_insert_parameterized.sql";
    public static final String DEFAULT_DELETE_SQL_FILE = "sample_delete_parameterized.sql";
    
    // Package path components
    public static final String JFEATURES_PACKAGE_PATH = COM + File.separator + JFEATURES;
}