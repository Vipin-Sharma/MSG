package com.jfeatures.msg.codegen.generator;

import com.jfeatures.msg.codegen.GenerateDatabaseConfig;
import com.jfeatures.msg.codegen.GenerateSpringBootApp;
import com.jfeatures.msg.codegen.GenerateUpdateController;
import com.jfeatures.msg.codegen.GenerateUpdateDAO;
import com.jfeatures.msg.codegen.GenerateUpdateDTO;
import com.jfeatures.msg.codegen.dbmetadata.ColumnMetadata;
import com.jfeatures.msg.codegen.dbmetadata.UpdateMetadata;
import com.jfeatures.msg.codegen.dbmetadata.UpdateMetadataExtractor;
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
 * Generates complete microservices for UPDATE SQL statements.
 * Orchestrates the creation of DTO, Controller, and DAO components
 * specifically optimized for UPDATE operations.
 */
@Slf4j
public class UpdateMicroserviceGenerator {

    /**
     * Generates a complete microservice for UPDATE SQL statements without audit support.
     */
    public GeneratedMicroservice generateUpdateMicroservice(String sql,
                                                           String businessDomainName,
                                                           DatabaseConnection databaseConnection) throws Exception {
        return generateUpdateMicroservice(sql, businessDomainName, databaseConnection, AuditConfiguration.disabled());
    }

    /**
     * Generates a complete microservice for UPDATE SQL statements with optional audit support.
     * When audit is enabled, audit columns are excluded from the DTO and populated from HTTP headers.
     *
     * @param sql the UPDATE SQL statement to generate microservice for
     * @param businessDomainName the business domain name for the microservice
     * @param databaseConnection database connection components
     * @param auditConfig the audit configuration for user context support
     * @return a complete GeneratedMicroservice with all components
     * @throws Exception if generation fails
     */
    public GeneratedMicroservice generateUpdateMicroservice(String sql,
                                                           String businessDomainName,
                                                           DatabaseConnection databaseConnection,
                                                           AuditConfiguration auditConfig) throws Exception {

        validateInputParameters(sql, businessDomainName, databaseConnection);

        // Default to disabled if null
        if (auditConfig == null) {
            auditConfig = AuditConfiguration.disabled();
        }

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

        // Partition SET columns into regular and audit columns
        List<ColumnMetadata> regularSetColumns = new ArrayList<>();
        List<ColumnMetadata> auditSetColumns = new ArrayList<>();
        AuditColumnPartitioner.partitionColumns(updateMetadata.setColumns(), auditConfig, regularSetColumns, auditSetColumns);

        if (auditConfig.enabled()) {
            log.info("Audit enabled: {} regular SET columns, {} audit SET columns",
                regularSetColumns.size(), auditSetColumns.size());
        }

        // Create metadata for DTO generation (excludes audit columns when audit is enabled)
        UpdateMetadata dtoMetadata = auditConfig.enabled()
            ? new UpdateMetadata(updateMetadata.tableName(), regularSetColumns, updateMetadata.whereColumns(), updateMetadata.originalSql())
            : updateMetadata;

        // Generate DTOs for UPDATE operations (excludes audit columns from SET)
        JavaFile updateDTO = GenerateUpdateDTO.createUpdateDTO(businessDomainName, dtoMetadata);

        // Generate Controller with UPDATE REST endpoints (with header parameter when audit is enabled)
        JavaFile controllerFile = GenerateUpdateController.createUpdateController(
            businessDomainName, updateMetadata, auditConfig);

        // Generate DAO for UPDATE operations (with userId parameter when audit is enabled)
        JavaFile daoFile = GenerateUpdateDAO.createUpdateDAO(
            businessDomainName, updateMetadata, auditConfig);

        log.info("Successfully generated UPDATE microservice components for: {}", businessDomainName);

        return new GeneratedMicroservice(
            businessDomainName,
            springBootApplication,
            updateDTO,
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