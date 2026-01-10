package test;

import com.jfeatures.msg.codegen.*;
import com.jfeatures.msg.codegen.dbmetadata.*;
import com.jfeatures.msg.codegen.audit.AuditFieldType;
import com.squareup.javapoet.JavaFile;
import java.sql.Types;
import java.util.*;

public class QuickAuditTest {
    public static void main(String[] args) throws Exception {
        // CREATE INSERT METADATA WITH ONLY TIMESTAMP AUDIT (no userId field)
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
        
        Map<ColumnMetadata, AuditFieldType> auditFields = new HashMap<>();
        auditFields.put(createdAt, AuditFieldType.CREATED_AT);
        
        InsertMetadata metadata = new InsertMetadata(
            "customer",
            Arrays.asList(firstName, createdAt),
            "INSERT INTO customer (first_name, created_at) VALUES (?, ?)",
            auditFields
        );
        
        System.out.println("=== TESTING INSERT WITH ONLY TIMESTAMP AUDIT FIELD ===\n");
        
        // Generate Controller
        JavaFile controllerFile = GenerateInsertController.createInsertController("Customer", metadata);
        String controllerCode = controllerFile.toString();
        
        System.out.println("CONTROLLER CODE:");
        System.out.println(controllerCode);
        
        // Verify
        boolean hasRequest = controllerCode.contains("HttpServletRequest request");
        boolean hasUserIdExtraction = controllerCode.contains("getUserId(request)");
        boolean hasLogging = controllerCode.contains("System.out.println");
        
        System.out.println("\n=== VERIFICATION ===");
        System.out.println("Has HttpServletRequest parameter: " + hasRequest);
        System.out.println("Has userId extraction: " + hasUserIdExtraction);
        System.out.println("Has logging: " + hasLogging);
        
        if (hasRequest && hasUserIdExtraction && hasLogging) {
            System.out.println("\n✓ ALL CHECKS PASSED!");
        } else {
            System.out.println("\n✗ SOME CHECKS FAILED!");
            System.exit(1);
        }
    }
}
