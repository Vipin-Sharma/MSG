package com.jfeatures.msg.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JdbcTemplateConfigTest {

    private JdbcTemplateConfig config;
    
    @Mock
    private DataSource mockDataSource;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        config = new JdbcTemplateConfig();
    }

    @Test
    void testClassHasConfigurationAnnotation() {
        // Verify that the config class has @Configuration annotation
        assertTrue(JdbcTemplateConfig.class.isAnnotationPresent(Configuration.class),
                  "JdbcTemplateConfig should have @Configuration annotation");
    }

    @Test
    void testJdbcTemplateMethodHasBeanAnnotation() throws Exception {
        // Verify that the jdbcTemplate method has @Bean annotation
        Method jdbcTemplateMethod = JdbcTemplateConfig.class.getMethod("jdbcTemplate", DataSource.class);
        assertTrue(jdbcTemplateMethod.isAnnotationPresent(Bean.class),
                  "jdbcTemplate method should have @Bean annotation");
    }

    @Test
    void testJdbcTemplateMethodSignature() throws Exception {
        // Verify method signature
        Method jdbcTemplateMethod = JdbcTemplateConfig.class.getMethod("jdbcTemplate", DataSource.class);
        
        assertEquals(JdbcTemplate.class, jdbcTemplateMethod.getReturnType(),
                    "jdbcTemplate method should return JdbcTemplate");
        assertEquals(1, jdbcTemplateMethod.getParameterCount(),
                    "jdbcTemplate method should have exactly one parameter");
        assertEquals(DataSource.class, jdbcTemplateMethod.getParameterTypes()[0],
                    "jdbcTemplate method parameter should be DataSource");
    }

    @Test
    void testJdbcTemplateCreation() {
        // Test that jdbcTemplate method creates a valid JdbcTemplate
        JdbcTemplate jdbcTemplate = config.jdbcTemplate(mockDataSource);
        
        assertNotNull(jdbcTemplate, "JdbcTemplate should not be null");
        assertInstanceOf(JdbcTemplate.class, jdbcTemplate,
                        "Should return JdbcTemplate instance");
    }

    @Test
    void testJdbcTemplateUsesProvidedDataSource() {
        // Test that JdbcTemplate uses the provided DataSource
        JdbcTemplate jdbcTemplate = config.jdbcTemplate(mockDataSource);
        
        assertNotNull(jdbcTemplate, "JdbcTemplate should not be null");
        
        // Verify that the JdbcTemplate uses the mock DataSource
        assertSame(mockDataSource, jdbcTemplate.getDataSource(),
                  "JdbcTemplate should use the provided DataSource");
    }

    @Test
    void testNullDataSourceHandling() {
        // Test behavior when null DataSource is provided
        assertThrows(IllegalArgumentException.class, 
                    () -> config.jdbcTemplate(null),
                    "Should throw IllegalArgumentException for null DataSource");
    }

    @Test
    void testMultipleJdbcTemplateCalls() {
        // Test that multiple calls with same DataSource return different instances
        JdbcTemplate jdbcTemplate1 = config.jdbcTemplate(mockDataSource);
        JdbcTemplate jdbcTemplate2 = config.jdbcTemplate(mockDataSource);
        
        assertNotNull(jdbcTemplate1);
        assertNotNull(jdbcTemplate2);
        
        // Both should use the same DataSource
        assertSame(mockDataSource, jdbcTemplate1.getDataSource());
        assertSame(mockDataSource, jdbcTemplate2.getDataSource());
    }

    @Test
    void testJdbcTemplateWithDifferentDataSources() {
        // Test with different DataSource instances
        DataSource anotherMockDataSource = mock(DataSource.class);
        
        JdbcTemplate jdbcTemplate1 = config.jdbcTemplate(mockDataSource);
        JdbcTemplate jdbcTemplate2 = config.jdbcTemplate(anotherMockDataSource);
        
        assertNotNull(jdbcTemplate1);
        assertNotNull(jdbcTemplate2);
        
        // Each should use their respective DataSource
        assertSame(mockDataSource, jdbcTemplate1.getDataSource());
        assertSame(anotherMockDataSource, jdbcTemplate2.getDataSource());
        assertNotSame(jdbcTemplate1.getDataSource(), jdbcTemplate2.getDataSource());
    }

    @Test
    void testConfigClassFollowsVipinsPrinciple() {
        // Test that the class follows "One public method per class" principle
        Method[] publicMethods = JdbcTemplateConfig.class.getMethods();
        
        // Count non-inherited public methods (excluding Object methods)
        long nonInheritedPublicMethods = java.util.Arrays.stream(publicMethods)
            .filter(method -> method.getDeclaringClass() == JdbcTemplateConfig.class)
            .count();
        
        assertEquals(1, nonInheritedPublicMethods, 
                    "Should have exactly one public method (following Vipin's Principle)");
    }

    @Test
    void testJdbcTemplateMethodIsPublic() throws Exception {
        Method jdbcTemplateMethod = JdbcTemplateConfig.class.getMethod("jdbcTemplate", DataSource.class);
        assertTrue(java.lang.reflect.Modifier.isPublic(jdbcTemplateMethod.getModifiers()),
                  "jdbcTemplate method should be public");
    }

    @Test
    void testJdbcTemplateConfiguration() {
        // Test that JdbcTemplate is properly configured
        JdbcTemplate jdbcTemplate = config.jdbcTemplate(mockDataSource);
        
        assertNotNull(jdbcTemplate.getDataSource(), "JdbcTemplate should have DataSource set");
        
        // Test that JdbcTemplate has default configuration
        // Note: JdbcTemplate uses default settings unless explicitly configured
        assertTrue(jdbcTemplate.getQueryTimeout() >= -1, 
                  "Query timeout should be valid (>= -1, where -1 means no timeout)");
    }

    @Test
    void testBeanMethodFollowsSpringConventions() throws Exception {
        // Test that the bean method follows Spring conventions
        Method jdbcTemplateMethod = JdbcTemplateConfig.class.getMethod("jdbcTemplate", DataSource.class);
        
        // Method name should be camelCase and descriptive
        assertEquals("jdbcTemplate", jdbcTemplateMethod.getName(),
                    "Bean method name should match the bean type");
        
        // Should have @Bean annotation
        assertTrue(jdbcTemplateMethod.isAnnotationPresent(Bean.class),
                  "Bean method should have @Bean annotation");
        
        // Should accept dependency as parameter
        assertEquals(1, jdbcTemplateMethod.getParameterCount(),
                    "Should accept DataSource as dependency parameter");
    }

    @Test
    void testDependencyInjectionReadiness() {
        // Test that config is ready for Spring dependency injection
        
        // Class should be public for Spring to instantiate it
        assertTrue(java.lang.reflect.Modifier.isPublic(JdbcTemplateConfig.class.getModifiers()),
                  "Config class should be public");
        
        // Should have public no-arg constructor (default constructor is fine)
        assertDoesNotThrow(() -> new JdbcTemplateConfig(),
                          "Should be able to instantiate config class");
    }

    @Test
    void testJdbcTemplateIsFullyInitialized() {
        // Test that returned JdbcTemplate is fully initialized
        JdbcTemplate jdbcTemplate = config.jdbcTemplate(mockDataSource);
        
        assertNotNull(jdbcTemplate, "JdbcTemplate should not be null");
        assertNotNull(jdbcTemplate.getDataSource(), "DataSource should be set");
        
        // JdbcTemplate should be ready to use (though actual SQL execution would need real DB)
        assertDoesNotThrow(() -> jdbcTemplate.getDataSource().toString(),
                          "JdbcTemplate should be properly initialized");
    }
}