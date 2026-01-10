package com.jfeatures.msg.codegen.audit;

import com.jfeatures.msg.codegen.*;
import com.jfeatures.msg.codegen.dbmetadata.*;
import com.squareup.javapoet.JavaFile;
import java.sql.Types;
import java.util.*;

public class QuickUpdateAuditTest {
    public static void main(String[] args) throws Exception {
        // CREATE UPDATE METADATA WITH ONLY TIMESTAMP AUDIT (no userId field)
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
        
        ColumnMetadata customerId = new ColumnMetadata();
        customerId.setColumnName("customer_id");
        customerId.setColumnTypeName("INT");
        customerId.setColumnType(Types.INTEGER);
        customerId.setIsNullable(0);
        
        Map<ColumnMetadata, AuditFieldType> auditFields = new HashMap<>();
        auditFields.put(updatedAt, AuditFieldType.UPDATED_AT);
        
        UpdateMetadata metadata = new UpdateMetadata(
            "customer",
            Arrays.asList(firstName, updatedAt),
            Collections.singletonList(customerId),
            "UPDATE customer SET first_name = ?, updated_at = ? WHERE customer_id = ?",
            auditFields
        );
        
        System.out.println("=== TESTING UPDATE WITH ONLY TIMESTAMP AUDIT FIELD ===\n");
        
        // Generate Controller
        JavaFile controllerFile = GenerateUpdateController.createUpdateController("Customer", metadata);
        String controllerCode = controllerFile.toString();
        
        System.out.println("CONTROLLER CODE:");
        System.out.println(controllerCode);
        
        // Generate DAO
        JavaFile daoFile = GenerateUpdateDAO.createUpdateDAO("Customer", metadata);
        String daoCode = daoFile.toString();
        
        System.out.println("\n\nDAO CODE:");
        System.out.println(daoCode);
        
        // Verify Controller
        boolean ctrlHasRequest = controllerCode.contains("HttpServletRequest request");
        boolean ctrlHasUserIdExtraction = controllerCode.contains("getUserId(request)");
        boolean ctrlHasLogging = controllerCode.contains("System.out.println");
        
        // Verify DAO
        boolean daoHasRequest = daoCode.contains("HttpServletRequest request");
        
        System.out.println("\n=== CONTROLLER VERIFICATION ===");
        System.out.println("Has HttpServletRequest parameter: " + ctrlHasRequest);
        System.out.println("Has userId extraction: " + ctrlHasUserIdExtraction);
        System.out.println("Has logging: " + ctrlHasLogging);
        
        System.out.println("\n=== DAO VERIFICATION ===");
        System.out.println("Has HttpServletRequest parameter: " + daoHasRequest);
        
        if (ctrlHasRequest && ctrlHasUserIdExtraction && ctrlHasLogging && daoHasRequest) {
            System.out.println("\n✓ ALL CHECKS PASSED!");
        } else {
            System.out.println("\n✗ SOME CHECKS FAILED!");
            System.exit(1);
        }
    }
}
