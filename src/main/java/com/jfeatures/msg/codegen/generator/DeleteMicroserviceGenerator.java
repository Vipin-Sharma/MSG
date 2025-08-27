package com.jfeatures.msg.codegen.generator;

import com.jfeatures.msg.codegen.domain.DatabaseConnection;
import com.jfeatures.msg.codegen.domain.GeneratedMicroservice;
import com.jfeatures.msg.codegen.dbmetadata.DeleteMetadata;
import com.jfeatures.msg.codegen.dbmetadata.DeleteMetadataExtractor;
import com.jfeatures.msg.codegen.util.SqlStatementType;
import com.jfeatures.msg.codegen.GenerateSpringBootApp;
import com.jfeatures.msg.codegen.GenerateDatabaseConfig;
import com.jfeatures.msg.codegen.GenerateDeleteController;
import com.jfeatures.msg.codegen.GenerateDeleteDAO;
import com.squareup.javapoet.JavaFile;
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
        
        // Extract DELETE metadata from SQL statement
        DeleteMetadataExtractor deleteExtractor = new DeleteMetadataExtractor(
            databaseConnection.dataSource(), 
            databaseConnection.namedParameterJdbcTemplate()
        );
        DeleteMetadata deleteMetadata = deleteExtractor.extractDeleteMetadata(sql);
        log.info("Extracted DELETE metadata for table: {}", deleteMetadata.tableName());
        
        // Generate Spring Boot application
        JavaFile springBootApplication = GenerateSpringBootApp.createSpringBootApp(businessDomainName);
        
        // Generate database configuration
        String databaseConfigContent = GenerateDatabaseConfig.createDatabaseConfig(businessDomainName);
        
        // Generate Controller with DELETE REST endpoints
        JavaFile controllerFile = GenerateDeleteController.createDeleteController(businessDomainName, deleteMetadata);
        
        // Generate DAO for DELETE operations
        JavaFile daoFile = GenerateDeleteDAO.createDeleteDAO(businessDomainName, deleteMetadata);
        
        log.info("Successfully generated DELETE microservice components for: {}", businessDomainName);
        
        // Note: DELETE operations don't typically need a separate DTO, using null for consistency with GeneratedMicroservice structure
        return new GeneratedMicroservice(
            businessDomainName,
            springBootApplication,
            null, // No DTO needed for DELETE operations 
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
}