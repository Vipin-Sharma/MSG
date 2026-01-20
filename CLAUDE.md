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

# Generation with audit/user context support
mvn exec:java -Dexec.mainClass="com.jfeatures.msg.codegen.MicroServiceGenerator" \
  -Dexec.args="--name Customer --destination ./output --sql-file sample_insert_parameterized.sql \
               --enable-audit --user-header X-User-ID --audit-columns created_by,updated_by"

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
- **Current Coverage**: 89%+ with 1050+ tests (unit + E2E)
- **Test Command**: `mvn test && mvn jacoco:report`
- **E2E Tests**: `mvn test -Pe2e-tests` (requires Docker for database)

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

## Audit/User Context Feature

MSG supports optional audit tracking for INSERT and UPDATE operations, allowing generated APIs to capture user context from HTTP headers.

### CLI Options
| Option | Description | Default |
|--------|-------------|---------|
| `--enable-audit` | Enable audit/user context support | `false` |
| `--user-header` | HTTP header name for user identification | `X-User-ID` |
| `--audit-columns` | Comma-separated list of audit column names | Required when audit enabled |

### How It Works
1. **Controller**: Adds `@RequestHeader` parameter to capture user ID from HTTP header
2. **DAO**: Maps audit columns from `userId` parameter instead of DTO
3. **DTO**: Excludes audit columns (they come from header, not request body)

### Example Usage
```bash
# INSERT with audit - created_by populated from X-User-ID header
mvn exec:java -Dexec.mainClass="com.jfeatures.msg.codegen.MicroServiceGenerator" \
  -Dexec.args="--name Customer --destination ./output --sql-file customer_insert.sql \
               --enable-audit --audit-columns created_by"

# UPDATE with custom header - updated_by populated from Authorization header
mvn exec:java -Dexec.mainClass="com.jfeatures.msg.codegen.MicroServiceGenerator" \
  -Dexec.args="--name Customer --destination ./output --sql-file customer_update.sql \
               --enable-audit --user-header Authorization --audit-columns updated_by,modified_by"
```

### Generated Code Example
```java
// Controller with audit
@PostMapping
public ResponseEntity<String> insertCustomer(
        @Valid @RequestBody CustomerInsertDTO insertRequest,
        @RequestHeader(value = "X-User-ID", required = true) String userId) {
    int rowsAffected = customerInsertDAO.insertCustomer(insertRequest, userId);
    return ResponseEntity.ok("Inserted " + rowsAffected + " row(s)");
}

// DAO with audit column mapping
public int insertCustomer(CustomerInsertDTO insertRequest, String userId) {
    Map<String, Object> sqlParamMap = new HashMap<>();
    sqlParamMap.put("customerName", insertRequest.getCustomerName());
    sqlParamMap.put("createdBy", userId);  // Audit column from userId
    return namedParameterJdbcTemplate.update(INSERT_SQL, sqlParamMap);
}
```

### Key Files for Audit Feature
- **Configuration**: `src/main/java/com/jfeatures/msg/codegen/domain/AuditConfiguration.java`
- **Constants**: `src/main/java/com/jfeatures/msg/codegen/constants/ProjectConstants.java`
- **Insert Flow**: `GenerateInsertController.java`, `GenerateInsertDAO.java`, `InsertMicroserviceGenerator.java`
- **Update Flow**: `GenerateUpdateController.java`, `GenerateUpdateDAO.java`, `UpdateMicroserviceGenerator.java`

## Important Context
- **Database**: SQL Server with hardcoded sakila database
- **Java Version**: Java 21 with text blocks and records
- **Type Mapping**: `SQLServerDataTypeEnum` for JDBC → Java conversion
- **Generated Structure**: Standard Maven layout with Spring Boot conventions

