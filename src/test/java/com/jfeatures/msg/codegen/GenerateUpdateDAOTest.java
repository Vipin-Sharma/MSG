package com.jfeatures.msg.codegen;

import com.jfeatures.msg.codegen.dbmetadata.ColumnMetadata;
import com.jfeatures.msg.codegen.dbmetadata.UpdateMetadata;
import com.squareup.javapoet.JavaFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Types;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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
        // Test error conditions based on research observations
        NullPointerException exception = assertThrows(NullPointerException.class, () ->
                GenerateUpdateDAO.createUpdateDAO(null, validMetadata)
        );
        
        // Research showed specific error message
        assertTrue(exception.getMessage().contains("Cannot invoke \"String.toLowerCase()\"") ||
                   exception.getMessage().contains("businessPurposeOfSQL"));
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
}