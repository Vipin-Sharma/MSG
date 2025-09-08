package com.jfeatures.msg.codegen.generator;

import com.jfeatures.msg.codegen.GenerateDatabaseConfig;
import com.jfeatures.msg.codegen.GenerateSpringBootApp;
import com.jfeatures.msg.codegen.GenerateUpdateController;
import com.jfeatures.msg.codegen.GenerateUpdateDAO;
import com.jfeatures.msg.codegen.GenerateUpdateDTO;
import com.jfeatures.msg.codegen.dbmetadata.UpdateMetadata;
import com.jfeatures.msg.codegen.dbmetadata.UpdateMetadataExtractor;
import com.jfeatures.msg.codegen.domain.DatabaseConnection;
import com.jfeatures.msg.codegen.domain.GeneratedMicroservice;
import com.jfeatures.msg.codegen.util.SqlStatementType;
import com.squareup.javapoet.JavaFile;
import lombok.extern.slf4j.Slf4j;

/**
 * Generates complete microservices for UPDATE SQL statements.
 * Orchestrates the creation of DTO, Controller, and DAO components
 * specifically optimized for UPDATE operations.
 */
@Slf4j
public class UpdateMicroserviceGenerator {
    
    /**
     * Generates a complete microservice for UPDATE SQL statements.
     * Creates all necessary components including DTOs for request data,
     * Controller for REST endpoints, and DAO for database updates.
     * 
     * @param sql the UPDATE SQL statement to generate microservice for
     * @param businessDomainName the business domain name for the microservice
     * @param databaseConnection database connection components
     * @return a complete GeneratedMicroservice with all components
     * @throws Exception if generation fails
     */
    public GeneratedMicroservice generateUpdateMicroservice(String sql, 
                                                           String businessDomainName, 
                                                           DatabaseConnection databaseConnection) throws Exception {
        
        validateInputParameters(sql, businessDomainName, databaseConnection);
        
        log.info("Generating UPDATE microservice for business domain: {}", businessDomainName);
        
        // Extract UPDATE metadata from SQL statement
        UpdateMetadataExtractor updateExtractor = new UpdateMetadataExtractor(
            databaseConnection.dataSource()
        );
        UpdateMetadata updateMetadata = updateExtractor.extractUpdateMetadata(sql);
        log.info("Extracted UPDATE metadata for table: {}", updateMetadata.tableName());
        
        // Generate Spring Boot application
        JavaFile springBootApplication = GenerateSpringBootApp.createSpringBootApp(businessDomainName);
        
        // Generate database configuration
        String databaseConfigContent = GenerateDatabaseConfig.createDatabaseConfig(businessDomainName);
        
        // Generate DTOs for UPDATE operations (both SET and WHERE DTOs)
        JavaFile updateDTO = GenerateUpdateDTO.createUpdateDTO(businessDomainName, updateMetadata);
        
        // Generate Controller with UPDATE REST endpoints
        JavaFile controllerFile = GenerateUpdateController.createUpdateController(businessDomainName, updateMetadata);
        
        // Generate DAO for UPDATE operations
        JavaFile daoFile = GenerateUpdateDAO.createUpdateDAO(businessDomainName, updateMetadata);
        
        log.info("Successfully generated UPDATE microservice components for: {}", businessDomainName);
        
        return new GeneratedMicroservice(
            businessDomainName,
            springBootApplication,
            updateDTO, // Note: This represents the main DTO (additional WHERE DTO is handled internally)
            controllerFile,
            daoFile,
            databaseConfigContent,
            SqlStatementType.UPDATE
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