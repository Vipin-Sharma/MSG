# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

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