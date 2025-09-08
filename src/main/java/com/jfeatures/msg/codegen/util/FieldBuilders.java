package com.jfeatures.msg.codegen.util;

import com.jfeatures.msg.codegen.SQLServerDataTypeEnum;
import com.jfeatures.msg.codegen.dbmetadata.ColumnMetadata;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.TypeName;
import javax.lang.model.element.Modifier;
import javax.sql.DataSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/**
 * Specialized builder for JavaPoet field specifications.
 * Follows Single Responsibility Principle - only field creation logic.
 */
public final class FieldBuilders {
    
    private FieldBuilders() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
    
    // =========================== JDBC TEMPLATE FIELDS ===========================
    
    /**
     * Creates a private final NamedParameterJdbcTemplate field.
     */
    public static FieldSpec jdbcTemplateField(String fieldName) {
        validateNotEmpty(fieldName, "fieldName");
        
        return FieldSpec.builder(NamedParameterJdbcTemplate.class, fieldName, 
                Modifier.PRIVATE, Modifier.FINAL)
                .build();
    }
    
    /**
     * Creates a private final DataSource field.
     */
    public static FieldSpec dataSourceField(String fieldName) {
        validateNotEmpty(fieldName, "fieldName");
        
        return FieldSpec.builder(DataSource.class, fieldName, 
                Modifier.PRIVATE, Modifier.FINAL)
                .build();
    }
    
    // =========================== SQL FIELDS ===========================
    
    /**
     * Creates a private static final SQL field with text block formatting.
     */
    public static FieldSpec sqlField(String sql, String fieldName) {
        validateNotEmpty(sql, "sql");
        validateNotEmpty(fieldName, "fieldName");
        
        return FieldSpec.builder(String.class, fieldName, 
                Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                .initializer("\"\"\"\n$L\"\"\"", sql)
                .build();
    }
    
    /**
     * Creates a SQL field with JavaDoc documentation.
     */
    public static FieldSpec sqlFieldWithJavaDoc(String sql, String fieldName, String operation, String businessName) {
        validateNotEmpty(sql, "sql");
        validateNotEmpty(fieldName, "fieldName");
        validateNotEmpty(operation, "operation");
        validateNotEmpty(businessName, "businessName");
        
        return FieldSpec.builder(String.class, fieldName, 
                Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                .initializer("\"\"\"\n$L\"\"\"", sql)
                .addJavadoc("SQL statement for $L $L operations", businessName.toLowerCase(), operation.toLowerCase())
                .build();
    }
    
    // =========================== DTO FIELDS ===========================
    
    /**
     * Creates a DTO field from column metadata with proper type mapping.
     */
    public static FieldSpec dtoField(ColumnMetadata columnMetadata) {
        if (columnMetadata == null) {
            throw new IllegalArgumentException("Column metadata cannot be null");
        }
        
        String fieldName = NamingConventions.fieldName(
                columnMetadata.getColumnAlias() != null ? 
                        columnMetadata.getColumnAlias() : 
                        columnMetadata.getColumnName()
        );
        
        Class<?> fieldType = SQLServerDataTypeEnum.getClassForType(columnMetadata.getColumnTypeName());
        
        return FieldSpec.builder(fieldType, fieldName)
                .addModifiers(Modifier.PRIVATE)
                .addJavadoc("Database column: $L (Type: $L)", 
                           columnMetadata.getColumnName(), 
                           columnMetadata.getColumnTypeName())
                .build();
    }
    
    /**
     * Creates a public DTO field (for Lombok @Value classes).
     */
    public static FieldSpec publicDtoField(ColumnMetadata columnMetadata) {
        if (columnMetadata == null) {
            throw new IllegalArgumentException("Column metadata cannot be null");
        }
        
        String fieldName = NamingConventions.fieldName(
                columnMetadata.getColumnAlias() != null ? 
                        columnMetadata.getColumnAlias() : 
                        columnMetadata.getColumnName()
        );
        
        Class<?> fieldType = SQLServerDataTypeEnum.getClassForType(columnMetadata.getColumnTypeName());
        
        return FieldSpec.builder(fieldType, fieldName)
                .addModifiers(Modifier.PUBLIC)
                .build();
    }
    
    // =========================== DEPENDENCY INJECTION FIELDS ===========================
    
    /**
     * Creates a DAO field for dependency injection in controllers.
     */
    public static FieldSpec daoField(TypeName daoType, String businessName) {
        validateNotNull(daoType, "daoType");
        validateNotEmpty(businessName, "businessName");
        
        String fieldName = NamingConventions.daoFieldName(businessName);
        
        return FieldSpec.builder(daoType, fieldName)
                .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                .addJavadoc("Data access object for $L operations", businessName.toLowerCase())
                .build();
    }
    
    // =========================== VALIDATION HELPERS ===========================
    
    private static void validateNotEmpty(String value, String parameterName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(parameterName + " cannot be null or empty");
        }
    }
    
    private static void validateNotNull(Object value, String parameterName) {
        if (value == null) {
            throw new IllegalArgumentException(parameterName + " cannot be null");
        }
    }
}