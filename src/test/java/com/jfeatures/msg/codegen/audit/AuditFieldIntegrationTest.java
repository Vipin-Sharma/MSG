package com.jfeatures.msg.codegen.audit;

import com.jfeatures.msg.codegen.*;
import com.jfeatures.msg.codegen.dbmetadata.*;
import com.squareup.javapoet.JavaFile;
import org.junit.jupiter.api.Test;

import java.sql.Types;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for audit field functionality across generators.
 * Tests the end-to-end audit field handling from metadata to generated code.
 */
class AuditFieldIntegrationTest {

    @Test
    void testInsertDAO_WithAuditFields_GeneratesCorrectCode() throws Exception {
        // Setup metadata with audit fields
        ColumnMetadata firstName = createColumn("first_name", "VARCHAR", Types.VARCHAR);
        ColumnMetadata createdAt = createColumn("created_at", "DATETIME", Types.TIMESTAMP);
        ColumnMetadata createdBy = createColumn("created_by", "VARCHAR", Types.VARCHAR);

        Map<ColumnMetadata, AuditFieldType> auditFields = new HashMap<>();
        auditFields.put(createdAt, AuditFieldType.CREATED_AT);
        auditFields.put(createdBy, AuditFieldType.CREATED_BY);

        InsertMetadata metadata = new InsertMetadata(
            "customer",
            Arrays.asList(firstName, createdAt, createdBy),
            "INSERT INTO customer (first_name, created_at, created_by) VALUES (?, ?, ?)",
            auditFields
        );

        // Generate DAO
        JavaFile javaFile = GenerateInsertDAO.createInsertDAO("Customer", metadata);
        String code = javaFile.toString();

        // Assertions - verify audit field auto-population
        assertTrue(code.contains("String userId"),
            "DAO should have userId String parameter for user ID audit fields");
        assertTrue(code.contains("LocalDateTime.now()"),
            "DAO should auto-populate timestamp audit fields with LocalDateTime.now()");
        assertTrue(code.contains("sqlParamMap.put(\"createdBy\", userId)"),
            "DAO should auto-populate user ID audit fields from userId parameter");
        assertTrue(code.contains("// Auto-populate audit fields"),
            "DAO should have comment explaining audit field auto-population");

        // Verify audit fields are NOT mapped from DTO
        assertFalse(code.contains("insertRequest.getCreatedAt()"),
            "createdAt should NOT come from DTO");
        assertFalse(code.contains("insertRequest.getCreatedBy()"),
            "createdBy should NOT come from DTO");

        // Verify business fields ARE mapped from DTO
        assertTrue(code.contains("insertRequest.getFirstName()"),
            "Business fields should come from DTO");
    }

    @Test
    void testInsertDTO_WithAuditFields_ExcludesAuditFieldsFromDTO() throws Exception {
        // Setup
        ColumnMetadata firstName = createColumn("first_name", "VARCHAR", Types.VARCHAR);
        ColumnMetadata lastName = createColumn("last_name", "VARCHAR", Types.VARCHAR);
        ColumnMetadata createdAt = createColumn("created_at", "DATETIME", Types.TIMESTAMP);
        ColumnMetadata createdBy = createColumn("created_by", "VARCHAR", Types.VARCHAR);

        Map<ColumnMetadata, AuditFieldType> auditFields = new HashMap<>();
        auditFields.put(createdAt, AuditFieldType.CREATED_AT);
        auditFields.put(createdBy, AuditFieldType.CREATED_BY);

        InsertMetadata metadata = new InsertMetadata(
            "customer",
            Arrays.asList(firstName, lastName, createdAt, createdBy),
            "INSERT INTO customer (first_name, last_name, created_at, created_by) VALUES (?, ?, ?, ?)",
            auditFields
        );

        // Generate DTO
        JavaFile javaFile = GenerateInsertDTO.createInsertDTO("Customer", metadata);
        String code = javaFile.toString();

        // Assertions - verify DTO only has business fields
        assertTrue(code.contains("private String firstName"),
            "DTO should include business fields");
        assertTrue(code.contains("private String lastName"),
            "DTO should include all business fields");
        assertFalse(code.contains("private LocalDateTime createdAt") || code.contains("private Instant createdAt"),
            "DTO should NOT have createdAt field");
        assertFalse(code.contains("private String createdBy") && code.contains("getCreatedBy"),
            "DTO should NOT have createdBy field with getter");
        assertTrue(code.contains("auto-populated"),
            "DTO Javadoc should mention audit fields are auto-populated");
    }

    @Test
    void testInsertController_WithAuditFields_PassesHttpServletRequest() throws Exception {
        // Setup metadata with user ID audit field
        ColumnMetadata firstName = createColumn("first_name", "VARCHAR", Types.VARCHAR);
        ColumnMetadata createdBy = createColumn("created_by", "VARCHAR", Types.VARCHAR);

        Map<ColumnMetadata, AuditFieldType> auditFields = new HashMap<>();
        auditFields.put(createdBy, AuditFieldType.CREATED_BY);

        InsertMetadata metadata = new InsertMetadata(
            "customer",
            Arrays.asList(firstName, createdBy),
            "INSERT INTO customer (first_name, created_by) VALUES (?, ?)",
            auditFields
        );

        // Generate Controller
        JavaFile javaFile = GenerateInsertController.createInsertController("Customer", metadata);
        String code = javaFile.toString();

        // Assertions - verify userId is extracted and passed
        assertTrue(code.contains("HttpServletRequest request"),
            "Controller should have HttpServletRequest parameter to extract userId");
        assertTrue(code.contains("String userId = CustomerAuthenticationUtil.getUserId(request)"),
            "Controller should extract userId from request");
        assertTrue(code.contains("insertCustomer(insertRequest, userId)"),
            "Controller should pass userId to DAO");
    }

    @Test
    void testInsertDAO_WithoutAuditFields_DoesNotRequireHttpServletRequest() throws Exception {
        // Setup metadata WITHOUT audit fields
        ColumnMetadata firstName = createColumn("first_name", "VARCHAR", Types.VARCHAR);
        ColumnMetadata lastName = createColumn("last_name", "VARCHAR", Types.VARCHAR);

        InsertMetadata metadata = new InsertMetadata(
            "customer",
            Arrays.asList(firstName, lastName),
            "INSERT INTO customer (first_name, last_name) VALUES (?, ?)",
            Collections.emptyMap()  // No audit fields
        );

        // Generate DAO
        JavaFile javaFile = GenerateInsertDAO.createInsertDAO("Customer", metadata);
        String code = javaFile.toString();

        // Assertions - verify NO HttpServletRequest when no audit fields
        assertFalse(code.contains("HttpServletRequest request"),
            "DAO should NOT have HttpServletRequest parameter when no user ID audit fields");
        assertFalse(code.contains("LocalDateTime.now()"),
            "DAO should NOT auto-populate timestamps when no audit fields");
        assertFalse(code.contains("AuthenticationUtil"),
            "DAO should NOT reference AuthenticationUtil when no audit fields");
    }

    @Test
    void testInsertDAO_WithOnlyTimestampAudit_DoesNotRequireHttpServletRequest() throws Exception {
        // Setup metadata with ONLY timestamp audit field (no user ID)
        ColumnMetadata firstName = createColumn("first_name", "VARCHAR", Types.VARCHAR);
        ColumnMetadata createdAt = createColumn("created_at", "DATETIME", Types.TIMESTAMP);

        Map<ColumnMetadata, AuditFieldType> auditFields = new HashMap<>();
        auditFields.put(createdAt, AuditFieldType.CREATED_AT);

        InsertMetadata metadata = new InsertMetadata(
            "customer",
            Arrays.asList(firstName, createdAt),
            "INSERT INTO customer (first_name, created_at) VALUES (?, ?)",
            auditFields
        );

        // Generate DAO
        JavaFile javaFile = GenerateInsertDAO.createInsertDAO("Customer", metadata);
        String code = javaFile.toString();

        // Assertions
        assertTrue(code.contains("String userId"),
            "DAO should have userId String parameter even with only timestamp audit fields (controller extracts and passes it)");
        assertTrue(code.contains("LocalDateTime.now()"),
            "DAO should auto-populate timestamp audit fields");
        assertFalse(code.contains("AuthenticationUtil"),
            "DAO should NOT reference AuthenticationUtil (userId extraction happens in controller)");
    }

    @Test
    void testUpdateDAO_WithAuditFields_GeneratesCorrectCode() throws Exception {
        // Setup UPDATE metadata with audit fields
        ColumnMetadata firstName = createColumn("first_name", "VARCHAR", Types.VARCHAR);
        ColumnMetadata updatedAt = createColumn("updated_at", "DATETIME", Types.TIMESTAMP);
        ColumnMetadata updatedBy = createColumn("updated_by", "VARCHAR", Types.VARCHAR);
        ColumnMetadata customerId = createColumn("customer_id", "INT", Types.INTEGER);

        Map<ColumnMetadata, AuditFieldType> auditFields = new HashMap<>();
        auditFields.put(updatedAt, AuditFieldType.UPDATED_AT);
        auditFields.put(updatedBy, AuditFieldType.UPDATED_BY);

        UpdateMetadata metadata = new UpdateMetadata(
            "customer",
            Arrays.asList(firstName, updatedAt, updatedBy),
            Collections.singletonList(customerId),
            "UPDATE customer SET first_name = ?, updated_at = ?, updated_by = ? WHERE customer_id = ?",
            auditFields
        );

        // Generate DAO
        JavaFile javaFile = GenerateUpdateDAO.createUpdateDAO("Customer", metadata);
        String code = javaFile.toString();

        // Assertions
        assertTrue(code.contains("String userId"),
            "UPDATE DAO should have userId String parameter for user ID audit fields");
        assertTrue(code.contains("LocalDateTime.now()"),
            "UPDATE DAO should auto-populate timestamp audit fields");
        assertTrue(code.contains("paramMap.put(\"updatedBy\", userId)"),
            "UPDATE DAO should auto-populate user ID audit fields from userId parameter");
    }

    @Test
    void testUpdateDTO_WithAuditFields_ExcludesAuditFieldsFromDTO() throws Exception {
        // Setup
        ColumnMetadata firstName = createColumn("first_name", "VARCHAR", Types.VARCHAR);
        ColumnMetadata updatedAt = createColumn("updated_at", "DATETIME", Types.TIMESTAMP);
        ColumnMetadata customerId = createColumn("customer_id", "INT", Types.INTEGER);

        Map<ColumnMetadata, AuditFieldType> auditFields = new HashMap<>();
        auditFields.put(updatedAt, AuditFieldType.UPDATED_AT);

        UpdateMetadata metadata = new UpdateMetadata(
            "customer",
            Arrays.asList(firstName, updatedAt),
            Collections.singletonList(customerId),
            "UPDATE customer SET first_name = ?, updated_at = ? WHERE customer_id = ?",
            auditFields
        );

        // Generate DTO
        JavaFile javaFile = GenerateUpdateDTO.createUpdateDTO("Customer", metadata);
        String code = javaFile.toString();

        // Assertions
        assertTrue(code.contains("private String firstName"),
            "UPDATE DTO should include business fields");
        assertFalse(code.contains("private LocalDateTime updatedAt") || code.contains("private Instant updatedAt"),
            "UPDATE DTO should NOT have updatedAt field");
    }

    private ColumnMetadata createColumn(String name, String typeName, int typeCode) {
        ColumnMetadata column = new ColumnMetadata();
        column.setColumnName(name);
        column.setColumnTypeName(typeName);
        column.setColumnType(typeCode);
        column.setIsNullable(1);
        return column;
    }
}
