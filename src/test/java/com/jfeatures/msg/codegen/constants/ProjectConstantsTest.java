package com.jfeatures.msg.codegen.constants;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import org.junit.jupiter.api.Test;

class ProjectConstantsTest {

    @Test
    void testValidationPatterns() {
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