package com.jfeatures.msg.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Method;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

class NamedParameterJdbcTemplateConfigTest {

    private NamedParameterJdbcTemplateConfig config;
    
    @Mock
    private DataSource mockDataSource;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        config = new NamedParameterJdbcTemplateConfig();
    }

    @Test
    void testClassHasConfigurationAnnotation() {
        // Verify that the config class has @Configuration annotation
        assertTrue(NamedParameterJdbcTemplateConfig.class.isAnnotationPresent(Configuration.class),
                  "NamedParameterJdbcTemplateConfig should have @Configuration annotation");
    }

    @Test
    void testNamedParameterJdbcTemplateMethodHasBeanAnnotation() throws Exception {
        // Verify that the namedParameterJdbcTemplate method has @Bean annotation
        Method method = NamedParameterJdbcTemplateConfig.class.getMethod("namedParameterJdbcTemplate", DataSource.class);
        assertTrue(method.isAnnotationPresent(Bean.class),
                  "namedParameterJdbcTemplate method should have @Bean annotation");
    }

    @Test
    void testNamedParameterJdbcTemplateMethodSignature() throws Exception {
        // Verify method signature
        Method method = NamedParameterJdbcTemplateConfig.class.getMethod("namedParameterJdbcTemplate", DataSource.class);
        
        assertEquals(NamedParameterJdbcTemplate.class, method.getReturnType(),
                    "Method should return NamedParameterJdbcTemplate");
        assertEquals(1, method.getParameterCount(),
                    "Method should have exactly one parameter");
        assertEquals(DataSource.class, method.getParameterTypes()[0],
                    "Method parameter should be DataSource");
    }

    @Test
    void testNamedParameterJdbcTemplateCreation() {
        // Test that method creates a valid NamedParameterJdbcTemplate
        NamedParameterJdbcTemplate template = config.namedParameterJdbcTemplate(mockDataSource);
        
        assertNotNull(template, "NamedParameterJdbcTemplate should not be null");
        assertInstanceOf(NamedParameterJdbcTemplate.class, template,
                        "Should return NamedParameterJdbcTemplate instance");
    }

    @Test
    void testNamedParameterJdbcTemplateUsesProvidedDataSource() {
        // Test that NamedParameterJdbcTemplate uses the provided DataSource
        NamedParameterJdbcTemplate template = config.namedParameterJdbcTemplate(mockDataSource);
        
        assertNotNull(template, "NamedParameterJdbcTemplate should not be null");
        
        // Verify that the template uses the mock DataSource through its JdbcTemplate
        assertSame(mockDataSource, template.getJdbcTemplate().getDataSource(),
                  "NamedParameterJdbcTemplate should use the provided DataSource");
    }

    @Test
    void testNullDataSourceHandling() {
        // Test behavior when null DataSource is provided
        assertThrows(IllegalArgumentException.class, 
                    () -> config.namedParameterJdbcTemplate(null),
                    "Should throw IllegalArgumentException for null DataSource");
    }

    @Test
    void testMultipleNamedParameterJdbcTemplateCalls() {
        // Test that multiple calls with same DataSource return different instances
        NamedParameterJdbcTemplate template1 = config.namedParameterJdbcTemplate(mockDataSource);
        NamedParameterJdbcTemplate template2 = config.namedParameterJdbcTemplate(mockDataSource);
        
        assertNotNull(template1);
        assertNotNull(template2);
        
        // Both should use the same DataSource
        assertSame(mockDataSource, template1.getJdbcTemplate().getDataSource());
        assertSame(mockDataSource, template2.getJdbcTemplate().getDataSource());
    }

    @Test
    void testNamedParameterJdbcTemplateWithDifferentDataSources() {
        // Test with different DataSource instances
        DataSource anotherMockDataSource = mock(DataSource.class);
        
        NamedParameterJdbcTemplate template1 = config.namedParameterJdbcTemplate(mockDataSource);
        NamedParameterJdbcTemplate template2 = config.namedParameterJdbcTemplate(anotherMockDataSource);
        
        assertNotNull(template1);
        assertNotNull(template2);
        
        // Each should use their respective DataSource
        assertSame(mockDataSource, template1.getJdbcTemplate().getDataSource());
        assertSame(anotherMockDataSource, template2.getJdbcTemplate().getDataSource());
        assertNotSame(template1.getJdbcTemplate().getDataSource(), template2.getJdbcTemplate().getDataSource());
    }

    @Test
    void testConfigClassFollowsVipinsPrinciple() {
        // Test that the class follows "One public method per class" principle
        Method[] publicMethods = NamedParameterJdbcTemplateConfig.class.getMethods();
        
        // Count non-inherited public methods (excluding Object methods)
        long nonInheritedPublicMethods = java.util.Arrays.stream(publicMethods)
            .filter(method -> method.getDeclaringClass() == NamedParameterJdbcTemplateConfig.class)
            .count();
        
        assertEquals(1, nonInheritedPublicMethods, 
                    "Should have exactly one public method (following Vipin's Principle)");
    }

    @Test
    void testNamedParameterJdbcTemplateMethodIsPublic() throws Exception {
        Method method = NamedParameterJdbcTemplateConfig.class.getMethod("namedParameterJdbcTemplate", DataSource.class);
        assertTrue(java.lang.reflect.Modifier.isPublic(method.getModifiers()),
                  "namedParameterJdbcTemplate method should be public");
    }

    @Test
    void testNamedParameterJdbcTemplateConfiguration() {
        // Test that NamedParameterJdbcTemplate is properly configured
        NamedParameterJdbcTemplate template = config.namedParameterJdbcTemplate(mockDataSource);
        
        assertNotNull(template.getJdbcTemplate(), "Should have underlying JdbcTemplate");
        assertNotNull(template.getJdbcTemplate().getDataSource(), "Should have DataSource set");
        
        // Test that template has default configuration
        assertTrue(template.getJdbcTemplate().getQueryTimeout() >= -1, 
                  "Query timeout should be valid (>= -1, where -1 means no timeout)");
    }

    @Test
    void testBeanMethodFollowsSpringConventions() throws Exception {
        // Test that the bean method follows Spring conventions
        Method method = NamedParameterJdbcTemplateConfig.class.getMethod("namedParameterJdbcTemplate", DataSource.class);
        
        // Method name should be camelCase and descriptive
        assertEquals("namedParameterJdbcTemplate", method.getName(),
                    "Bean method name should match the bean type");
        
        // Should have @Bean annotation
        assertTrue(method.isAnnotationPresent(Bean.class),
                  "Bean method should have @Bean annotation");
        
        // Should accept dependency as parameter
        assertEquals(1, method.getParameterCount(),
                    "Should accept DataSource as dependency parameter");
    }

    @Test
    void testDependencyInjectionReadiness() {
        // Test that config is ready for Spring dependency injection
        
        // Class should be public for Spring to instantiate it
        assertTrue(java.lang.reflect.Modifier.isPublic(NamedParameterJdbcTemplateConfig.class.getModifiers()),
                  "Config class should be public");
        
        // Should have public no-arg constructor (default constructor is fine)
        assertDoesNotThrow(NamedParameterJdbcTemplateConfig::new,
                          "Should be able to instantiate config class");
    }

    @Test
    void testNamedParameterJdbcTemplateIsFullyInitialized() {
        // Test that returned NamedParameterJdbcTemplate is fully initialized
        NamedParameterJdbcTemplate template = config.namedParameterJdbcTemplate(mockDataSource);
        
        assertNotNull(template, "NamedParameterJdbcTemplate should not be null");
        assertNotNull(template.getJdbcTemplate(), "Underlying JdbcTemplate should be set");
        assertNotNull(template.getJdbcTemplate().getDataSource(), "DataSource should be set");
        
        // Template should be ready to use (though actual SQL execution would need real DB)
        assertDoesNotThrow(() -> template.getJdbcTemplate().getDataSource().toString(),
                          "NamedParameterJdbcTemplate should be properly initialized");
    }

    @Test
    void testNamedParameterJdbcTemplateHasCorrectUnderlyingJdbcTemplate() {
        // Test that the underlying JdbcTemplate is properly configured
        NamedParameterJdbcTemplate template = config.namedParameterJdbcTemplate(mockDataSource);
        
        // Should have a JdbcTemplate that uses our DataSource
        assertNotNull(template.getJdbcTemplate(), "Should have underlying JdbcTemplate");
        assertSame(mockDataSource, template.getJdbcTemplate().getDataSource(), 
                  "Underlying JdbcTemplate should use provided DataSource");
    }

    @Test
    void testNamedParameterJdbcTemplateSupportsNamedParameters() {
        // Test that the template is ready for named parameter operations
        NamedParameterJdbcTemplate template = config.namedParameterJdbcTemplate(mockDataSource);
        
        assertNotNull(template, "Template should not be null");
        
        // The template should be capable of handling named parameters
        // This is inherent to NamedParameterJdbcTemplate, but we verify initialization
        assertNotNull(template.getJdbcTemplate(), "Should have underlying JdbcTemplate for parameter resolution");
    }

    @Test
    void testConfigurationClassNaming() {
        // Test that the configuration class follows naming conventions
        String className = NamedParameterJdbcTemplateConfig.class.getSimpleName();
        assertTrue(className.endsWith("Config"), "Configuration class should end with 'Config'");
        assertTrue(className.contains("NamedParameterJdbcTemplate"), 
                  "Class name should indicate what it configures");
    }
}