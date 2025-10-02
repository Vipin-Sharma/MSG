package com.jfeatures.msg;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

class ApplicationTest {

    @Test
    void testApplicationAnnotations() {
        // Test that the class has the correct Spring Boot annotations
        assertTrue(Application.class.isAnnotationPresent(SpringBootApplication.class));
    }

    @Test
    void testMainMethod() {
        // Test that main method exists and is public static
        try {
            java.lang.reflect.Method mainMethod = Application.class.getMethod("main", String[].class);
            assertNotNull(mainMethod);
            assertTrue(java.lang.reflect.Modifier.isStatic(mainMethod.getModifiers()));
            assertTrue(java.lang.reflect.Modifier.isPublic(mainMethod.getModifiers()));
        } catch (NoSuchMethodException e) {
            fail("Main method should exist");
        }
    }

    @Test
    void testMainMethodCallsSpringApplicationRun() {
        // Mock SpringApplication.run to verify it's called with correct parameters
        try (MockedStatic<SpringApplication> mockedSpringApplication = mockStatic(SpringApplication.class)) {
            ConfigurableApplicationContext mockContext = mock(ConfigurableApplicationContext.class);
            mockedSpringApplication.when(() -> SpringApplication.run(Application.class, new String[]{"test"}))
                    .thenReturn(mockContext);

            // Call the main method
            Application.main(new String[]{"test"});

            // Verify SpringApplication.run was called with correct parameters
            mockedSpringApplication.verify(() -> SpringApplication.run(Application.class, new String[]{"test"}));
        }
    }

    @Test
    void testMainMethodWithNullArgs() {
        // Test main method with null arguments
        try (MockedStatic<SpringApplication> mockedSpringApplication = mockStatic(SpringApplication.class)) {
            ConfigurableApplicationContext mockContext = mock(ConfigurableApplicationContext.class);
            mockedSpringApplication.when(() -> SpringApplication.run(Application.class, (String[]) null))
                    .thenReturn(mockContext);

            // Call the main method with null
            Application.main(null);

            // Verify SpringApplication.run was called with null arguments
            mockedSpringApplication.verify(() -> SpringApplication.run(Application.class, (String[]) null));
        }
    }

    @Test
    void testMainMethodWithEmptyArgs() {
        // Test main method with empty arguments array
        try (MockedStatic<SpringApplication> mockedSpringApplication = mockStatic(SpringApplication.class)) {
            ConfigurableApplicationContext mockContext = mock(ConfigurableApplicationContext.class);
            String[] emptyArgs = {};
            mockedSpringApplication.when(() -> SpringApplication.run(Application.class, emptyArgs))
                    .thenReturn(mockContext);

            // Call the main method with empty args
            Application.main(emptyArgs);

            // Verify SpringApplication.run was called with empty arguments
            mockedSpringApplication.verify(() -> SpringApplication.run(Application.class, emptyArgs));
        }
    }

    @Test
    void testMainMethodWithMultipleArgs() {
        // Test main method with multiple arguments
        try (MockedStatic<SpringApplication> mockedSpringApplication = mockStatic(SpringApplication.class)) {
            ConfigurableApplicationContext mockContext = mock(ConfigurableApplicationContext.class);
            String[] multipleArgs = {"--server.port=8080", "--debug", "--spring.profiles.active=test"};
            mockedSpringApplication.when(() -> SpringApplication.run(Application.class, multipleArgs))
                    .thenReturn(mockContext);

            // Call the main method with multiple args
            Application.main(multipleArgs);

            // Verify SpringApplication.run was called with multiple arguments
            mockedSpringApplication.verify(() -> SpringApplication.run(Application.class, multipleArgs));
        }
    }

    @Test
    void testApplicationCanBeInstantiated() {
        // Test that the Application class can be instantiated (even though it's typically not needed)
        assertDoesNotThrow(Application::new);
    }

    @Test
    void testSpringBootApplicationAnnotationConfiguration() {
        // Test SpringBootApplication annotation configuration
        SpringBootApplication annotation = Application.class.getAnnotation(SpringBootApplication.class);
        assertNotNull(annotation);
        
        // Test default values are used (empty arrays/strings)
        assertEquals(0, annotation.exclude().length);
        assertEquals(0, annotation.excludeName().length);
        assertEquals(0, annotation.scanBasePackages().length);
        assertEquals(0, annotation.scanBasePackageClasses().length);
    }

    @Test
    void testClassStructure() {
        // Test basic class structure
        assertFalse(Application.class.isInterface());
        assertFalse(Application.class.isEnum());
        assertFalse(Application.class.isAnnotation());
        assertTrue(java.lang.reflect.Modifier.isPublic(Application.class.getModifiers()));
        
        // Test class is in correct package
        assertEquals("com.jfeatures.msg", Application.class.getPackageName());
    }

    @Test
    void testMainMethodSignature() {
        // Verify main method has exact signature expected by JVM
        try {
            java.lang.reflect.Method mainMethod = Application.class.getMethod("main", String[].class);
            assertEquals(void.class, mainMethod.getReturnType());
            assertEquals(1, mainMethod.getParameterCount());
            assertEquals(String[].class, mainMethod.getParameterTypes()[0]);
        } catch (NoSuchMethodException e) {
            fail("Main method should exist with correct signature");
        }
    }

    @Test
    void testPublicModifier() {
        // Test that the Application class is public
        assertTrue(java.lang.reflect.Modifier.isPublic(Application.class.getModifiers()));
    }
}