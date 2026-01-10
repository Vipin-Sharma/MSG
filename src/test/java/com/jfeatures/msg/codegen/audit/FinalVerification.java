package com.jfeatures.msg.codegen.audit;

import com.jfeatures.msg.codegen.*;
import com.jfeatures.msg.codegen.dbmetadata.*;
import com.squareup.javapoet.JavaFile;
import java.sql.Types;
import java.util.*;

public class FinalVerification {
    public static void main(String[] args) throws Exception {
        System.out.println("\n" + "=".repeat(100));
        System.out.println("FINAL VERIFICATION: INSERT & UPDATE with created_by/updated_by");
        System.out.println("=".repeat(100) + "\n");
        
        testInsert();
        testUpdate();
    }
    
    static void testInsert() throws Exception {
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
        
        JavaFile controllerFile = GenerateInsertController.createInsertController("Customer", metadata);
        JavaFile daoFile = GenerateInsertDAO.createInsertDAO("Customer", metadata);
        
        System.out.println("INSERT CONTROLLER METHOD:");
        System.out.println("-".repeat(100));
        String ctrl = controllerFile.toString();
        String[] lines = ctrl.split("\n");
        boolean inMethod = false;
        for (String line : lines) {
            if (line.contains("public ResponseEntity createCustomer")) inMethod = true;
            if (inMethod) {
                System.out.println(line);
                if (line.trim().equals("}") && !line.contains("if")) break;
            }
        }
        
        System.out.println("\nINSERT DAO METHOD:");
        System.out.println("-".repeat(100));
        String dao = daoFile.toString();
        lines = dao.split("\n");
        inMethod = false;
        for (String line : lines) {
            if (line.contains("public int insertCustomer")) inMethod = true;
            if (inMethod) {
                System.out.println(line);
                if (line.trim().equals("}") && !line.contains("if")) break;
            }
        }
    }
    
    static void testUpdate() throws Exception {
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
        
        JavaFile controllerFile = GenerateUpdateController.createUpdateController("Customer", metadata);
        JavaFile daoFile = GenerateUpdateDAO.createUpdateDAO("Customer", metadata);
        
        System.out.println("\n\nUPDATE CONTROLLER METHOD:");
        System.out.println("-".repeat(100));
        String ctrl = controllerFile.toString();
        String[] lines = ctrl.split("\n");
        boolean inMethod = false;
        for (String line : lines) {
            if (line.contains("public ResponseEntity<Void> updateCustomer")) inMethod = true;
            if (inMethod) {
                System.out.println(line);
                if (line.trim().equals("}") && !line.contains("if")) break;
            }
        }
        
        System.out.println("\nUPDATE DAO METHOD:");
        System.out.println("-".repeat(100));
        String dao = daoFile.toString();
        lines = dao.split("\n");
        inMethod = false;
        for (String line : lines) {
            if (line.contains("public int updateCustomer")) inMethod = true;
            if (inMethod) {
                System.out.println(line);
                if (line.trim().equals("}") && !line.contains("if")) break;
            }
        }
        
        System.out.println("\n" + "=".repeat(100));
        System.out.println("✓ VERIFICATION COMPLETE");
        System.out.println("=".repeat(100));
    }
    
    static ColumnMetadata createColumn(String name, String typeName, int typeCode) {
        ColumnMetadata column = new ColumnMetadata();
        column.setColumnName(name);
        column.setColumnTypeName(typeName);
        column.setColumnType(typeCode);
        column.setIsNullable(1);
        return column;
    }
}
