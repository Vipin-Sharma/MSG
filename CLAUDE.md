# CLAUDE.md

This file provides comprehensive guidance to Claude Code (claude.ai/code) when working with the MSG (Microservice Generator) repository.

## Project Overview

MSG is an enterprise-grade Java 21 application that transforms SQL statements into complete Spring Boot microservices. It uses a sophisticated metadata-driven approach to generate production-ready code with proper validation, logging, and REST API endpoints.

**Key Capabilities:**
- Converts SELECT/INSERT/UPDATE/DELETE SQL to Spring Boot microservices
- Generates DTOs, DAOs, Controllers, and configuration classes
- Uses JDBC metadata extraction (not SQL parsing) for reliability
- Produces clean, maintainable Java code following enterprise standards
- Supports complex SQL queries including JOINs, subqueries, and CTEs

## Commands

### Build and Compilation
```bash
# Clean and compile the project
mvn clean compile

# Build the project
mvn clean install

# Package as executable JAR
mvn clean package
```

### Testing
```bash
# Run all tests
mvn test

# Run integration tests
mvn integration-test

# Run specific test class
mvn test -Dtest=ClassName

# Generate test coverage report
mvn jacoco:report
```

### Code Generation (Main Functionality)
```bash
# Generate microservice from SQL - main entry point
mvn exec:java -Dexec.mainClass="com.jfeatures.msg.codegen.MicroServiceGenerator" \
  -Dexec.args="--name Customer --destination ./output --sql-file sample_parameterized_sql.sql"

# Generate different CRUD operations
mvn exec:java -Dexec.mainClass="com.jfeatures.msg.codegen.MicroServiceGenerator" \
  -Dexec.args="--name Customer --destination ./output --sql-file sample_insert_parameterized.sql"

mvn exec:java -Dexec.mainClass="com.jfeatures.msg.codegen.MicroServiceGenerator" \
  -Dexec.args="--name Customer --destination ./output --sql-file sample_update_parameterized.sql"

mvn exec:java -Dexec.mainClass="com.jfeatures.msg.codegen.MicroServiceGenerator" \
  -Dexec.args="--name Customer --destination ./output --sql-file sample_delete_parameterized.sql"

# Use default behavior (tries files in priority order)
mvn exec:java -Dexec.mainClass="com.jfeatures.msg.codegen.MicroServiceGenerator" \
  -Dexec.args="--name Customer --destination ./output"
```

### Debugging
```bash
# Enable verbose logging
mvn -X exec:java -Dexec.mainClass="com.jfeatures.msg.codegen.MicroServiceGenerator" \
  -Dexec.args="--name Customer --destination ./output"
```

## Architecture Overview

MSG (Microservice Generator) transforms SQL statements into complete Spring Boot microservices using a metadata-driven approach. The system follows clean architecture principles with single responsibility per class.

### Core Architecture

**Main Orchestrator**: `MicroServiceGenerator` - CLI entry point that coordinates the entire generation process

**SQL Processing Layer**:
- `SqlFileResolver` - Locates SQL files with intelligent fallback logic
- `SqlStatementDetector` - Identifies SQL statement type (SELECT/INSERT/UPDATE/DELETE)

**Operation-Specific Generators** (each handles one CRUD operation):
- `SelectMicroserviceGenerator` - Handles SELECT statements → GET APIs
- `InsertMicroserviceGenerator` - Handles INSERT statements → POST APIs  
- `UpdateMicroserviceGenerator` - Handles UPDATE statements → PUT APIs
- `DeleteMicroserviceGenerator` - Handles DELETE statements → DELETE APIs

**Metadata Extraction Layer**:
- `SqlMetadata` - Extracts SELECT result set metadata via JDBC
- `UpdateMetadataExtractor` - Analyzes UPDATE SET/WHERE clauses via database metadata
- `InsertMetadataExtractor` - Extracts INSERT column info via database metadata
- `DeleteMetadataExtractor` - Analyzes DELETE WHERE conditions (legacy)
- `ParameterMetadataExtractor` - Extracts SQL parameters via JDBC (used by DELETE)

**Code Generation Layer** (uses JavaPoet):
- DTO Generators: `GenerateDTO`, `GenerateInsertDTO`, `GenerateUpdateDTO`, `GenerateDeleteDTO`
- Controller Generators: `GenerateController`, `GenerateInsertController`, `GenerateUpdateController`, `GenerateDeleteController`  
- DAO Generators: `GenerateDAO`, `GenerateInsertDAO`, `GenerateUpdateDAO`, `GenerateDeleteDAO`
- Config Generators: `GenerateSpringBootApp`, `GenerateDatabaseConfig`

**File System Layer**:
- `MicroserviceProjectWriter` - Orchestrates complete project writing
- `ProjectDirectoryBuilder` - Creates Maven-standard directory structure
- `MicroserviceDirectoryCleaner` - Safely cleans target directories

### Key Design Patterns

1. **Single Responsibility**: Each class has exactly one public method focused on single responsibility
2. **Orchestration Pattern**: Main generators coordinate multiple specialized components  
3. **Metadata-Driven**: Uses database metadata and JDBC parameter extraction instead of SQL parsing
4. **Convention Over Configuration**: Follows Spring Boot and REST conventions

### Generated Project Structure

```
generated-microservice/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/com/jfeatures/msg/{domain}/
│   │   │   ├── Application.java
│   │   │   ├── controller/{Domain}Controller.java
│   │   │   ├── dao/{Domain}DAO.java
│   │   │   ├── dto/{Domain}DTO.java
│   │   │   └── config/DatabaseConfig.java
│   │   └── resources/application.properties
│   └── test/java/com/jfeatures/
```

### Technology Stack

- Java 21 with text blocks and modern features
- Spring Boot 2.7.x framework
- Spring Data JDBC for database operations
- JavaPoet for type-safe code generation
- Lombok for reducing boilerplate
- Jakarta Validation for input validation
- OpenAPI/Swagger for API documentation
- Picocli for CLI interface
- SQL Server JDBC driver (primary database)

### SQL File Requirements

SQL files must be placed in `src/main/resources/` with specific naming patterns:
- `sample_parameterized_sql.sql` (SELECT statements)
- `sample_insert_parameterized.sql` (INSERT statements)  
- `sample_update_parameterized.sql` (UPDATE statements)
- `sample_delete_parameterized.sql` (DELETE statements)

Parameters must use `?` placeholders for proper metadata extraction.

### Fallback Logic

When no `--sql-file` specified, tries files in priority order:
1. UPDATE (`sample_update_parameterized.sql`)
2. INSERT (`sample_insert_parameterized.sql`)
3. DELETE (`sample_delete_parameterized.sql`)
4. SELECT (`sample_parameterized_sql.sql`)

### Database Metadata Strategy

- **SELECT/UPDATE/INSERT**: Database metadata extraction for reliable type mapping
- **DELETE**: JDBC parameter extraction for parameter handling
- Uses `SQLServerDataTypeEnum` for JDBC → Java type mapping
- Supports complex queries with JOINs, subqueries, and CTEs

### Testing Generated Services

After generation, test the microservice:
```bash
cd ./output  # Generated microservice directory
mvn spring-boot:run

# Test endpoints
curl -X GET "http://localhost:8080/api/customer?param=value"
curl -X POST "http://localhost:8080/api/customer" -H "Content-Type: application/json" -d '{...}'
```

## Detailed Component Analysis

### Core Data Structures

**ColumnMetadata** (`src/main/java/com/jfeatures/msg/codegen/dbmetadata/ColumnMetadata.java`):
- Contains complete JDBC metadata for database columns
- Properties: columnName, columnType, columnTypeName, precision, scale, isNullable, etc.
- Used by all generators to create properly typed Java code

**Metadata Records** (package `com.jfeatures.msg.codegen.dbmetadata`):
- `InsertMetadata(tableName, insertColumns, originalSql)` - INSERT operation metadata
- `UpdateMetadata(tableName, setColumns, whereColumns, originalSql)` - UPDATE operation metadata  
- `DeleteMetadata(tableName, parameters, originalSql)` - DELETE operation metadata
- All are Java records (immutable data carriers)

### Code Generation Pipeline

1. **SQL File Resolution** (`SqlFileResolver`):
   - Locates SQL files in `src/main/resources/`
   - Implements fallback logic: UPDATE → INSERT → DELETE → SELECT
   - Handles both explicit file specification and default behavior

2. **Statement Detection** (`SqlStatementDetector`):
   - Uses regex patterns to identify SQL operation type
   - Patterns: `"^\s*SELECT"`, `"^\s*INSERT"`, `"^\s*UPDATE"`, `"^\s*DELETE"`
   - Case-insensitive matching with whitespace tolerance

3. **Metadata Extraction**:
   - **SELECT**: `SqlMetadata` extracts ResultSet metadata via JDBC execution
   - **INSERT**: `InsertMetadataExtractor` uses DatabaseMetaData to get column info
   - **UPDATE**: `UpdateMetadataExtractor` analyzes SET/WHERE clauses via database metadata
   - **DELETE**: `ParameterMetadataExtractor` uses JDBC parameter metadata

4. **Code Generation** (JavaPoet-based):
   - **DTO Generation**: Creates request/response classes with proper validation annotations
   - **DAO Generation**: Creates data access objects with NamedParameterJdbcTemplate
   - **Controller Generation**: Creates REST controllers with proper HTTP mappings
   - **Configuration**: Generates Spring Boot application class and database config

### Critical Architecture Decisions

**Metadata-Driven Approach**: Instead of parsing SQL (complex and error-prone), MSG executes SQL against database metadata to extract type information. This provides:
- Accurate type mapping (JDBC types → Java types)
- Support for complex SQL without parsing complexity
- Reliable parameter extraction
- Database-specific type handling

**Single Responsibility Classes**: Every generator class has exactly one public static method:
- `GenerateInsertDAO.createInsertDAO(businessName, metadata)`
- `GenerateUpdateDAO.createUpdateDAO(businessName, metadata)`
- Clean, testable, and maintainable design

**JavaPoet Type Safety**: All code generation uses JavaPoet for:
- Type-safe code construction
- Proper import management
- Consistent formatting
- Compile-time validation of generated structures

### Package Structure & Responsibilities

```
com.jfeatures.msg/
├── Application.java                 // Main Spring Boot entry point
├── codegen/                        // Core code generation
│   ├── MicroServiceGenerator.java  // CLI orchestrator  
│   ├── Generate*.java             // Code generators (DAO, DTO, Controller)
│   ├── constants/                 // Project constants and enums
│   ├── database/                  // Database connection handling
│   ├── dbmetadata/               // Metadata extraction and records
│   ├── domain/                   // Domain objects
│   ├── filesystem/               // File system operations
│   ├── generator/                // High-level generators
│   ├── jdbc/                     // JDBC utilities
│   ├── mapping/                  // Result set mapping
│   ├── sql/                      // SQL processing
│   └── util/                     // Utility classes
├── config/                       // Spring configuration
├── controller/                   // Application controllers
└── sql/                         // SQL file utilities
```

### Database Connection & Configuration

**Current Implementation**: 
- Uses hardcoded SQL Server connection in `DataSourceConfig.java`
- Credentials: sa/Password@1 (⚠️ SECURITY RISK - should be externalized)
- Database: sakila on localhost:1433
- Connection string includes `encrypt=true;trustServerCertificate=true`

**Connection Management**:
- `DatabaseConnectionFactory` creates connection objects
- Returns `DatabaseConnection` record with DataSource, JdbcTemplate, NamedParameterJdbcTemplate
- No connection pooling implemented (potential performance issue)

### Type Mapping System

**SQLServerDataTypeEnum** (`src/main/java/com/jfeatures/msg/codegen/SQLServerDataTypeEnum.java`):
- Maps SQL Server types to Java types
- Examples: VARCHAR → String, INTEGER → Integer, DATETIME → LocalDateTime
- Handles nullable types appropriately
- Provides default mapping for unknown types

**Naming Conventions**:
- **Java Classes**: PascalCase (CustomerInsertDAO, CustomerUpdateDTO)
- **Java Fields**: camelCase (customerId, customerName)
- **SQL Parameters**: camelCase in maps (customerId) but original names in SQL (customer_id)
- **Packages**: lowercase with business name (com.jfeatures.msg.customer.dao)

### Testing Strategy & Current Coverage

**Current State**: 89% test coverage with 531 tests
**Test Categories**:
- Unit tests for individual generators and utilities
- Integration tests for complete workflows
- Metadata extraction tests with mock databases
- File system operation tests
- Configuration tests

**Recent Improvements**:
- Added comprehensive tests for GenerateInsertDAO and GenerateUpdateDAO
- Fixed critical Java naming convention bugs
- Reality-based testing approach (observe actual behavior, then test)

## Security Considerations & Known Issues

### Critical Security Issues (IMMEDIATE ATTENTION REQUIRED)

1. **Hardcoded Database Credentials** (CRITICAL):
   - File: `src/main/java/com/jfeatures/msg/config/DataSourceConfig.java`
   - Issue: Database username/password exposed in source code
   - Fix: Use environment variables or encrypted configuration

2. **Resource Leak** (CRITICAL):
   - File: `src/main/java/com/jfeatures/msg/sql/ReadFileFromResources.java`
   - Issue: Potential NPE if resource file not found
   - Fix: Add null check before calling `readAllBytes()`

3. **SQL Injection Risk** (HIGH):
   - File: `src/main/java/com/jfeatures/msg/codegen/dbmetadata/SqlMetadata.java`
   - Issue: Direct query execution without validation
   - Fix: Use parameterized queries and input validation

4. **Directory Traversal** (HIGH):
   - File: `src/main/java/com/jfeatures/msg/codegen/filesystem/MicroserviceDirectoryCleaner.java`
   - Issue: No path validation for destination directories
   - Fix: Add path sanitization and sandboxing

### Performance Issues

1. **Inefficient ResultSet Processing**:
   - `SqlMetadata.java` processes metadata in row mapper (should be once only)
   - Returns null from row mapper (logic error)

2. **No Connection Pooling**:
   - Multiple database connections created without pooling
   - Risk of connection exhaustion under load

### Code Quality Issues

1. **Platform-Specific Paths**: Hardcoded Linux paths in ProjectConstants
2. **Generic Exception Handling**: Loss of error context in exception wrapping
3. **Magic Strings/Numbers**: Scattered throughout codebase
4. **Commented Debug Code**: Indicates potential unresolved issues

## Development Guidelines

### When Adding New Generators:
1. Follow single responsibility principle (one public static method)
2. Use JavaPoet for type-safe code generation
3. Implement proper error handling with specific exception types
4. Add comprehensive test coverage with reality-based testing
5. Validate all inputs thoroughly
6. Use proper naming conventions (camelCase parameters, PascalCase classes)

### When Modifying Metadata Extraction:
1. Ensure JDBC resources are properly closed
2. Handle null results gracefully
3. Add appropriate logging for debugging
4. Test with various SQL statement types
5. Validate type mappings are accurate

### When Working with File System:
1. Validate all file paths for security
2. Use try-with-resources for file operations
3. Handle file system exceptions appropriately
4. Ensure cross-platform compatibility
5. Add comprehensive logging

### Testing Best Practices:
1. Use reality-based testing (observe actual behavior first)
2. Create small research programs to understand code generation
3. Test error conditions and edge cases
4. Ensure 100% pass rate before committing
5. Add meaningful assertions, not just coverage

## Known Bugs & Issues

### Recently Fixed:
- ✅ Java naming convention violation in GenerateInsertDAO (customer_id → customerId)
- ✅ Missing delimiter in CaseUtils.toCamelCase() calls
- ✅ Added comprehensive test coverage for core DAO generators

### Pending Fixes:
- ❌ Hardcoded database credentials (CRITICAL)
- ❌ Resource leak in file reading (CRITICAL)  
- ❌ SQL injection risks (HIGH)
- ❌ Directory traversal vulnerability (HIGH)
- ❌ Inefficient ResultSet processing (MEDIUM)
- ❌ No connection pooling (MEDIUM)
- ❌ Platform-specific paths (LOW)

### Technical Debt:
- Generic exception handling patterns
- Scattered magic strings and numbers
- Commented debug code
- Inconsistent logging patterns