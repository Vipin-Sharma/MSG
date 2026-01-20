package com.jfeatures.msg.codegen;

import static org.junit.jupiter.api.Assertions.*;

import com.jfeatures.msg.codegen.dbmetadata.ColumnMetadata;
import com.jfeatures.msg.codegen.dbmetadata.UpdateMetadata;
import com.jfeatures.msg.codegen.domain.AuditConfiguration;
import com.squareup.javapoet.JavaFile;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Reality-based tests for GenerateUpdateDAO based on research findings.
 * Tests actual behavior observed via research program, not assumptions.
 */
class GenerateUpdateDAOTest {

    private ColumnMetadata nameColumn;
    private ColumnMetadata statusColumn;
    private ColumnMetadata idColumn;
    private UpdateMetadata validMetadata;

    @BeforeEach
    void setUp() {
        // Set up test data based on research
        nameColumn = new ColumnMetadata();
        nameColumn.setColumnName("customer_name");
        nameColumn.setColumnTypeName("VARCHAR");
        nameColumn.setColumnType(Types.VARCHAR);
        nameColumn.setIsNullable(0);
        
        statusColumn = new ColumnMetadata();
        statusColumn.setColumnName("status");
        statusColumn.setColumnTypeName("VARCHAR");
        statusColumn.setColumnType(Types.VARCHAR);
        statusColumn.setIsNullable(0);
        
        idColumn = new ColumnMetadata();
        idColumn.setColumnName("customer_id");
        idColumn.setColumnTypeName("INTEGER");
        idColumn.setColumnType(Types.INTEGER);
        idColumn.setIsNullable(0);

        validMetadata = new UpdateMetadata(
                "customers",
                Arrays.asList(nameColumn, statusColumn),
                Collections.singletonList(idColumn),
                "UPDATE customers SET customer_name = ?, status = ? WHERE customer_id = ?"
        );
    }

    @Test
    void testCreateUpdateDAO_WithValidMetadata_GeneratesCorrectClass() throws Exception {
        // Based on research findings only
        JavaFile javaFile = GenerateUpdateDAO.createUpdateDAO("Customer", validMetadata);

        assertNotNull(javaFile);
        // Assert ONLY what was observed in research
        assertEquals("com.jfeatures.msg.customer.dao", javaFile.packageName);
        assertEquals("CustomerUpdateDAO", javaFile.typeSpec.name);

        String code = javaFile.toString();
        // Test actual observed behavior, not assumptions
        assertTrue(code.contains("@Component"));  // Research showed @Component
        assertTrue(code.contains("@Slf4j"));      // Research showed @Slf4j
        assertTrue(code.contains("NamedParameterJdbcTemplate"));  // Research showed NamedParameterJdbcTemplate
        assertTrue(code.contains("public int updateCustomer"));  // Research showed this method signature
        assertTrue(code.contains("@Valid CustomerUpdateDTO updateDto, String customerId"));  // Research showed this parameter
        assertTrue(code.contains("private static final String SQL"));  // Research showed this field
        assertTrue(code.contains("UPDATE"));  // Research showed SQL generation
        assertTrue(code.contains("Map<String, Object> paramMap"));  // Research showed this mapping approach
        assertTrue(code.contains("log.info"));  // Research showed logging
    }

    @Test
    void testCreateUpdateDAO_WithSingleSetColumn_GeneratesCorrectClass() throws Exception {
        ColumnMetadata singleStatusColumn = new ColumnMetadata();
        singleStatusColumn.setColumnName("status");
        singleStatusColumn.setColumnTypeName("VARCHAR");
        singleStatusColumn.setColumnType(Types.VARCHAR);
        singleStatusColumn.setIsNullable(0);
        
        ColumnMetadata singleIdColumn = new ColumnMetadata();
        singleIdColumn.setColumnName("id");
        singleIdColumn.setColumnTypeName("INTEGER");
        singleIdColumn.setColumnType(Types.INTEGER);
        singleIdColumn.setIsNullable(0);

        UpdateMetadata singleColumnMetadata = new UpdateMetadata(
                "simple_table",
                Collections.singletonList(singleStatusColumn),
                Collections.singletonList(singleIdColumn),
                "UPDATE simple_table SET status = ? WHERE id = ?"
        );

        JavaFile javaFile = GenerateUpdateDAO.createUpdateDAO("Simple", singleColumnMetadata);

        assertNotNull(javaFile);
        assertEquals("com.jfeatures.msg.simple.dao", javaFile.packageName);
        assertEquals("SimpleUpdateDAO", javaFile.typeSpec.name);

        String code = javaFile.toString();
        assertTrue(code.contains("public int updateSimple"));  // Method name based on business name
        assertTrue(code.contains("@Valid SimpleUpdateDTO updateDto, String id"));
        assertTrue(code.contains("paramMap.put(\"status\""));  // Single parameter mapping
    }

    @Test
    void testCreateUpdateDAO_WithNullBusinessName_ThrowsException() {
        // Test error conditions - now throws IllegalArgumentException due to validation in JavaPoetTypeNameBuilder
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                GenerateUpdateDAO.createUpdateDAO(null, validMetadata)
        );
        
        // Verify proper error message
        assertTrue(exception.getMessage().contains("SQL business domain name cannot be null or empty"));
    }

    @Test
    void testCreateUpdateDAO_WithEmptySetColumns_GeneratesInvalidSQL() throws Exception {
        // Research showed this doesn't throw an exception but generates invalid SQL
        ColumnMetadata singleIdColumn = new ColumnMetadata();
        singleIdColumn.setColumnName("id");
        singleIdColumn.setColumnTypeName("INTEGER");
        singleIdColumn.setColumnType(Types.INTEGER);
        singleIdColumn.setIsNullable(0);
        
        UpdateMetadata emptySetMetadata = new UpdateMetadata(
                "test_table",
                Collections.emptyList(),  // Empty SET columns
                Collections.singletonList(singleIdColumn),
                "UPDATE test_table WHERE id = ?"
        );
        
        JavaFile javaFile = GenerateUpdateDAO.createUpdateDAO("Test", emptySetMetadata);
        
        assertNotNull(javaFile);  // Research showed it doesn't throw exception
        String code = javaFile.toString();
        assertTrue(code.contains("SET\n  WHERE"));  // Invalid SQL pattern observed in research
    }

    @Test
    void testCreateUpdateDAO_WithMultipleColumns_GeneratesCamelCaseParameterMapping() throws Exception {
        // Test that parameter mapping uses camelCase (different from INSERT!)
        JavaFile javaFile = GenerateUpdateDAO.createUpdateDAO("Customer", validMetadata);
        
        String code = javaFile.toString();
        // Based on research: parameters are mapped in camelCase (different from INSERT)
        assertTrue(code.contains("paramMap.put(\"customerName\""));  // customer_name -> customerName
        assertTrue(code.contains("paramMap.put(\"status\""));  
        assertTrue(code.contains("paramMap.put(\"customerId\""));  // WHERE parameter
        assertTrue(code.contains("updateDto.getCustomerName()"));  // Getter method names
        assertTrue(code.contains("updateDto.getStatus()"));
    }

    @Test
    void testCreateUpdateDAO_GeneratesCorrectSQLFormat() throws Exception {
        JavaFile javaFile = GenerateUpdateDAO.createUpdateDAO("Customer", validMetadata);
        
        String code = javaFile.toString();
        // Based on research: SQL uses named parameters with =: syntax (no spaces)
        assertTrue(code.contains("UPDATE"));
        assertTrue(code.contains("customers"));
        assertTrue(code.contains("=: customerName"));  // Named parameter format
        assertTrue(code.contains("=: status"));
        assertTrue(code.contains("WHERE"));
        assertTrue(code.contains("=: customerId"));
    }

    @Test
    void testCreateUpdateDAO_GeneratesCorrectMethodSignature() throws Exception {
        JavaFile javaFile = GenerateUpdateDAO.createUpdateDAO("Customer", validMetadata);
        
        String code = javaFile.toString();
        // Based on research: method takes DTO + WHERE parameters as separate arguments
        assertTrue(code.contains("updateCustomer(@Valid CustomerUpdateDTO updateDto, String customerId)"));
        assertTrue(code.contains("public int updateCustomer"));  // Returns int
        assertTrue(code.contains("@Valid"));     // Uses validation
    }

    @Test
    void testCreateUpdateDAO_GeneratesLoggingStatements() throws Exception {
        JavaFile javaFile = GenerateUpdateDAO.createUpdateDAO("Customer", validMetadata);
        
        String code = javaFile.toString();
        // Based on research: includes logging statements
        assertTrue(code.contains("log.info(\"Executing UPDATE: {}\", SQL)"));
        assertTrue(code.contains("log.debug(\"Parameters: {}\", paramMap)"));
        assertTrue(code.contains("log.info(\"Updated {} rows for {}\", rowsUpdated, \"Customer\")"));
    }

    @Test
    void testCreateUpdateDAO_GeneratesCorrectPackageStructure() throws Exception {
        // Test various business names to verify package naming convention
        JavaFile customerFile = GenerateUpdateDAO.createUpdateDAO("Customer", validMetadata);
        assertEquals("com.jfeatures.msg.customer.dao", customerFile.packageName);
        
        JavaFile productFile = GenerateUpdateDAO.createUpdateDAO("Product", validMetadata);
        assertEquals("com.jfeatures.msg.product.dao", productFile.packageName);
        
        JavaFile orderDetailFile = GenerateUpdateDAO.createUpdateDAO("OrderDetail", validMetadata);
        assertEquals("com.jfeatures.msg.orderdetail.dao", orderDetailFile.packageName);
    }

    @Test
    void testCreateUpdateDAO_GeneratesCorrectImports() throws Exception {
        JavaFile javaFile = GenerateUpdateDAO.createUpdateDAO("Customer", validMetadata);

        String code = javaFile.toString();
        // Based on research: check for key imports
        assertTrue(code.contains("import jakarta.validation.Valid"));
        assertTrue(code.contains("import lombok.extern.slf4j.Slf4j"));
        assertTrue(code.contains("import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate"));
        assertTrue(code.contains("import org.springframework.stereotype.Component"));
    }

    // ========== AUDIT CONFIGURATION TESTS ==========

    @Test
    void testCreateUpdateDAO_WithAuditEnabled_AddsUserIdParameter() throws Exception {
        // Create metadata with audit column
        ColumnMetadata updatedByColumn = new ColumnMetadata();
        updatedByColumn.setColumnName("updated_by");
        updatedByColumn.setColumnTypeName("VARCHAR");
        updatedByColumn.setColumnType(Types.VARCHAR);
        updatedByColumn.setIsNullable(0);

        UpdateMetadata metadataWithAudit = new UpdateMetadata(
                "customers",
                Arrays.asList(nameColumn, updatedByColumn),
                Collections.singletonList(idColumn),
                "UPDATE customers SET customer_name = ?, updated_by = ? WHERE customer_id = ?"
        );

        AuditConfiguration auditConfig = new AuditConfiguration(
                true, "X-User-ID", Set.of("updated_by"));

        JavaFile javaFile = GenerateUpdateDAO.createUpdateDAO("Customer", metadataWithAudit, auditConfig);

        String code = javaFile.toString();

        // Verify userId parameter is present in method signature
        assertTrue(code.contains("String userId"),
                "Method should have userId parameter");

        // Verify audit column is mapped from userId, not from DTO
        assertTrue(code.contains("paramMap.put(\"updatedBy\", userId)"),
                "Audit column should be mapped from userId");

        // Verify regular columns still come from DTO
        assertTrue(code.contains("updateDto.getCustomerName()"),
                "Regular columns should still come from DTO");
    }

    @Test
    void testCreateUpdateDAO_WithAuditDisabled_NoUserIdParameter() throws Exception {
        AuditConfiguration auditConfig = AuditConfiguration.disabled();

        JavaFile javaFile = GenerateUpdateDAO.createUpdateDAO("Customer", validMetadata, auditConfig);

        String code = javaFile.toString();

        // Verify userId parameter is NOT present
        assertFalse(code.contains("String userId"),
                "Should NOT have userId parameter when audit disabled");

        // Verify all columns come from DTO
        assertTrue(code.contains("updateDto.getCustomerName()"));
        assertTrue(code.contains("updateDto.getStatus()"));
    }

    @Test
    void testCreateUpdateDAO_WithMultipleAuditColumns_MapsAllFromUserId() throws Exception {
        // Create metadata with multiple audit columns
        ColumnMetadata modifiedByColumn = new ColumnMetadata();
        modifiedByColumn.setColumnName("modified_by");
        modifiedByColumn.setColumnTypeName("VARCHAR");
        modifiedByColumn.setColumnType(Types.VARCHAR);
        modifiedByColumn.setIsNullable(0);

        ColumnMetadata updatedByColumn = new ColumnMetadata();
        updatedByColumn.setColumnName("updated_by");
        updatedByColumn.setColumnTypeName("VARCHAR");
        updatedByColumn.setColumnType(Types.VARCHAR);
        updatedByColumn.setIsNullable(0);

        UpdateMetadata metadataWithAudit = new UpdateMetadata(
                "customers",
                Arrays.asList(nameColumn, modifiedByColumn, updatedByColumn),
                Collections.singletonList(idColumn),
                "UPDATE customers SET customer_name = ?, modified_by = ?, updated_by = ? WHERE customer_id = ?"
        );

        AuditConfiguration auditConfig = new AuditConfiguration(
                true, "X-User-ID", Set.of("modified_by", "updated_by"));

        JavaFile javaFile = GenerateUpdateDAO.createUpdateDAO("Customer", metadataWithAudit, auditConfig);

        String code = javaFile.toString();

        // Verify both audit columns are mapped from userId
        assertTrue(code.contains("paramMap.put(\"modifiedBy\", userId)"),
                "modified_by should be mapped from userId");
        assertTrue(code.contains("paramMap.put(\"updatedBy\", userId)"),
                "updated_by should be mapped from userId");

        // Verify regular columns still come from DTO
        assertTrue(code.contains("updateDto.getCustomerName()"));
    }

    @Test
    void testCreateUpdateDAO_WithNullAuditConfig_DefaultsToDisabled() throws Exception {
        JavaFile javaFile = GenerateUpdateDAO.createUpdateDAO("Customer", validMetadata, null);

        String code = javaFile.toString();

        // Should behave like audit disabled
        assertFalse(code.contains("String userId"),
                "Should NOT have userId parameter when config is null");
    }

    @Test
    void testCreateUpdateDAO_BackwardCompatibility_NoAuditConfig() throws Exception {
        // Test that the original method without AuditConfiguration still works
        JavaFile javaFile = GenerateUpdateDAO.createUpdateDAO("Customer", validMetadata);

        String code = javaFile.toString();

        // Should behave like audit disabled
        assertFalse(code.contains("String userId"),
                "Default should NOT have userId parameter");

        // All columns should come from DTO
        assertTrue(code.contains("updateDto.getCustomerName()"));
        assertTrue(code.contains("updateDto.getStatus()"));
    }

    @Test
    void testCreateUpdateDAO_WithAuditEnabled_GeneratesCorrectJavaDoc() throws Exception {
        ColumnMetadata updatedByColumn = new ColumnMetadata();
        updatedByColumn.setColumnName("updated_by");
        updatedByColumn.setColumnTypeName("VARCHAR");
        updatedByColumn.setColumnType(Types.VARCHAR);
        updatedByColumn.setIsNullable(0);

        UpdateMetadata metadataWithAudit = new UpdateMetadata(
                "customers",
                Arrays.asList(nameColumn, updatedByColumn),
                Collections.singletonList(idColumn),
                "UPDATE customers SET customer_name = ?, updated_by = ? WHERE customer_id = ?"
        );

        AuditConfiguration auditConfig = new AuditConfiguration(
                true, "X-User-ID", Set.of("updated_by"));

        JavaFile javaFile = GenerateUpdateDAO.createUpdateDAO("Customer", metadataWithAudit, auditConfig);

        String code = javaFile.toString();

        // Verify JavaDoc mentions audit tracking
        assertTrue(code.contains("audit tracking") || code.contains("audit"),
                "JavaDoc should mention audit tracking when audit is enabled");
        assertTrue(code.contains("@param userId"),
                "JavaDoc should document userId parameter");
    }

    @Test
    void testCreateUpdateDAO_CaseInsensitiveAuditColumnMatching() throws Exception {
        // Create metadata with audit column in different case
        ColumnMetadata updatedByColumn = new ColumnMetadata();
        updatedByColumn.setColumnName("UPDATED_BY"); // Uppercase
        updatedByColumn.setColumnTypeName("VARCHAR");
        updatedByColumn.setColumnType(Types.VARCHAR);
        updatedByColumn.setIsNullable(0);

        UpdateMetadata metadataWithAudit = new UpdateMetadata(
                "customers",
                Arrays.asList(nameColumn, updatedByColumn),
                Collections.singletonList(idColumn),
                "UPDATE customers SET customer_name = ?, UPDATED_BY = ? WHERE customer_id = ?"
        );

        AuditConfiguration auditConfig = new AuditConfiguration(
                true, "X-User-ID", Set.of("updated_by")); // lowercase in config

        JavaFile javaFile = GenerateUpdateDAO.createUpdateDAO("Customer", metadataWithAudit, auditConfig);

        String code = javaFile.toString();

        // Verify the audit column is still mapped from userId (case-insensitive match)
        assertTrue(code.contains("paramMap.put(\"updatedBy\", userId)") ||
                   code.contains("paramMap.put(\"UPDATEDBY\", userId)"),
                "Should match audit columns case-insensitively");
    }
}