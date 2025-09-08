package com.jfeatures.msg.codegen.generator;

import com.jfeatures.msg.codegen.GenerateDatabaseConfig;
import com.jfeatures.msg.codegen.GenerateDeleteController;
import com.jfeatures.msg.codegen.GenerateDeleteDAO;
import com.jfeatures.msg.codegen.GenerateDeleteDTO;
import com.jfeatures.msg.codegen.GenerateSpringBootApp;
import com.jfeatures.msg.codegen.dbmetadata.ColumnMetadata;
import com.jfeatures.msg.codegen.dbmetadata.DeleteMetadata;
import com.jfeatures.msg.codegen.domain.DatabaseConnection;
import com.jfeatures.msg.codegen.domain.GeneratedMicroservice;
import com.jfeatures.msg.codegen.util.SqlStatementType;
import com.squareup.javapoet.JavaFile;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * Generates complete microservices for DELETE SQL statements.
 * Following Vipin's Principle: Single responsibility - DELETE microservice orchestration only.
 */
@Slf4j
public class DeleteMicroserviceGenerator {
    
    /**
     * Generates a complete microservice for DELETE SQL statements.
     * Single responsibility: Orchestrate DELETE microservice generation.
     */
    public GeneratedMicroservice generateDeleteMicroservice(String sql, 
                                                           String businessDomainName, 
                                                           DatabaseConnection databaseConnection) throws Exception {
        
        validateInputParameters(sql, businessDomainName, databaseConnection);
        
        log.info("Generating DELETE microservice for business domain: {}", businessDomainName);
        
        // Extract table name from SQL
        String tableName = extractTableNameFromDeleteSql(sql);
        log.info("Extracted table name from DELETE SQL: {}", tableName);
        
        // Extract WHERE clause parameters using the reliable parameter extractor (same as SELECT)
        com.jfeatures.msg.codegen.ParameterMetadataExtractor parameterExtractor = 
            new com.jfeatures.msg.codegen.ParameterMetadataExtractor(databaseConnection.dataSource());
        List<com.jfeatures.msg.codegen.domain.DBColumn> whereParameters = parameterExtractor.extractParameters(sql);
        
        // Create simplified DeleteMetadata using original parameters
        DeleteMetadata deleteMetadata = new DeleteMetadata(tableName, convertToColumnMetadata(whereParameters), sql);
        log.info("Extracted DELETE metadata for {} WHERE parameters in table: {}", whereParameters.size(), deleteMetadata.tableName());
        
        // Generate Spring Boot application
        JavaFile springBootApplication = GenerateSpringBootApp.createSpringBootApp(businessDomainName);
        
        // Generate database configuration
        String databaseConfigContent = GenerateDatabaseConfig.createDatabaseConfig(businessDomainName);
        
        // Generate DTO for DELETE operations (WHERE clause parameters)
        JavaFile deleteDTO = GenerateDeleteDTO.createDeleteDTO(businessDomainName, deleteMetadata);
        
        // Generate Controller with DELETE REST endpoints
        JavaFile controllerFile = GenerateDeleteController.createDeleteController(businessDomainName, deleteMetadata);
        
        // Generate DAO for DELETE operations
        JavaFile daoFile = GenerateDeleteDAO.createDeleteDAO(businessDomainName, deleteMetadata);
        
        log.info("Successfully generated DELETE microservice components for: {}", businessDomainName);
        
        return new GeneratedMicroservice(
            businessDomainName,
            springBootApplication,
            deleteDTO,
            controllerFile,
            daoFile,
            databaseConfigContent,
            SqlStatementType.DELETE
        );
    }
    
    private void validateInputParameters(String sql, String businessDomainName, DatabaseConnection databaseConnection) {
        if (sql == null || sql.trim().isEmpty()) {
            throw new IllegalArgumentException("SQL statement cannot be null or empty");
        }
        if (businessDomainName == null || businessDomainName.trim().isEmpty()) {
            throw new IllegalArgumentException("Business domain name cannot be null or empty");
        }
        if (databaseConnection == null) {
            throw new IllegalArgumentException("Database connection cannot be null");
        }
    }
    
    /**
     * Extracts table name from DELETE SQL statement.
     */
    private String extractTableNameFromDeleteSql(String sql) {
        // Simple extraction - find text between DELETE FROM and WHERE/whitespace
        String upperSql = sql.toUpperCase().trim();
        int fromIndex = upperSql.indexOf("DELETE FROM");
        if (fromIndex == -1) {
            throw new IllegalArgumentException("Invalid DELETE SQL: missing DELETE FROM clause");
        }
        
        String afterFrom = sql.substring(fromIndex + "DELETE FROM".length()).trim();
        int whereIndex = afterFrom.toUpperCase().indexOf("WHERE");
        
        String tableName;
        if (whereIndex > 0) {
            tableName = afterFrom.substring(0, whereIndex).trim();
        } else {
            tableName = afterFrom.trim();
        }
        
        // Remove any trailing semicolon or whitespace
        tableName = tableName.replaceAll("[;\\s]+$", "");
        
        return tableName;
    }
    
    /**
     * Converts DBColumn objects to ColumnMetadata objects.
     */
    private List<ColumnMetadata> convertToColumnMetadata(List<com.jfeatures.msg.codegen.domain.DBColumn> dbColumns) {
        return dbColumns.stream()
            .map(dbCol -> {
                ColumnMetadata colMeta = new ColumnMetadata();
                colMeta.setColumnName(dbCol.columnName());
                // Convert JDBC type name to database type name for SQLServerDataTypeEnum compatibility
                colMeta.setColumnTypeName(mapJdbcTypeToDbType(dbCol.jdbcType()));
                colMeta.setColumnType(java.sql.Types.VARCHAR); // Default type, actual type from DBColumn if needed
                colMeta.setIsNullable(1); // Assume nullable for WHERE parameters
                return colMeta;
            })
            .toList();
    }
    
    /**
     * Maps JDBC type names to database type names for SQLServerDataTypeEnum compatibility.
     */
    private String mapJdbcTypeToDbType(String jdbcType) {
        return switch (jdbcType) {
            case "INTEGER" -> "INT";
            case "CHAR" -> "CHAR";
            case "VARCHAR" -> "NVARCHAR";
            case "BIGINT" -> "BIGINT";
            case "DECIMAL" -> "DECIMAL";
            case "FLOAT" -> "FLOAT";
            case "REAL" -> "REAL";
            case "BIT" -> "BIT";
            case "DATE" -> "DATE";
            case "TIMESTAMP" -> "DATETIME2";
            default -> "NVARCHAR"; // Default fallback to String type
        };
    }
}