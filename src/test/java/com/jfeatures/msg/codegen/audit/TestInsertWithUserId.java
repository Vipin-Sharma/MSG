package com.jfeatures.msg.codegen.audit;

import com.jfeatures.msg.codegen.*;
import com.jfeatures.msg.codegen.dbmetadata.*;
import com.squareup.javapoet.JavaFile;
import java.sql.Types;
import java.util.*;

public class TestInsertWithUserId {
    public static void main(String[] args) throws Exception {
        // CREATE INSERT METADATA WITH BOTH timestamp AND userId audit fields
        ColumnMetadata firstName = new ColumnMetadata();
        firstName.setColumnName("first_name");
        firstName.setColumnTypeName("VARCHAR");
        firstName.setColumnType(Types.VARCHAR);
        firstName.setIsNullable(0);
        
        ColumnMetadata createdAt = new ColumnMetadata();
        createdAt.setColumnName("created_at");
        createdAt.setColumnTypeName("DATETIME");
        createdAt.setColumnType(Types.TIMESTAMP);
        createdAt.setIsNullable(1);
        
        ColumnMetadata createdBy = new ColumnMetadata();
        createdBy.setColumnName("created_by");
        createdBy.setColumnTypeName("VARCHAR");
        createdBy.setColumnType(Types.VARCHAR);
        createdBy.setIsNullable(1);
        
        Map<ColumnMetadata, AuditFieldType> auditFields = new HashMap<>();
        auditFields.put(createdAt, AuditFieldType.CREATED_AT);
        auditFields.put(createdBy, AuditFieldType.CREATED_BY);
        
        InsertMetadata metadata = new InsertMetadata(
            "customer",
            Arrays.asList(firstName, createdAt, createdBy),
            "INSERT INTO customer (first_name, created_at, created_by) VALUES (?, ?, ?)",
            auditFields
        );
        
        System.out.println("=== TESTING INSERT WITH BOTH TIMESTAMP AND USERID ===\n");
        
        // Generate DAO
        JavaFile daoFile = GenerateInsertDAO.createInsertDAO("Customer", metadata);
        String daoCode = daoFile.toString();
        
        System.out.println("DAO CODE:");
        System.out.println(daoCode);
        
        // Analysis
        System.out.println("\n" + "=".repeat(80));
        System.out.println("CRITICAL ANALYSIS - ANSWERING YOUR 3 QUESTIONS:");
        System.out.println("=".repeat(80));
        
        boolean hasRequestParam = daoCode.contains("HttpServletRequest request");
        boolean usesAuthUtil = daoCode.contains("AuthenticationUtil.getUserId(request)");
        boolean setsCreatedBy = daoCode.contains("createdBy");
        
        System.out.println("1. Does DAO have HttpServletRequest parameter? " + hasRequestParam);
        System.out.println("2. Does DAO use AuthenticationUtil to get userId? " + usesAuthUtil);
        System.out.println("3. Does DAO set created_by column? " + setsCreatedBy);
        
        if (hasRequestParam && usesAuthUtil && setsCreatedBy) {
            System.out.println("\n✓ DAO correctly handles created_by audit field!");
        } else {
            System.out.println("\n✗ DAO has issues with created_by handling!");
            if (!hasRequestParam) System.out.println("  - Missing HttpServletRequest parameter");
            if (!usesAuthUtil) System.out.println("  - Not using AuthenticationUtil");
            if (!setsCreatedBy) System.out.println("  - Not setting created_by column");
        }
    }
}
