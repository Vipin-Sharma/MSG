package com.jfeatures.msg.codegen.generator;

import com.jfeatures.msg.codegen.GenerateDatabaseConfig;
import com.jfeatures.msg.codegen.GenerateInsertController;
import com.jfeatures.msg.codegen.GenerateInsertDAO;
import com.jfeatures.msg.codegen.GenerateInsertDTO;
import com.jfeatures.msg.codegen.GenerateSpringBootApp;
import com.jfeatures.msg.codegen.dbmetadata.InsertMetadata;
import com.jfeatures.msg.codegen.dbmetadata.InsertMetadataExtractor;
import com.jfeatures.msg.codegen.domain.DatabaseConnection;
import com.jfeatures.msg.codegen.domain.GeneratedMicroservice;
import com.jfeatures.msg.codegen.util.SqlStatementType;
import com.squareup.javapoet.JavaFile;
import lombok.extern.slf4j.Slf4j;

/**
 * Generates complete microservices for INSERT SQL statements.
 * Following Vipin's Principle: Single responsibility - INSERT microservice orchestration only.
 */
@Slf4j
public class InsertMicroserviceGenerator {
    
    /**
     * Generates a complete microservice for INSERT SQL statements.
     * Single responsibility: Orchestrate INSERT microservice generation.
     */
    public GeneratedMicroservice generateInsertMicroservice(String sql, 
                                                           String businessDomainName, 
                                                           DatabaseConnection databaseConnection) throws Exception {
        
        validateInputParameters(sql, businessDomainName, databaseConnection);
        
        log.info("Generating INSERT microservice for business domain: {}", businessDomainName);
        
        // Extract INSERT metadata from SQL statement
        InsertMetadataExtractor insertExtractor = new InsertMetadataExtractor(
            databaseConnection.dataSource(), 
            databaseConnection.namedParameterJdbcTemplate()
        );
        InsertMetadata insertMetadata = insertExtractor.extractInsertMetadata(sql);
        log.info("Extracted INSERT metadata for table: {}", insertMetadata.tableName());
        
        // Generate Spring Boot application
        JavaFile springBootApplication = GenerateSpringBootApp.createSpringBootApp(businessDomainName);
        
        // Generate database configuration
        String databaseConfigContent = GenerateDatabaseConfig.createDatabaseConfig(businessDomainName);
        
        // Generate DTO for INSERT operations
        JavaFile insertDTO = GenerateInsertDTO.createInsertDTO(businessDomainName, insertMetadata);
        
        // Generate Controller with INSERT REST endpoints
        JavaFile controllerFile = GenerateInsertController.createInsertController(businessDomainName, insertMetadata);
        
        // Generate DAO for INSERT operations
        JavaFile daoFile = GenerateInsertDAO.createInsertDAO(businessDomainName, insertMetadata);
        
        log.info("Successfully generated INSERT microservice components for: {}", businessDomainName);
        
        return new GeneratedMicroservice(
            businessDomainName,
            springBootApplication,
            insertDTO,
            controllerFile,
            daoFile,
            databaseConfigContent,
            SqlStatementType.INSERT
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