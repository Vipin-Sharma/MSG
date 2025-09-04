# CLAUDE.md

This file provides AI-specific guidance to Claude Code (claude.ai/code) when working with the MSG (Microservice Generator) repository.

## Project Overview for AI Context

MSG is an enterprise-grade Java 21 application that transforms SQL statements into complete Spring Boot microservices using a metadata-driven approach.

**Core Architecture Understanding:**
- **Main Entry**: `MicroServiceGenerator.java` - CLI orchestrator
- **Metadata Extraction**: Database-driven (not SQL parsing) for reliability
- **Code Generation**: JavaPoet-based with single responsibility pattern
- **Target Output**: Complete Spring Boot projects with DTOs, DAOs, Controllers

## Essential Commands for AI Assistance

### Build and Test Commands
```bash
# Compile project
mvn clean compile

# Run tests with coverage
mvn test
mvn jacoco:report

# Package for distribution
mvn clean package
```

### Code Generation Commands
```bash
# Main generation command pattern
mvn exec:java -Dexec.mainClass="com.jfeatures.msg.codegen.MicroServiceGenerator" \
  -Dexec.args="--name {BusinessName} --destination {OutputPath} --sql-file {SqlFile}"

# Common SQL file patterns in src/main/resources/
# - sample_parameterized_sql.sql (SELECT → GET APIs)
# - sample_insert_parameterized.sql (INSERT → POST APIs)  
# - sample_update_parameterized.sql (UPDATE → PUT APIs)
# - sample_delete_parameterized.sql (DELETE → DELETE APIs)

# Debug mode for troubleshooting
mvn -X exec:java -Dexec.mainClass="com.jfeatures.msg.codegen.MicroServiceGenerator" \
  -Dexec.args="--name Customer --destination ./output"
```

## Key Architecture Components for AI Understanding

**Main Orchestrator**: `MicroServiceGenerator` - Entry point that coordinates the entire process

**Core Workflow**:
1. `SqlFileResolver` - Finds SQL files (fallback: UPDATE → INSERT → DELETE → SELECT)
2. `SqlStatementDetector` - Identifies SQL type via regex patterns
3. **Metadata Extractors** - Database-driven extraction (not parsing):
   - `SqlMetadata` - SELECT ResultSet metadata via JDBC
   - `InsertMetadataExtractor` - Column info via DatabaseMetaData
   - `UpdateMetadataExtractor` - SET/WHERE analysis via database metadata
   - `ParameterMetadataExtractor` - Parameter extraction for DELETE

**Code Generators** (JavaPoet-based, single public method each):
- **DTOs**: `GenerateDTO`, `GenerateInsertDTO`, `GenerateUpdateDTO`, `GenerateDeleteDTO`
- **Controllers**: `GenerateController`, `GenerateInsertController`, `GenerateUpdateController`, `GenerateDeleteController`
- **DAOs**: `GenerateDAO`, `GenerateInsertDAO`, `GenerateUpdateDAO`, `GenerateDeleteDAO`
- **Config**: `GenerateSpringBootApp`, `GenerateDatabaseConfig`

## AI Development Guidelines

### Single Responsibility Pattern
Every generator class has exactly one public static method:
```java
// ✅ Correct pattern
public class GenerateInsertDAO {
    public static TypeSpec createInsertDAO(String businessName, InsertMetadata metadata) {
        // Implementation
    }
}
```

### Key File Locations for AI Assistance
- **Main CLI**: `src/main/java/com/jfeatures/msg/codegen/MicroServiceGenerator.java`
- **Generators**: `src/main/java/com/jfeatures/msg/codegen/Generate*.java`
- **Metadata**: `src/main/java/com/jfeatures/msg/codegen/dbmetadata/`
- **Config**: `src/main/java/com/jfeatures/msg/config/DataSourceConfig.java`

### Testing Approach
- **Reality-Based Testing**: Observe actual generated code behavior, then test
- **Current Coverage**: 89% with 531 tests
- **Test Command**: `mvn test && mvn jacoco:report`

### Critical Security Issues for AI Awareness
1. **Hardcoded credentials** in `DataSourceConfig.java` (use environment variables)
2. **Resource leak** in `ReadFileFromResources.java` (add null checks)
3. **SQL injection** in `SqlMetadata.java` (validate inputs)
4. **Directory traversal** in file operations (validate paths)

### Common AI Tasks
- **Adding new generators**: Follow single responsibility, use JavaPoet, add tests
- **Modifying metadata extraction**: Ensure proper JDBC resource handling
- **Code generation fixes**: Use JavaPoet type-safe construction
- **Testing**: Reality-based approach with comprehensive coverage

## Important Context
- **Database**: SQL Server with hardcoded sakila database
- **Java Version**: Java 21 with text blocks and records
- **Type Mapping**: `SQLServerDataTypeEnum` for JDBC → Java conversion
- **Generated Structure**: Standard Maven layout with Spring Boot conventions

