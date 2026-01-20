package com.jfeatures.msg.codegen;

import static org.junit.jupiter.api.Assertions.*;

import com.jfeatures.msg.codegen.dbmetadata.ColumnMetadata;
import com.jfeatures.msg.codegen.dbmetadata.InsertMetadata;
import com.jfeatures.msg.codegen.domain.AuditConfiguration;
import com.squareup.javapoet.JavaFile;
import java.io.IOException;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Reality-based tests for GenerateInsertDAO based on research findings.
 * Tests actual behavior observed via research program, not assumptions.
 */
class GenerateInsertDAOTest {

    private ColumnMetadata customerIdColumn;
    private ColumnMetadata customerNameColumn;
    private InsertMetadata validMetadata;

    @BeforeEach
    void setUp() {
        // Set up test data based on research
        customerIdColumn = new ColumnMetadata();
        customerIdColumn.setColumnName("customer_id");
        customerIdColumn.setColumnTypeName("INTEGER");
        customerIdColumn.setColumnType(Types.INTEGER);
        customerIdColumn.setIsNullable(0);

        customerNameColumn = new ColumnMetadata();
        customerNameColumn.setColumnName("customer_name");
        customerNameColumn.setColumnTypeName("VARCHAR");
        customerNameColumn.setColumnType(Types.VARCHAR);
        customerNameColumn.setIsNullable(0);

        validMetadata = new InsertMetadata(
                "customers",
                Arrays.asList(customerIdColumn, customerNameColumn),
                "INSERT INTO customers (customer_id, customer_name) VALUES (?, ?)"
        );
    }

    @Test
    void testCreateInsertDAO_WithValidMetadata_GeneratesCorrectClass() throws IOException {
        // Based on research findings only
        JavaFile javaFile = GenerateInsertDAO.createInsertDAO("Customer", validMetadata);

        assertNotNull(javaFile);
        // Assert ONLY what was observed in research
        assertEquals("com.jfeatures.msg.customer.dao", javaFile.packageName);
        assertEquals("CustomerInsertDAO", javaFile.typeSpec.name);

        String code = javaFile.toString();
        // Test actual observed behavior, not assumptions
        assertTrue(code.contains("@Component"));  // Research showed @Component, not @Repository
        assertTrue(code.contains("NamedParameterJdbcTemplate"));  // Research showed NamedParameterJdbcTemplate
        assertTrue(code.contains("public int insertCustomer"));  // Research showed this method signature
        assertTrue(code.contains("CustomerInsertDTO insertRequest"));  // Research showed this parameter
        assertTrue(code.contains("private static final String SQL"));  // Research showed this field
        assertTrue(code.contains("INSERT INTO"));  // Research showed SQL generation
        assertTrue(code.contains("Map<String, Object> sqlParamMap"));  // Research showed this mapping approach
    }

    @Test
    void testCreateInsertDAO_WithSingleColumn_GeneratesCorrectClass() throws IOException {
        ColumnMetadata singleColumn = new ColumnMetadata();
        singleColumn.setColumnName("id");
        singleColumn.setColumnTypeName("INTEGER");
        singleColumn.setColumnType(Types.INTEGER);
        singleColumn.setIsNullable(0);

        InsertMetadata singleColumnMetadata = new InsertMetadata(
                "simple_table",
                Collections.singletonList(singleColumn),
                "INSERT INTO simple_table (id) VALUES (?)"
        );

        JavaFile javaFile = GenerateInsertDAO.createInsertDAO("Simple", singleColumnMetadata);

        assertNotNull(javaFile);
        assertEquals("com.jfeatures.msg.simple.dao", javaFile.packageName);
        assertEquals("SimpleInsertDAO", javaFile.typeSpec.name);

        String code = javaFile.toString();
        assertTrue(code.contains("public int insertSimple"));  // Method name based on business name
        assertTrue(code.contains("SimpleInsertDTO insertRequest"));
        assertTrue(code.contains("sqlParamMap.put(\"id\""));  // Single parameter mapping
    }

    private static Stream<Arguments> invalidBusinessNameProvider() {
        return Stream.of(
            Arguments.of("null business name", null),
            Arguments.of("empty business name", ""),
            Arguments.of("whitespace business name", "   ")
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidBusinessNameProvider")
    void testCreateInsertDAO_WithInvalidBusinessName_ThrowsException(String testName, String businessName) {
        // Test error conditions based on research observations
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                GenerateInsertDAO.createInsertDAO(businessName, validMetadata)
        );

        assertEquals("Business purpose of SQL cannot be null or empty", exception.getMessage());
    }

    @Test
    void testCreateInsertDAO_WithNullMetadata_ThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                GenerateInsertDAO.createInsertDAO("Customer", null)
        );
        
        assertEquals("Insert metadata cannot be null", exception.getMessage());
    }

    @Test
    void testCreateInsertDAO_WithEmptyColumns_ThrowsException() {
        // Test edge cases based on research
        InsertMetadata emptyMetadata = new InsertMetadata(
                "test_table", 
                Collections.emptyList(), 
                "INSERT INTO test_table () VALUES ()"
        );
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                GenerateInsertDAO.createInsertDAO("Test", emptyMetadata)
        );
        
        assertEquals("Insert metadata must have at least one column", exception.getMessage());
    }

    @Test
    void testCreateInsertDAO_WithMultipleColumns_GeneratesCorrectParameterMapping() throws IOException {
        // Test that parameter mapping matches actual research observations
        JavaFile javaFile = GenerateInsertDAO.createInsertDAO("Customer", validMetadata);
        
        String code = javaFile.toString();
        // Fixed: parameters now use proper camelCase Java naming conventions
        assertTrue(code.contains("sqlParamMap.put(\"customerId\""));  // Fixed: camelCase parameter names
        assertTrue(code.contains("sqlParamMap.put(\"customerName\""));  // Fixed: camelCase parameter names  
        assertTrue(code.contains("insertRequest.getCustomerId()"));  // Fixed: proper getter method names
        assertTrue(code.contains("insertRequest.getCustomerName()"));
    }

    @Test
    void testCreateInsertDAO_GeneratesCorrectSQLFormat() throws IOException {
        JavaFile javaFile = GenerateInsertDAO.createInsertDAO("Customer", validMetadata);
        
        String code = javaFile.toString();
        // Fixed: SQL uses named parameters with proper camelCase naming
        assertTrue(code.contains("INSERT INTO"));
        assertTrue(code.contains("customers"));
        assertTrue(code.contains(": customerId"));  // Fixed: camelCase parameter names in SQL
        assertTrue(code.contains(": customerName"));  // Fixed: camelCase parameter names in SQL
        assertTrue(code.contains("VALUES"));
    }

    @Test
    void testCreateInsertDAO_GeneratesCorrectPackageStructure() throws IOException {
        // Test various business names to verify package naming convention
        JavaFile customerFile = GenerateInsertDAO.createInsertDAO("Customer", validMetadata);
        assertEquals("com.jfeatures.msg.customer.dao", customerFile.packageName);

        JavaFile productFile = GenerateInsertDAO.createInsertDAO("Product", validMetadata);
        assertEquals("com.jfeatures.msg.product.dao", productFile.packageName);

        JavaFile orderDetailFile = GenerateInsertDAO.createInsertDAO("OrderDetail", validMetadata);
        assertEquals("com.jfeatures.msg.orderdetail.dao", orderDetailFile.packageName);
    }

    // ========== AUDIT CONFIGURATION TESTS ==========

    @Test
    void testCreateInsertDAO_WithAuditEnabled_AddsUserIdParameter() throws IOException {
        // Create metadata with audit column
        ColumnMetadata createdByColumn = new ColumnMetadata();
        createdByColumn.setColumnName("created_by");
        createdByColumn.setColumnTypeName("VARCHAR");
        createdByColumn.setColumnType(Types.VARCHAR);
        createdByColumn.setIsNullable(0);

        InsertMetadata metadataWithAudit = new InsertMetadata(
                "customers",
                Arrays.asList(customerIdColumn, customerNameColumn, createdByColumn),
                "INSERT INTO customers (customer_id, customer_name, created_by) VALUES (?, ?, ?)"
        );

        AuditConfiguration auditConfig = new AuditConfiguration(
                true, "X-User-ID", Set.of("created_by"));

        JavaFile javaFile = GenerateInsertDAO.createInsertDAO("Customer", metadataWithAudit, auditConfig);

        String code = javaFile.toString();

        // Verify userId parameter is present in method signature
        assertTrue(code.contains("public int insertCustomer(CustomerInsertDTO insertRequest, String userId)"),
                "Method should have userId parameter");

        // Verify audit column is mapped from userId, not from DTO
        assertTrue(code.contains("sqlParamMap.put(\"createdBy\", userId)"),
                "Audit column should be mapped from userId");

        // Verify regular columns still come from DTO
        assertTrue(code.contains("insertRequest.getCustomerId()"),
                "Regular columns should still come from DTO");
        assertTrue(code.contains("insertRequest.getCustomerName()"),
                "Regular columns should still come from DTO");
    }

    @Test
    void testCreateInsertDAO_WithAuditDisabled_NoUserIdParameter() throws IOException {
        AuditConfiguration auditConfig = AuditConfiguration.disabled();

        JavaFile javaFile = GenerateInsertDAO.createInsertDAO("Customer", validMetadata, auditConfig);

        String code = javaFile.toString();

        // Verify userId parameter is NOT present
        assertTrue(code.contains("public int insertCustomer(CustomerInsertDTO insertRequest)"),
                "Method should NOT have userId parameter when audit disabled");
        assertFalse(code.contains("String userId"),
                "Should NOT have userId parameter when audit disabled");

        // Verify all columns come from DTO
        assertTrue(code.contains("insertRequest.getCustomerId()"));
        assertTrue(code.contains("insertRequest.getCustomerName()"));
    }

    @Test
    void testCreateInsertDAO_WithMultipleAuditColumns_MapsAllFromUserId() throws IOException {
        // Create metadata with multiple audit columns
        ColumnMetadata createdByColumn = new ColumnMetadata();
        createdByColumn.setColumnName("created_by");
        createdByColumn.setColumnTypeName("VARCHAR");
        createdByColumn.setColumnType(Types.VARCHAR);
        createdByColumn.setIsNullable(0);

        ColumnMetadata updatedByColumn = new ColumnMetadata();
        updatedByColumn.setColumnName("updated_by");
        updatedByColumn.setColumnTypeName("VARCHAR");
        updatedByColumn.setColumnType(Types.VARCHAR);
        updatedByColumn.setIsNullable(0);

        InsertMetadata metadataWithAudit = new InsertMetadata(
                "customers",
                Arrays.asList(customerIdColumn, customerNameColumn, createdByColumn, updatedByColumn),
                "INSERT INTO customers (customer_id, customer_name, created_by, updated_by) VALUES (?, ?, ?, ?)"
        );

        AuditConfiguration auditConfig = new AuditConfiguration(
                true, "X-User-ID", Set.of("created_by", "updated_by"));

        JavaFile javaFile = GenerateInsertDAO.createInsertDAO("Customer", metadataWithAudit, auditConfig);

        String code = javaFile.toString();

        // Verify both audit columns are mapped from userId
        assertTrue(code.contains("sqlParamMap.put(\"createdBy\", userId)"),
                "created_by should be mapped from userId");
        assertTrue(code.contains("sqlParamMap.put(\"updatedBy\", userId)"),
                "updated_by should be mapped from userId");

        // Verify regular columns still come from DTO
        assertTrue(code.contains("insertRequest.getCustomerId()"));
        assertTrue(code.contains("insertRequest.getCustomerName()"));
    }

    @Test
    void testCreateInsertDAO_WithNullAuditConfig_DefaultsToDisabled() throws IOException {
        JavaFile javaFile = GenerateInsertDAO.createInsertDAO("Customer", validMetadata, null);

        String code = javaFile.toString();

        // Should behave like audit disabled
        assertTrue(code.contains("public int insertCustomer(CustomerInsertDTO insertRequest)"),
                "Method should NOT have userId parameter when config is null");
        assertFalse(code.contains("String userId"),
                "Should NOT have userId parameter when config is null");
    }

    @Test
    void testCreateInsertDAO_BackwardCompatibility_NoAuditConfig() throws IOException {
        // Test that the original method without AuditConfiguration still works
        JavaFile javaFile = GenerateInsertDAO.createInsertDAO("Customer", validMetadata);

        String code = javaFile.toString();

        // Should behave like audit disabled
        assertTrue(code.contains("public int insertCustomer(CustomerInsertDTO insertRequest)"),
                "Default should NOT have userId parameter");
        assertFalse(code.contains("String userId"),
                "Default should NOT have userId parameter");

        // All columns should come from DTO
        assertTrue(code.contains("insertRequest.getCustomerId()"));
        assertTrue(code.contains("insertRequest.getCustomerName()"));
    }

    @Test
    void testCreateInsertDAO_WithAuditEnabled_GeneratesCorrectJavaDoc() throws IOException {
        ColumnMetadata createdByColumn = new ColumnMetadata();
        createdByColumn.setColumnName("created_by");
        createdByColumn.setColumnTypeName("VARCHAR");
        createdByColumn.setColumnType(Types.VARCHAR);
        createdByColumn.setIsNullable(0);

        InsertMetadata metadataWithAudit = new InsertMetadata(
                "customers",
                Arrays.asList(customerIdColumn, createdByColumn),
                "INSERT INTO customers (customer_id, created_by) VALUES (?, ?)"
        );

        AuditConfiguration auditConfig = new AuditConfiguration(
                true, "X-User-ID", Set.of("created_by"));

        JavaFile javaFile = GenerateInsertDAO.createInsertDAO("Customer", metadataWithAudit, auditConfig);

        String code = javaFile.toString();

        // Verify JavaDoc mentions audit tracking
        assertTrue(code.contains("audit tracking") || code.contains("audit"),
                "JavaDoc should mention audit tracking when audit is enabled");
        assertTrue(code.contains("@param userId"),
                "JavaDoc should document userId parameter");
    }

    @Test
    void testCreateInsertDAO_CaseInsensitiveAuditColumnMatching() throws IOException {
        // Create metadata with audit column in different case
        ColumnMetadata createdByColumn = new ColumnMetadata();
        createdByColumn.setColumnName("CREATED_BY"); // Uppercase
        createdByColumn.setColumnTypeName("VARCHAR");
        createdByColumn.setColumnType(Types.VARCHAR);
        createdByColumn.setIsNullable(0);

        InsertMetadata metadataWithAudit = new InsertMetadata(
                "customers",
                Arrays.asList(customerIdColumn, createdByColumn),
                "INSERT INTO customers (customer_id, CREATED_BY) VALUES (?, ?)"
        );

        AuditConfiguration auditConfig = new AuditConfiguration(
                true, "X-User-ID", Set.of("created_by")); // lowercase in config

        JavaFile javaFile = GenerateInsertDAO.createInsertDAO("Customer", metadataWithAudit, auditConfig);

        String code = javaFile.toString();

        // Verify the audit column is still mapped from userId (case-insensitive match)
        assertTrue(code.contains("sqlParamMap.put(\"createdBy\", userId)") ||
                   code.contains("sqlParamMap.put(\"CREATEDBY\", userId)"),
                "Should match audit columns case-insensitively");
    }
}