package com.jfeatures.msg.codegen.constants;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import org.junit.jupiter.api.Test;

class CodeGenerationConstantsTest {

    @Test
    void testUtilityClassCannotBeInstantiated() throws Exception {
        // Verify that the utility class has a private constructor
        Constructor<CodeGenerationConstants> constructor = CodeGenerationConstants.class.getDeclaredConstructor();
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
    void testFieldNames() {
        // Test field names used in generated code
        assertEquals("namedParameterJdbcTemplate", CodeGenerationConstants.JDBC_TEMPLATE_FIELD_NAME);
        assertEquals("SQL", CodeGenerationConstants.SQL_FIELD_NAME);
        
        // Verify field names are not empty
        assertFalse(CodeGenerationConstants.JDBC_TEMPLATE_FIELD_NAME.isEmpty());
        assertFalse(CodeGenerationConstants.SQL_FIELD_NAME.isEmpty());
    }

    @Test
    void testBuilderPatternThreshold() {
        // Test builder pattern threshold
        assertEquals(255, CodeGenerationConstants.BUILDER_PATTERN_FIELD_THRESHOLD);
        
        // Verify threshold is a reasonable positive number
        assertTrue(CodeGenerationConstants.BUILDER_PATTERN_FIELD_THRESHOLD > 0);
        assertTrue(CodeGenerationConstants.BUILDER_PATTERN_FIELD_THRESHOLD <= 1000); // Reasonable upper bound
    }

    @Test
    void testCodeGenerationPatterns() {
        // Test code generation pattern constants
        assertEquals("Builder", CodeGenerationConstants.BUILDER_VARIABLE_NAME);
        assertEquals("dto", CodeGenerationConstants.DTO_VARIABLE_NAME);
        assertEquals("result", CodeGenerationConstants.RESULT_LIST_NAME);
        
        // Verify pattern names are not empty
        assertFalse(CodeGenerationConstants.BUILDER_VARIABLE_NAME.isEmpty());
        assertFalse(CodeGenerationConstants.DTO_VARIABLE_NAME.isEmpty());
        assertFalse(CodeGenerationConstants.RESULT_LIST_NAME.isEmpty());
    }

    @Test
    void testMethodPrefixes() {
        // Test method prefixes
        assertEquals("get", CodeGenerationConstants.DAO_METHOD_PREFIX);
        assertEquals("set", CodeGenerationConstants.SETTER_METHOD_PREFIX);
        
        // Verify prefixes are not empty
        assertFalse(CodeGenerationConstants.DAO_METHOD_PREFIX.isEmpty());
        assertFalse(CodeGenerationConstants.SETTER_METHOD_PREFIX.isEmpty());
        
        // Verify prefixes are lowercase (Java convention)
        assertEquals(CodeGenerationConstants.DAO_METHOD_PREFIX.toLowerCase(), 
                    CodeGenerationConstants.DAO_METHOD_PREFIX);
        assertEquals(CodeGenerationConstants.SETTER_METHOD_PREFIX.toLowerCase(), 
                    CodeGenerationConstants.SETTER_METHOD_PREFIX);
    }

    @Test
    void testConstantsAreNotNull() {
        // Verify all constants are not null
        assertNotNull(CodeGenerationConstants.JDBC_TEMPLATE_FIELD_NAME);
        assertNotNull(CodeGenerationConstants.SQL_FIELD_NAME);
        assertNotNull(CodeGenerationConstants.BUILDER_VARIABLE_NAME);
        assertNotNull(CodeGenerationConstants.DTO_VARIABLE_NAME);
        assertNotNull(CodeGenerationConstants.RESULT_LIST_NAME);
        assertNotNull(CodeGenerationConstants.DAO_METHOD_PREFIX);
        assertNotNull(CodeGenerationConstants.SETTER_METHOD_PREFIX);
    }

    @Test
    void testJavaConventions() {
        // Test that constants follow Java naming conventions
        
        // Field names should be camelCase
        assertTrue(Character.isLowerCase(CodeGenerationConstants.JDBC_TEMPLATE_FIELD_NAME.charAt(0)));
        assertTrue(Character.isLowerCase(CodeGenerationConstants.DTO_VARIABLE_NAME.charAt(0)));
        assertTrue(Character.isLowerCase(CodeGenerationConstants.RESULT_LIST_NAME.charAt(0)));
        
        // SQL field name should be uppercase (constant convention)
        assertTrue(CodeGenerationConstants.SQL_FIELD_NAME.equals(CodeGenerationConstants.SQL_FIELD_NAME.toUpperCase()));
        
        // Builder name should be PascalCase
        assertTrue(Character.isUpperCase(CodeGenerationConstants.BUILDER_VARIABLE_NAME.charAt(0)));
    }

    @Test
    void testBuilderPatternThresholdUsage() {
        // Test that the threshold value makes sense for builder pattern usage
        int threshold = CodeGenerationConstants.BUILDER_PATTERN_FIELD_THRESHOLD;
        
        // Should trigger builder for large field counts
        assertTrue(threshold >= 3, "Threshold should be at least 3 for meaningful builder pattern usage");
        
        // Should not be too high to be practical
        assertTrue(threshold <= 1000, "Threshold should not be impractically high");
        
        // Common use case: 255 is a reasonable threshold for builder pattern
        assertEquals(255, threshold, "Expected threshold value for builder pattern");
    }

    @Test
    void testVariableNameConsistency() {
        // Test that variable names are consistent with Java conventions
        
        // DTO variable name should be simple and descriptive
        assertEquals("dto", CodeGenerationConstants.DTO_VARIABLE_NAME);
        assertTrue(CodeGenerationConstants.DTO_VARIABLE_NAME.length() <= 10, "Variable name should be concise");
        
        // Result list name should be descriptive
        assertEquals("result", CodeGenerationConstants.RESULT_LIST_NAME);
        assertTrue(CodeGenerationConstants.RESULT_LIST_NAME.length() <= 20, "Result name should be concise");
        
        // JDBC template field name should be descriptive
        assertTrue(CodeGenerationConstants.JDBC_TEMPLATE_FIELD_NAME.contains("JdbcTemplate"));
    }

    @Test
    void testMethodPrefixConsistency() {
        // Test that method prefixes follow Java Bean conventions
        assertEquals("get", CodeGenerationConstants.DAO_METHOD_PREFIX);
        assertEquals("set", CodeGenerationConstants.SETTER_METHOD_PREFIX);
        
        // Verify they are proper Java method prefixes
        assertTrue(CodeGenerationConstants.DAO_METHOD_PREFIX.matches("^[a-z][a-zA-Z]*$"));
        assertTrue(CodeGenerationConstants.SETTER_METHOD_PREFIX.matches("^[a-z][a-zA-Z]*$"));
    }
}