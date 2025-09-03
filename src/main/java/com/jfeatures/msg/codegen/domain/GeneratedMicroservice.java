package com.jfeatures.msg.codegen.domain;

import com.jfeatures.msg.codegen.util.SqlStatementType;
import com.squareup.javapoet.JavaFile;

/**
 * Represents a complete generated microservice with all its components.
 * This value object encapsulates all the generated files and metadata
 * needed to write a complete Spring Boot microservice to the filesystem.
 */
public record GeneratedMicroservice(
    String businessDomainName,
    JavaFile springBootApplication,
    JavaFile dtoFile,
    JavaFile controllerFile,
    JavaFile daoFile,
    String databaseConfigContent,
    SqlStatementType statementType
) {
    
    public GeneratedMicroservice {
        if (businessDomainName == null || businessDomainName.trim().isEmpty()) {
            throw new IllegalArgumentException("Business domain name cannot be null or empty");
        }
        if (springBootApplication == null) {
            throw new IllegalArgumentException("Spring Boot application file cannot be null");
        }
        if (dtoFile == null) {
            throw new IllegalArgumentException("DTO file cannot be null");
        }
        if (controllerFile == null) {
            throw new IllegalArgumentException("Controller file cannot be null");
        }
        if (daoFile == null) {
            throw new IllegalArgumentException("DAO file cannot be null");
        }
        if (databaseConfigContent == null || databaseConfigContent.trim().isEmpty()) {
            throw new IllegalArgumentException("Database config content cannot be null or empty");
        }
        if (statementType == null) {
            throw new IllegalArgumentException("SQL statement type cannot be null");
        }
    }
}