package com.jfeatures.msg.codegen.generator;

import com.jfeatures.msg.codegen.GenerateDatabaseConfig;
import com.jfeatures.msg.codegen.GenerateInsertController;
import com.jfeatures.msg.codegen.GenerateInsertDAO;
import com.jfeatures.msg.codegen.GenerateInsertDTO;
import com.jfeatures.msg.codegen.GenerateSpringBootApp;
import com.jfeatures.msg.codegen.dbmetadata.ColumnMetadata;
import com.jfeatures.msg.codegen.dbmetadata.InsertMetadata;
import com.jfeatures.msg.codegen.dbmetadata.InsertMetadataExtractor;
import com.jfeatures.msg.codegen.domain.AuditConfiguration;
import com.jfeatures.msg.codegen.domain.DatabaseConnection;
import com.jfeatures.msg.codegen.domain.GeneratedMicroservice;
import com.jfeatures.msg.codegen.util.AuditColumnPartitioner;
import com.jfeatures.msg.codegen.util.SqlStatementType;
import com.squareup.javapoet.JavaFile;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * Generates complete microservices for INSERT SQL statements.
 * Following Vipin's Principle: Single responsibility - INSERT microservice orchestration only.
 */
@Slf4j
public class InsertMicroserviceGenerator {

    /**
     * Generates a complete microservice for INSERT SQL statements without audit support.
     * Single responsibility: Orchestrate INSERT microservice generation.
     */
    public GeneratedMicroservice generateInsertMicroservice(String sql,
                                                           String businessDomainName,
                                                           DatabaseConnection databaseConnection) throws Exception {
        return generateInsertMicroservice(sql, businessDomainName, databaseConnection, AuditConfiguration.disabled());
    }

    /**
     * Generates a complete microservice for INSERT SQL statements with optional audit support.
     * When audit is enabled, audit columns are excluded from the DTO and populated from HTTP headers.
     *
     * @param sql The INSERT SQL statement
     * @param businessDomainName The business domain name (e.g., "Customer")
     * @param databaseConnection The database connection for metadata extraction
     * @param auditConfig The audit configuration (enabled/disabled, header name, audit columns)
     * @return Generated microservice with all components
     * @throws Exception if generation fails
     */
    public GeneratedMicroservice generateInsertMicroservice(String sql,
                                                           String businessDomainName,
                                                           DatabaseConnection databaseConnection,
                                                           AuditConfiguration auditConfig) throws Exception {

        validateInputParameters(sql, businessDomainName, databaseConnection);

        // Default to disabled if null
        if (auditConfig == null) {
            auditConfig = AuditConfiguration.disabled();
        }

        log.info("Generating INSERT microservice for business domain: {}", businessDomainName);

        // Extract INSERT metadata from SQL statement
        InsertMetadataExtractor insertExtractor = new InsertMetadataExtractor(
            databaseConnection.dataSource()
        );
        InsertMetadata insertMetadata = insertExtractor.extractInsertMetadata(sql);
        log.info("Extracted INSERT metadata for table: {}", insertMetadata.tableName());

        // Generate Spring Boot application
        JavaFile springBootApplication = GenerateSpringBootApp.createSpringBootApp(businessDomainName);

        // Generate database configuration
        String databaseConfigContent = GenerateDatabaseConfig.createDatabaseConfig(businessDomainName);

        // Partition columns into regular and audit columns
        List<ColumnMetadata> regularColumns = new ArrayList<>();
        List<ColumnMetadata> auditColumns = new ArrayList<>();
        AuditColumnPartitioner.partitionColumns(insertMetadata.insertColumns(), auditConfig, regularColumns, auditColumns);

        if (auditConfig.enabled()) {
            log.info("Audit enabled: {} regular columns, {} audit columns",
                regularColumns.size(), auditColumns.size());
        }

        // Create metadata for DTO generation (excludes audit columns when audit is enabled)
        InsertMetadata dtoMetadata = auditConfig.enabled()
            ? new InsertMetadata(insertMetadata.tableName(), regularColumns, insertMetadata.originalSql())
            : insertMetadata;

        // Generate DTO for INSERT operations (excludes audit columns)
        JavaFile insertDTO = GenerateInsertDTO.createInsertDTO(businessDomainName, dtoMetadata);

        // Generate Controller with INSERT REST endpoints (with header parameter when audit is enabled)
        JavaFile controllerFile = GenerateInsertController.createInsertController(
            businessDomainName, insertMetadata, auditConfig);

        // Generate DAO for INSERT operations (with userId parameter when audit is enabled)
        JavaFile daoFile = GenerateInsertDAO.createInsertDAO(
            businessDomainName, insertMetadata, auditConfig);

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