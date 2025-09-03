package com.jfeatures.msg.codegen.constants;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class ProjectConstantsTest {

    @Test
    void testUtilityClassCannotBeInstantiated() throws Exception {
        // Verify that the utility class has a private constructor
        Constructor<ProjectConstants> constructor = ProjectConstants.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()), 
                  "Constructor should be private");

        // Make constructor accessible and verify it throws UnsupportedOperationException
        constructor.setAccessible(true);
        Exception exception = assertThrows(
            Exception.class,
            constructor::newInstance
        );
        // The actual UnsupportedOperationException is wrapped in InvocationTargetException
        Throwable cause = exception.getCause();
        assertInstanceOf(UnsupportedOperationException.class, cause);
        assertEquals("This is a utility class and cannot be instantiated", cause.getMessage());
    }

    @Test
    void shouldHaveDefaultDestinationDirectory() {
        // Then
        assertThat(ProjectConstants.DEFAULT_DESTINATION_DIRECTORY).isNotNull();
        assertThat(ProjectConstants.DEFAULT_DESTINATION_DIRECTORY).isNotEmpty();
    }

    @Test
    void shouldHaveDefaultBusinessDomain() {
        // Then
        assertThat(ProjectConstants.DEFAULT_BUSINESS_DOMAIN).isNotNull();
        assertThat(ProjectConstants.DEFAULT_BUSINESS_DOMAIN).isNotEmpty();
        assertThat(ProjectConstants.DEFAULT_BUSINESS_DOMAIN).isEqualTo("Customer");
    }

    @Test
    void testDirectoryConstants() {
        // Test basic directory components
        assertEquals("src", ProjectConstants.SRC);
        assertEquals("main", ProjectConstants.MAIN);
        assertEquals("test", ProjectConstants.TEST);
        assertEquals("java", ProjectConstants.JAVA);
        assertEquals("resources", ProjectConstants.RESOURCES);
        assertEquals("com", ProjectConstants.COM);
        assertEquals("jfeatures", ProjectConstants.JFEATURES);
    }

    @Test
    void testMavenDirectoryPaths() {
        // Test Maven standard directory paths
        String expectedSrcMainJava = "src" + File.separator + "main" + File.separator + "java";
        String expectedSrcTestJava = "src" + File.separator + "test" + File.separator + "java";
        String expectedSrcMainResources = "src" + File.separator + "main" + File.separator + "resources";

        assertEquals(expectedSrcMainJava, ProjectConstants.SRC_MAIN_JAVA_PATH);
        assertEquals(expectedSrcTestJava, ProjectConstants.SRC_TEST_JAVA_PATH);
        assertEquals(expectedSrcMainResources, ProjectConstants.SRC_MAIN_RESOURCES_PATH);
    }

    @Test
    void testTemplateFileNames() {
        // Test template file names
        assertEquals("pom_file.xml", ProjectConstants.POM_TEMPLATE_FILE);
        assertEquals("application_properties_file.txt", ProjectConstants.APPLICATION_PROPERTIES_TEMPLATE_FILE);
    }

    @Test
    void testGeneratedFileNames() {
        // Test generated file names
        assertEquals("pom.xml", ProjectConstants.POM_FILE_NAME);
        assertEquals("application.properties", ProjectConstants.APPLICATION_PROPERTIES_FILE_NAME);
        assertEquals("DatabaseConfig.java", ProjectConstants.DATABASE_CONFIG_FILE_NAME);
    }

    @Test
    void testSqlFileNames() {
        // Test SQL file names
        assertEquals("sample_update_parameterized.sql", ProjectConstants.DEFAULT_UPDATE_SQL_FILE);
        assertEquals("sample_parameterized_sql.sql", ProjectConstants.DEFAULT_SELECT_SQL_FILE);
        assertEquals("sample_insert_parameterized.sql", ProjectConstants.DEFAULT_INSERT_SQL_FILE);
        assertEquals("sample_delete_parameterized.sql", ProjectConstants.DEFAULT_DELETE_SQL_FILE);
    }

    @Test
    void testPackagePaths() {
        // Test package path components
        String expectedJfeaturesPath = "com" + File.separator + "jfeatures";
        assertEquals(expectedJfeaturesPath, ProjectConstants.JFEATURES_PACKAGE_PATH);
    }

    @Test
    void testValidationPatterns() {
        // Test validation patterns
        assertEquals("^[a-zA-Z][a-zA-Z0-9_]*$", ProjectConstants.VALID_BUSINESS_NAME_PATTERN);
        
        // Test that the pattern works correctly
        assertTrue("Customer".matches(ProjectConstants.VALID_BUSINESS_NAME_PATTERN));
        assertTrue("Order123".matches(ProjectConstants.VALID_BUSINESS_NAME_PATTERN));
        assertTrue("User_Service".matches(ProjectConstants.VALID_BUSINESS_NAME_PATTERN));
        
        // Test invalid patterns
        assertFalse("123Customer".matches(ProjectConstants.VALID_BUSINESS_NAME_PATTERN)); // starts with number
        assertFalse("Customer-Service".matches(ProjectConstants.VALID_BUSINESS_NAME_PATTERN)); // contains hyphen
        assertFalse("Customer Service".matches(ProjectConstants.VALID_BUSINESS_NAME_PATTERN)); // contains space
        assertFalse("".matches(ProjectConstants.VALID_BUSINESS_NAME_PATTERN)); // empty string
    }

    @Test
    void testErrorMessages() {
        // Test error messages
        assertEquals("Business purpose name cannot be null or empty", 
                    ProjectConstants.ERROR_NULL_BUSINESS_NAME);
        assertEquals("Destination directory cannot be null or empty", 
                    ProjectConstants.ERROR_NULL_DESTINATION);
        assertEquals("Business purpose name must start with a letter and contain only letters, numbers, and underscores", 
                    ProjectConstants.ERROR_INVALID_BUSINESS_NAME);
        assertEquals("SQL cannot be null or empty", 
                    ProjectConstants.ERROR_NULL_SQL);
        assertEquals("DataSource cannot be null", 
                    ProjectConstants.ERROR_NULL_DATASOURCE);
    }

    @Test
    void testFileSystemPathConsistency() {
        // Test that paths are constructed consistently
        assertTrue(ProjectConstants.SRC_MAIN_JAVA_PATH.startsWith(ProjectConstants.SRC));
        assertTrue(ProjectConstants.SRC_MAIN_JAVA_PATH.contains(ProjectConstants.MAIN));
        assertTrue(ProjectConstants.SRC_MAIN_JAVA_PATH.contains(ProjectConstants.JAVA));
        
        assertTrue(ProjectConstants.SRC_TEST_JAVA_PATH.startsWith(ProjectConstants.SRC));
        assertTrue(ProjectConstants.SRC_TEST_JAVA_PATH.contains(ProjectConstants.TEST));
        assertTrue(ProjectConstants.SRC_TEST_JAVA_PATH.contains(ProjectConstants.JAVA));
        
        assertTrue(ProjectConstants.SRC_MAIN_RESOURCES_PATH.startsWith(ProjectConstants.SRC));
        assertTrue(ProjectConstants.SRC_MAIN_RESOURCES_PATH.contains(ProjectConstants.MAIN));
        assertTrue(ProjectConstants.SRC_MAIN_RESOURCES_PATH.contains(ProjectConstants.RESOURCES));
    }
}