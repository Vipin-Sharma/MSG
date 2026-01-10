package com.jfeatures.msg.codegen.audit;

import com.jfeatures.msg.codegen.*;
import com.jfeatures.msg.codegen.dbmetadata.*;
import com.squareup.javapoet.JavaFile;
import org.junit.jupiter.api.Test;

import java.sql.Types;
import java.util.*;

/**
 * Demonstration test that prints actual generated code with audit fields.
 * Run this test to see exactly what code would be generated when database has audit columns.
 */
class AuditFieldCodeGenerationDemo {

    @Test
    void demonstrateGeneratedInsertDAOWithAuditFields() throws Exception {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("DEMONSTRATION: Generated INSERT DAO with Audit Fields");
        System.out.println("=".repeat(80) + "\n");

        // Setup metadata with audit fields (simulating what would happen if DB had audit columns)
        ColumnMetadata firstName = createColumn("first_name", "VARCHAR", Types.VARCHAR);
        ColumnMetadata lastName = createColumn("last_name", "VARCHAR", Types.VARCHAR);
        ColumnMetadata email = createColumn("email", "VARCHAR", Types.VARCHAR);
        ColumnMetadata addressId = createColumn("address_id", "INT", Types.INTEGER);
        ColumnMetadata active = createColumn("active", "CHAR", Types.CHAR);
        ColumnMetadata createdAt = createColumn("created_at", "DATETIME", Types.TIMESTAMP);
        ColumnMetadata createdBy = createColumn("created_by", "VARCHAR", Types.VARCHAR);

        Map<ColumnMetadata, AuditFieldType> auditFields = new HashMap<>();
        auditFields.put(createdAt, AuditFieldType.CREATED_AT);
        auditFields.put(createdBy, AuditFieldType.CREATED_BY);

        InsertMetadata metadata = new InsertMetadata(
            "customer",
            Arrays.asList(firstName, lastName, email, addressId, active, createdAt, createdBy),
            "INSERT INTO customer (first_name, last_name, email, address_id, active, created_at, created_by) VALUES (?, ?, ?, ?, ?, ?, ?)",
            auditFields
        );

        // Generate DAO
        JavaFile daoFile = GenerateInsertDAO.createInsertDAO("Customer", metadata);
        System.out.println("--- GENERATED DAO CODE ---\n");
        System.out.println(daoFile.toString());

        // Generate DTO
        JavaFile dtoFile = GenerateInsertDTO.createInsertDTO("Customer", metadata);
        System.out.println("\n\n--- GENERATED DTO CODE ---\n");
        System.out.println(dtoFile.toString());

        // Generate Controller
        JavaFile controllerFile = GenerateInsertController.createInsertController("Customer", metadata);
        System.out.println("\n\n--- GENERATED CONTROLLER CODE ---\n");
        System.out.println(controllerFile.toString());

        // Generate AuthenticationUtil
        JavaFile authUtilFile = GenerateAuthenticationUtil.createAuthenticationUtil("Customer");
        System.out.println("\n\n--- GENERATED AUTHENTICATION UTIL CODE ---\n");
        System.out.println(authUtilFile.toString());

        System.out.println("\n" + "=".repeat(80));
        System.out.println("END OF DEMONSTRATION");
        System.out.println("=".repeat(80) + "\n");
    }

    @Test
    void demonstrateGeneratedUpdateDAOWithAuditFields() throws Exception {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("DEMONSTRATION: Generated UPDATE DAO with Audit Fields");
        System.out.println("=".repeat(80) + "\n");

        // Setup metadata with audit fields
        ColumnMetadata firstName = createColumn("first_name", "VARCHAR", Types.VARCHAR);
        ColumnMetadata email = createColumn("email", "VARCHAR", Types.VARCHAR);
        ColumnMetadata updatedAt = createColumn("updated_at", "DATETIME", Types.TIMESTAMP);
        ColumnMetadata updatedBy = createColumn("updated_by", "VARCHAR", Types.VARCHAR);
        ColumnMetadata customerId = createColumn("customer_id", "INT", Types.INTEGER);

        Map<ColumnMetadata, AuditFieldType> auditFields = new HashMap<>();
        auditFields.put(updatedAt, AuditFieldType.UPDATED_AT);
        auditFields.put(updatedBy, AuditFieldType.UPDATED_BY);

        UpdateMetadata metadata = new UpdateMetadata(
            "customer",
            Arrays.asList(firstName, email, updatedAt, updatedBy),
            Collections.singletonList(customerId),
            "UPDATE customer SET first_name = ?, email = ?, updated_at = ?, updated_by = ? WHERE customer_id = ?",
            auditFields
        );

        // Generate DAO
        JavaFile daoFile = GenerateUpdateDAO.createUpdateDAO("Customer", metadata);
        System.out.println("--- GENERATED UPDATE DAO CODE ---\n");
        System.out.println(daoFile.toString());

        // Generate DTO
        JavaFile dtoFile = GenerateUpdateDTO.createUpdateDTO("Customer", metadata);
        System.out.println("\n\n--- GENERATED UPDATE DTO CODE ---\n");
        System.out.println(dtoFile.toString());

        // Generate Controller
        JavaFile controllerFile = GenerateUpdateController.createUpdateController("Customer", metadata);
        System.out.println("\n\n--- GENERATED UPDATE CONTROLLER CODE ---\n");
        System.out.println(controllerFile.toString());

        System.out.println("\n" + "=".repeat(80));
        System.out.println("END OF DEMONSTRATION");
        System.out.println("=".repeat(80) + "\n");
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
