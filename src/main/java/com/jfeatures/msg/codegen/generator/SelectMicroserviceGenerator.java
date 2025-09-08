package com.jfeatures.msg.codegen.generator;

import com.jfeatures.msg.codegen.GenerateController;
import com.jfeatures.msg.codegen.GenerateDAO;
import com.jfeatures.msg.codegen.GenerateDTO;
import com.jfeatures.msg.codegen.GenerateDatabaseConfig;
import com.jfeatures.msg.codegen.GenerateSpringBootApp;
import com.jfeatures.msg.codegen.ParameterMetadataExtractor;
import com.jfeatures.msg.codegen.dbmetadata.ColumnMetadata;
import com.jfeatures.msg.codegen.dbmetadata.SqlMetadata;
import com.jfeatures.msg.codegen.domain.DBColumn;
import com.jfeatures.msg.codegen.domain.DatabaseConnection;
import com.jfeatures.msg.codegen.domain.GeneratedMicroservice;
import com.jfeatures.msg.codegen.util.SqlStatementType;
import com.jfeatures.msg.controller.CodeGenController;
import com.squareup.javapoet.JavaFile;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * Generates complete microservices for SELECT SQL statements.
 * Orchestrates the creation of DTO, Controller, and DAO components
 * specifically optimized for SELECT operations.
 */
@Slf4j
public class SelectMicroserviceGenerator {
    
    /**
     * Generates a complete microservice for SELECT SQL statements.
     * Creates all necessary components including DTO for result mapping,
     * Controller for REST endpoints, and DAO for database access.
     * 
     * @param sql the SELECT SQL statement to generate microservice for
     * @param businessDomainName the business domain name for the microservice
     * @param databaseConnection database connection components
     * @return a complete GeneratedMicroservice with all components
     * @throws Exception if generation fails
     */
    public GeneratedMicroservice generateSelectMicroservice(String sql, 
                                                           String businessDomainName, 
                                                           DatabaseConnection databaseConnection) throws Exception {
        
        validateInputParameters(sql, businessDomainName, databaseConnection);
        
        log.info("Generating SELECT microservice for business domain: {}", businessDomainName);
        
        // Extract metadata from database for SELECT columns
        SqlMetadata sqlMetadata = new SqlMetadata(databaseConnection.jdbcTemplate());
        CodeGenController codeGenController = new CodeGenController(sqlMetadata);
        List<ColumnMetadata> resultSetColumnDefinitions = codeGenController.selectColumnMetadata();
        
        // Extract SQL parameters from WHERE clause
        ArrayList<DBColumn> sqlWhereClauseParameters = extractSqlParametersFromStatement(sql, databaseConnection);
        
        // Generate Spring Boot application
        JavaFile springBootApplication = GenerateSpringBootApp.createSpringBootApp(businessDomainName);
        
        // Generate database configuration
        String databaseConfigContent = GenerateDatabaseConfig.createDatabaseConfig(businessDomainName);
        
        // Generate DTO for SELECT result mapping
        JavaFile dtoFile = GenerateDTO.dtoFromColumnMetadata(resultSetColumnDefinitions, businessDomainName);
        
        // Generate Controller with REST endpoints
        JavaFile controllerFile = GenerateController.createController(businessDomainName, sqlWhereClauseParameters);
        
        // Generate DAO using metadata approach - much simpler and more reliable than SQL parsing
        JavaFile daoFile = GenerateDAO.createDaoFromMetadata(businessDomainName, 
                                                             resultSetColumnDefinitions, 
                                                             sqlWhereClauseParameters, 
                                                             sql);
        
        log.info("Successfully generated SELECT microservice components for: {}", businessDomainName);
        
        return new GeneratedMicroservice(
            businessDomainName,
            springBootApplication,
            dtoFile,
            controllerFile,
            daoFile,
            databaseConfigContent,
            SqlStatementType.SELECT
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
    
    private ArrayList<DBColumn> extractSqlParametersFromStatement(String sql, DatabaseConnection databaseConnection) throws SQLException {
        ParameterMetadataExtractor extractor = new ParameterMetadataExtractor(databaseConnection.dataSource());
        List<DBColumn> parameters = extractor.extractParameters(sql);
        return new ArrayList<>(parameters);
    }
}