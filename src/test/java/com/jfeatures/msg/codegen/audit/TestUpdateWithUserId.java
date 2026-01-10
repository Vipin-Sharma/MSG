package com.jfeatures.msg.codegen.audit;

import com.jfeatures.msg.codegen.*;
import com.jfeatures.msg.codegen.dbmetadata.*;
import com.squareup.javapoet.JavaFile;
import java.sql.Types;
import java.util.*;

public class TestUpdateWithUserId {
    public static void main(String[] args) throws Exception {
        // CREATE UPDATE METADATA WITH BOTH timestamp AND userId audit fields
        ColumnMetadata firstName = new ColumnMetadata();
        firstName.setColumnName("first_name");
        firstName.setColumnTypeName("VARCHAR");
        firstName.setColumnType(Types.VARCHAR);
        firstName.setIsNullable(0);
        
        ColumnMetadata updatedAt = new ColumnMetadata();
        updatedAt.setColumnName("updated_at");
        updatedAt.setColumnTypeName("DATETIME");
        updatedAt.setColumnType(Types.TIMESTAMP);
        updatedAt.setIsNullable(1);
        
        ColumnMetadata updatedBy = new ColumnMetadata();
        updatedBy.setColumnName("updated_by");
        updatedBy.setColumnTypeName("VARCHAR");
        updatedBy.setColumnType(Types.VARCHAR);
        updatedBy.setIsNullable(1);
        
        ColumnMetadata customerId = new ColumnMetadata();
        customerId.setColumnName("customer_id");
        customerId.setColumnTypeName("INT");
        customerId.setColumnType(Types.INTEGER);
        customerId.setIsNullable(0);
        
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
        
        System.out.println("=== TESTING UPDATE WITH BOTH TIMESTAMP AND USERID ===\n");
        
        // Generate DAO
        JavaFile daoFile = GenerateUpdateDAO.createUpdateDAO("Customer", metadata);
        String daoCode = daoFile.toString();
        
        System.out.println("DAO CODE:");
        System.out.println(daoCode);
        
        // Analysis
        System.out.println("\n" + "=".repeat(80));
        System.out.println("CRITICAL ANALYSIS - CHECKING UPDATE DAO:");
        System.out.println("=".repeat(80));
        
        boolean hasRequestParam = daoCode.contains("HttpServletRequest request");
        boolean usesAuthUtil = daoCode.contains("AuthenticationUtil.getUserId(request)");
        boolean setsUpdatedBy = daoCode.contains("updatedBy");
        boolean setsUpdatedAt = daoCode.contains("updatedAt");
        
        System.out.println("1. Does DAO have HttpServletRequest parameter? " + hasRequestParam);
        System.out.println("2. Does DAO use AuthenticationUtil to get userId? " + usesAuthUtil);
        System.out.println("3. Does DAO set updated_by column? " + setsUpdatedBy);
        System.out.println("4. Does DAO set updated_at column? " + setsUpdatedAt);
        
        // Check the actual parameter map code
        if (daoCode.contains("paramMap.put(\"updatedBy\"")) {
            System.out.println("\n✓ DAO sets updatedBy in parameter map");
        } else {
            System.out.println("\n✗ DAO does NOT set updatedBy in parameter map - THIS IS THE BUG!");
        }
        
        if (daoCode.contains("paramMap.put(\"updatedAt\"")) {
            System.out.println("✓ DAO sets updatedAt in parameter map");
        } else {
            System.out.println("✗ DAO does NOT set updatedAt in parameter map - THIS IS THE BUG!");
        }
        
        if (hasRequestParam && usesAuthUtil && daoCode.contains("paramMap.put(\"updatedBy\"")) {
            System.out.println("\n✓ UPDATE DAO correctly handles audit fields!");
        } else {
            System.out.println("\n✗ UPDATE DAO has issues!");
        }
    }
}
