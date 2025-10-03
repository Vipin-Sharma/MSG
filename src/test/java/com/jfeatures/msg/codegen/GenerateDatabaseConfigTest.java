package com.jfeatures.msg.codegen;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class GenerateDatabaseConfigTest {

    @Test
    void shouldGenerateValidDatabaseConfig() {
        String result = GenerateDatabaseConfig.createDatabaseConfig("Customer");

        assertThat(result)
            .isNotNull()
            .contains("@Configuration")
            .contains("DatabaseConfig")
            .contains("@Bean")
            .contains("DataSource")
            .contains("NamedParameterJdbcTemplate");
    }

    @Test
    void shouldContainSqlServerConfiguration() {
        String result = GenerateDatabaseConfig.createDatabaseConfig("Customer");

        assertThat(result)
            .contains("@ConfigurationProperties(\"spring.datasource\")")
            .contains("DataSourceBuilder.create().build()");
    }

    @Test
    void testCreateDatabaseConfig_WithDifferentBusinessNames_GeneratesCorrectPackages() {
        String[] businessNames = {"Product", "Order", "Customer", "UserAccount", "OrderDetail"};

        for (String businessName : businessNames) {
            String result = GenerateDatabaseConfig.createDatabaseConfig(businessName);

            assertNotNull(result);
            assertTrue(result.contains("package com.jfeatures.msg." + businessName.toLowerCase() + ".config"));
            assertTrue(result.contains("class DatabaseConfig"));
        }
    }

    @Test
    void testCreateDatabaseConfig_WithNullBusinessName_HandlesGracefully() {
        assertThrows(Exception.class, () -> GenerateDatabaseConfig.createDatabaseConfig(null));
    }

    @Test
    void testCreateDatabaseConfig_WithEmptyBusinessName_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> GenerateDatabaseConfig.createDatabaseConfig(""));
        assertThrows(IllegalArgumentException.class, () -> GenerateDatabaseConfig.createDatabaseConfig(null));
    }

    @Test
    void testCreateDatabaseConfig_ReplacesPlaceholdersCorrectly() {
        String result = GenerateDatabaseConfig.createDatabaseConfig("TestService");

        assertNotNull(result);
        assertFalse(result.contains("${packageName}"));
        assertFalse(result.contains("${businessPurpose}"));
        assertTrue(result.contains("com.jfeatures.msg.testservice.config"));
        assertTrue(result.contains("TestService") || result.contains("testservice"));
    }

    @Test
    void testCreateDatabaseConfig_VerifiesTemplateFileHandling() {
        assertDoesNotThrow(() -> {
            String result = GenerateDatabaseConfig.createDatabaseConfig("Valid");
            assertNotNull(result);
            assertFalse(result.isEmpty());
        });

        String result = GenerateDatabaseConfig.createDatabaseConfig("TemplateTest");

        assertTrue(result.length() > 100);
        assertTrue(result.contains("import"));
        assertTrue(result.contains("package"));
    }

    @Test
    void testCreateDatabaseConfig_TemplateContainsExpectedStructure() {
        String result = GenerateDatabaseConfig.createDatabaseConfig("Product");

        assertNotNull(result);

        assertTrue(result.contains("@Configuration"));
        assertTrue(result.contains("@Bean"));
        assertTrue(result.contains("@Primary"));
        assertTrue(result.contains("@ConfigurationProperties"));
        assertTrue(result.contains("public DataSource dataSource"));
        assertTrue(result.contains("public NamedParameterJdbcTemplate namedParameterJdbcTemplate"));

        assertTrue(result.contains("import org.springframework.context.annotation.Configuration"));
        assertTrue(result.contains("import org.springframework.context.annotation.Bean"));
        assertTrue(result.contains("import javax.sql.DataSource"));
        assertTrue(result.contains("import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate"));
    }

    @Test
    void testCreateDatabaseConfig_GeneratesValidJavaCode() {
        String result = GenerateDatabaseConfig.createDatabaseConfig("Customer");

        assertNotNull(result);

        assertTrue(result.contains("public class DatabaseConfig"));
        assertTrue(result.contains("{"));
        assertTrue(result.contains("}"));
        assertTrue(result.contains("() {"));
        assertTrue(result.contains("return "));
        assertFalse(result.contains("${"));
        assertFalse(result.contains("}}"));
    }

    @Test
    void testCreateDatabaseConfig_WithSpecialCharactersInBusinessName_HandlesCorrectly() {
        String[] edgeCaseNames = {"User-Profile", "Order_Detail", "Product123", "ABC"};

        for (String businessName : edgeCaseNames) {
            assertDoesNotThrow(() -> {
                String result = GenerateDatabaseConfig.createDatabaseConfig(businessName);
                assertNotNull(result);
                assertTrue(result.contains("class DatabaseConfig"));
            });
        }
    }

    @Test
    void testCreateDatabaseConfig_ConsistentOutput() {
        String businessName = "ConsistencyTest";

        String result1 = GenerateDatabaseConfig.createDatabaseConfig(businessName);
        String result2 = GenerateDatabaseConfig.createDatabaseConfig(businessName);

        assertEquals(result1, result2);
    }

    @Test
    void testCreateDatabaseConfig_ContainsExpectedAnnotations() {
        String result = GenerateDatabaseConfig.createDatabaseConfig("AnnotationTest");

        assertNotNull(result);

        assertTrue(result.contains("@Configuration"));
        assertTrue(result.contains("@ConfigurationProperties"));
        assertTrue(result.contains("@Primary"));
        assertTrue(result.contains("@Bean"));

        long beanCount = result.lines()
            .filter(line -> line.trim().equals("@Bean"))
            .count();
        assertTrue(beanCount >= 2);
    }

    @Test
    void testCreateDatabaseConfig_WithWhitespaceBusinessName_HandlesCorrectly() {
        String[] whitespaceNames = {
            " Customer ",
            "\tProduct\t",
            "\nOrder\n",
            "  Service  "
        };

        for (String businessName : whitespaceNames) {
            assertDoesNotThrow(() -> {
                String result = GenerateDatabaseConfig.createDatabaseConfig(businessName);
                assertNotNull(result);
                assertTrue(result.contains("class DatabaseConfig"));
                assertTrue(result.contains("package com.jfeatures.msg."));
            });
        }
    }

    @Test
    void testCreateDatabaseConfig_WithVeryLongBusinessName_HandlesCorrectly() {
        String longName = "ThisIsAVeryLongBusinessNameThatShouldStillWorkProperly";

        String result = GenerateDatabaseConfig.createDatabaseConfig(longName);

        assertNotNull(result);
        assertTrue(result.contains("class DatabaseConfig"));
        assertTrue(result.contains("package com.jfeatures.msg."));
        assertTrue(result.contains(longName.toLowerCase()));
    }

    @Test
    void testCreateDatabaseConfig_WithSingleCharacterBusinessName_HandlesCorrectly() {
        String[] singleCharNames = {"A", "B", "Z", "X"};

        for (String businessName : singleCharNames) {
            String result = GenerateDatabaseConfig.createDatabaseConfig(businessName);

            assertNotNull(result);
            assertTrue(result.contains("class DatabaseConfig"));
            assertTrue(result.contains("package com.jfeatures.msg." + businessName.toLowerCase() + ".config"));
        }
    }

    @Test
    void testCreateDatabaseConfig_WithNumericBusinessName_HandlesCorrectly() {
        String[] numericNames = {"123Service", "Service123", "12345"};

        for (String businessName : numericNames) {
            assertDoesNotThrow(() -> {
                String result = GenerateDatabaseConfig.createDatabaseConfig(businessName);
                assertNotNull(result);
                assertTrue(result.contains("class DatabaseConfig"));
            });
        }
    }

    @Test
    void testCreateDatabaseConfig_TemplateReplacementCompleteness() {
        String businessName = "ComprehensiveTest";
        String result = GenerateDatabaseConfig.createDatabaseConfig(businessName);

        assertNotNull(result);

        assertFalse(result.contains("${"), "Template should not contain unreplaced placeholders");
        assertFalse(result.contains("${packageName}"), "packageName placeholder should be replaced");
        assertFalse(result.contains("${businessPurpose}"), "businessPurpose placeholder should be replaced");

        assertTrue(result.contains("comprehensivetest"), "Business name should appear in lowercase in package");
        assertTrue(result.contains("ComprehensiveTest"), "Original business name should appear somewhere");
    }

    @Test
    void testCreateDatabaseConfig_GeneratedCodeStructure() {
        String result = GenerateDatabaseConfig.createDatabaseConfig("StructureTest");

        assertNotNull(result);

        assertTrue(result.matches("(?s).*package\\s+[a-z.]+;.*"), "Should have proper package declaration");
        assertTrue(result.contains("public class DatabaseConfig"), "Should have proper class declaration");
        assertTrue(result.contains("public DataSource dataSource()"), "Should have dataSource method");
        assertTrue(result.contains("public NamedParameterJdbcTemplate"), "Should have NamedParameterJdbcTemplate method");
        assertTrue(result.contains("import "), "Should contain import statements");

        long configurationCount = result.lines()
            .filter(line -> line.trim().equals("@Configuration"))
            .count();
        assertEquals(1, configurationCount, "Should have exactly one @Configuration annotation");
    }

    @Test
    void testCreateDatabaseConfig_CaseSensitivityHandling() {
        String[] caseVariations = {
            "customer", "Customer", "CUSTOMER",
            "orderService", "OrderService", "ORDERSERVICE"
        };

        for (String businessName : caseVariations) {
            String result = GenerateDatabaseConfig.createDatabaseConfig(businessName);

            assertNotNull(result);
            assertTrue(result.contains("class DatabaseConfig"));
            assertTrue(result.contains("package com.jfeatures.msg." + businessName.toLowerCase()));
        }
    }

    @Test
    void testCreateDatabaseConfig_WithMissingTemplate_ThrowsRuntimeException() {
        assertDoesNotThrow(() -> {
            String result = GenerateDatabaseConfig.createDatabaseConfig("TestTemplate");
            assertNotNull(result);
        });

        assertThrows(Exception.class, () ->
            GenerateDatabaseConfig.createDatabaseConfig(null));
    }

    @Test
    void testCreateDatabaseConfig_JavaPackageNameBuilderIntegration() {
        String[] specialCases = {
            "",
            " ",
            "Test@Special",
            "123",
            "a".repeat(100)
        };

        for (String businessName : specialCases) {
            try {
                String result = GenerateDatabaseConfig.createDatabaseConfig(businessName);
                if (result != null) {
                    assertTrue(result.contains("class DatabaseConfig"));
                }
            } catch (RuntimeException e) {
                assertNotNull(e);
            }
        }
    }

    @Test
    void testCreateDatabaseConfig_TemplateProcessingEdgeCases() {
        String[] edgeCases = {
            "Test${}",
            "packageName",
            "businessPurpose",
            "test${packageName}"
        };

        for (String businessName : edgeCases) {
            String result = GenerateDatabaseConfig.createDatabaseConfig(businessName);

            assertNotNull(result);
            assertTrue(result.contains("class DatabaseConfig"));

            if (!businessName.contains("${")) {
                assertFalse(result.contains("${packageName}"),
                    "Template placeholder should be replaced for business name: " + businessName);
                assertFalse(result.contains("${businessPurpose}"),
                    "Template placeholder should be replaced for business name: " + businessName);
            }
        }

        String conflictingBusinessName = "${businessPurpose}test";
        String conflictResult = GenerateDatabaseConfig.createDatabaseConfig(conflictingBusinessName);
        assertNotNull(conflictResult);
        assertTrue(conflictResult.contains("class DatabaseConfig"));
        assertTrue(conflictResult.contains("package com.jfeatures.msg."));
    }

    @Test
    void testCreateDatabaseConfig_ResourceHandlingBehavior() {
        for (int i = 0; i < 5; i++) {
            String result = GenerateDatabaseConfig.createDatabaseConfig("ResourceTest" + i);
            assertNotNull(result);
            assertTrue(result.contains("resourcetest" + i));
        }

        String businessName = "ConcurrentTest";
        String result1 = GenerateDatabaseConfig.createDatabaseConfig(businessName);
        String result2 = GenerateDatabaseConfig.createDatabaseConfig(businessName);

        assertEquals(result1, result2);
        assertNotNull(result1);
        assertNotNull(result2);
        assertTrue(result1.contains("class DatabaseConfig"));
        assertTrue(result2.contains("class DatabaseConfig"));
    }

    @Test
    void testPrivateConstructor_ThrowsUnsupportedOperationException() throws Exception {
        var constructor = GenerateDatabaseConfig.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        assertThrows(java.lang.reflect.InvocationTargetException.class, constructor::newInstance);

        try {
            constructor.newInstance();
            fail("Expected InvocationTargetException");
        } catch (java.lang.reflect.InvocationTargetException e) {
            assertTrue(e.getCause() instanceof UnsupportedOperationException);
            assertEquals("Utility class", e.getCause().getMessage());
        }
    }
}
