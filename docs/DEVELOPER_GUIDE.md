# MSG Developer Guide

Architecture, design principles, and contribution guidelines for MSG (Microservice Generator).

## Table of Contents

- [Project Philosophy & Design Principles](#project-philosophy--design-principles)
- [System Architecture](#system-architecture)
- [Development Environment Setup](#development-environment-setup)
- [Code Generation Patterns](#code-generation-patterns)
- [Development Workflow](#development-workflow)
- [Testing Strategy](#testing-strategy)
- [End-to-End Testing](#end-to-end-testing)
- [Contributing Guidelines](#contributing-guidelines)
- [Extending MSG](#extending-msg)

## Project Philosophy & Design Principles

### Core Philosophy: "From SQL to Service"
MSG eliminates microservices boilerplate by transforming SQL statements into complete Spring Boot applications through metadata-driven generation.

### Design Principles

**1. Single Responsibility Principle**
Every class has exactly one public method, focused on a single responsibility.
```java
// ✅ Good: Single responsibility
public class GenerateInsertDAO {
    public static TypeSpec createInsertDAO(String businessName, InsertMetadata metadata) {
        // Implementation
    }
}

// ❌ Bad: Multiple responsibilities
public class DAOGenerator {
    public TypeSpec createInsertDAO(...) { }
    public TypeSpec createUpdateDAO(...) { }
    public TypeSpec createSelectDAO(...) { }
}
```

**2. Clean Code Architecture**
Following SOLID, DRY, and YAGNI principles with:
- Self-documenting class and method names
- Orchestration pattern for complex workflows
- Value objects for data encapsulation
- Immutable data structures (Java records)

**3. Metadata-Driven Generation**
Uses database metadata and JDBC parameter extraction instead of complex SQL parsing for reliability and accuracy.

**4. Convention Over Configuration**
Follows Spring Boot and REST API conventions for predictable, maintainable output.

## System Architecture

### High-Level Architecture

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────────┐
│   SQL File      │───▶│ MicroService     │───▶│   Generated         │
│   (Input)       │    │ Generator        │    │   Microservice      │
│                 │    │ (Orchestrator)   │    │   (Spring Boot)     │
└─────────────────┘    └──────────────────┘    └─────────────────────┘
```

### Component Architecture

#### 1. Main Orchestrator Layer
```java
MicroServiceGenerator (CLI Entry Point)
├── SqlFileResolver (File Location & Reading)
├── SqlStatementDetector (Statement Type Detection)  
└── Operation-Specific Generators
```

**Key Classes:**
- `MicroServiceGenerator.java` - Main CLI orchestrator
- `SqlFileResolver.java` - Intelligent file resolution with fallback
- `SqlStatementDetector.java` - SQL type detection via regex

#### 2. Operation-Specific Generator Layer

Each CRUD operation has its own specialized generator:

```
SelectMicroserviceGenerator     ┌─→ GenerateDTO
InsertMicroserviceGenerator ────┼─→ GenerateController  
UpdateMicroserviceGenerator     │  GenerateDAO
DeleteMicroserviceGenerator     └─→ GenerateSpringBootApp
```

**Files:**
- `src/main/java/com/jfeatures/msg/codegen/generator/SelectMicroserviceGenerator.java`
- `src/main/java/com/jfeatures/msg/codegen/generator/InsertMicroserviceGenerator.java`
- `src/main/java/com/jfeatures/msg/codegen/generator/UpdateMicroserviceGenerator.java`
- `src/main/java/com/jfeatures/msg/codegen/generator/DeleteMicroserviceGenerator.java`

#### 3. Metadata Extraction Layer

**Database Metadata Strategy:**
- **SELECT**: `SqlMetadata` - Executes query to extract ResultSet metadata
- **INSERT**: `InsertMetadataExtractor` - Uses DatabaseMetaData for column info
- **UPDATE**: `UpdateMetadataExtractor` - Analyzes SET/WHERE clauses via database metadata
- **DELETE**: `ParameterMetadataExtractor` - Uses JDBC parameter metadata

**Key Files:**
- `src/main/java/com/jfeatures/msg/codegen/dbmetadata/SqlMetadata.java`
- `src/main/java/com/jfeatures/msg/codegen/dbmetadata/InsertMetadataExtractor.java`
- `src/main/java/com/jfeatures/msg/codegen/dbmetadata/UpdateMetadataExtractor.java`
- `src/main/java/com/jfeatures/msg/codegen/dbmetadata/ParameterMetadataExtractor.java`

**Metadata Records:**
```java
// Immutable data carriers
public record InsertMetadata(String tableName, List<ColumnMetadata> insertColumns, String originalSql) {}
public record UpdateMetadata(String tableName, List<ColumnMetadata> setColumns, List<ColumnMetadata> whereColumns, String originalSql) {}
public record DeleteMetadata(String tableName, List<DBColumn> parameters, String originalSql) {}
```

#### 4. Code Generation Layer

**DTO Generators:**
- `GenerateDTO.java` (SELECT response DTOs)
- `GenerateInsertDTO.java` (INSERT request DTOs)
- `GenerateUpdateDTO.java` (UPDATE request DTOs)
- `GenerateDeleteDTO.java` (DELETE request DTOs)

**Controller Generators:**
- `GenerateController.java` (GET endpoints)
- `GenerateInsertController.java` (POST endpoints)
- `GenerateUpdateController.java` (PUT endpoints)
- `GenerateDeleteController.java` (DELETE endpoints)

**DAO Generators:**
- `GenerateDAO.java` (SELECT data access)
- `GenerateInsertDAO.java` (INSERT data access)
- `GenerateUpdateDAO.java` (UPDATE data access)
- `GenerateDeleteDAO.java` (DELETE data access)

**Configuration Generators:**
- `GenerateSpringBootApp.java` (Application main class)
- `GenerateDatabaseConfig.java` (Database configuration)

#### 5. File System Layer

**Project Structure Management:**
- `MicroserviceProjectWriter.java` - Orchestrates complete project writing
- `ProjectDirectoryBuilder.java` - Creates Maven-standard directory structure
- `MicroserviceDirectoryCleaner.java` - Safely cleans target directories

### Package Structure

```
com.jfeatures.msg.codegen/
├── MicroServiceGenerator.java           # Main CLI entry point
├── Generate*.java                       # Code generators (DAO, DTO, Controller)
├── constants/                          # Project constants and enums
│   ├── ProjectConstants.java
│   └── SQLServerDataTypeEnum.java
├── database/                           # Database connection handling  
│   ├── DataSourceConfig.java
│   └── DatabaseConnectionFactory.java
├── dbmetadata/                        # Metadata extraction and records
│   ├── ColumnMetadata.java
│   ├── InsertMetadata.java
│   ├── UpdateMetadata.java
│   ├── DeleteMetadata.java
│   ├── SqlMetadata.java
│   ├── InsertMetadataExtractor.java
│   ├── UpdateMetadataExtractor.java
│   └── ParameterMetadataExtractor.java
├── domain/                            # Domain objects
│   └── GeneratedMicroservice.java
├── filesystem/                        # File system operations
│   ├── MicroserviceProjectWriter.java
│   ├── ProjectDirectoryBuilder.java
│   └── MicroserviceDirectoryCleaner.java
├── generator/                         # High-level generators
│   ├── SelectMicroserviceGenerator.java
│   ├── InsertMicroserviceGenerator.java
│   ├── UpdateMicroserviceGenerator.java
│   └── DeleteMicroserviceGenerator.java
├── jdbc/                             # JDBC utilities
│   └── JdbcTemplateFactory.java
├── mapping/                          # Result set mapping
│   └── ResultSetToColumnMetadataMapper.java
├── sql/                              # SQL processing
│   ├── SqlFileResolver.java
│   ├── SqlStatementDetector.java
│   └── ReadFileFromResources.java
└── util/                             # Utility classes
    └── CaseUtils.java
```

### Technology Stack

- **Java 21**: Modern Java features including text blocks and records
- **Spring Boot 3.x**: Latest Spring Boot framework
- **JavaPoet**: Type-safe code generation library
- **Lombok**: Reduces boilerplate code
- **Jakarta Validation**: Input validation annotations
- **OpenAPI/Swagger**: API documentation generation
- **Picocli**: Command-line interface framework
- **Maven**: Build and dependency management
- **JUnit 5**: Testing framework
- **SQL Server JDBC**: Database connectivity

## Development Environment Setup

### Prerequisites
- Java 21 or later
- Maven 3.8+
- IDE with Lombok plugin (IntelliJ IDEA, Eclipse, VS Code)
- SQL Server database for testing

### Setup Steps

```bash
# 1. Clone repository
git clone <repository-url>
cd MSG

# 2. Install dependencies
mvn clean install

# 3. Run tests to verify setup
mvn test

# 4. Compile project
mvn clean compile

# 5. Setup database (for testing)
docker-compose up -d --build
```

### IDE Configuration

**IntelliJ IDEA:**
- Install Lombok plugin
- Enable annotation processing
- Set Java 21 as project SDK
- Configure Maven auto-import

**VS Code:**
- Install Extension Pack for Java
- Install Lombok Annotations Support
- Configure Java 21 runtime

**Eclipse:**
- Install Lombok (download lombok.jar, run installer)
- Set Java 21 as project JRE
- Enable Maven nature

## Code Generation Patterns

### JavaPoet Usage Examples

**Field Generation with Annotations:**
```java
FieldSpec fieldSpec = FieldSpec.builder(String.class, "firstName")
    .addModifiers(Modifier.PRIVATE)
    .addAnnotation(AnnotationSpec.builder(NotNull.class)
        .addMember("message", "$S", "firstName is required for customer creation")
        .build())
    .build();
```

**Method Generation with Parameters:**
```java
MethodSpec methodSpec = MethodSpec.methodBuilder("insertCustomer")
    .addModifiers(Modifier.PUBLIC)
    .returns(int.class)
    .addParameter(ParameterSpec.builder(CustomerInsertDTO.class, "request")
        .build())
    .addStatement("Map<String, Object> params = new HashMap<>()")
    .addStatement("params.put($S, request.getFirstName())", "firstName")
    .addStatement("return namedParameterJdbcTemplate.update(SQL, params)")
    .build();
```

**Class Generation with Text Block SQL:**
```java
TypeSpec daoClass = TypeSpec.classBuilder(className)
    .addModifiers(Modifier.PUBLIC)
    .addAnnotation(Component.class)
    .addField(FieldSpec.builder(String.class, "SQL")
        .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
        .initializer("$S", sqlTextBlock)
        .build())
    .addMethod(insertMethod)
    .build();
```

### Database Type Mapping

MSG uses `SQLServerDataTypeEnum` for database-to-Java type mapping:

```java
public enum SQLServerDataTypeEnum {
    INTEGER("int", Integer.class),
    VARCHAR("nvarchar", String.class),
    CHAR("char", String.class),
    TIMESTAMP("datetime2", Timestamp.class),
    DATE("date", Date.class),
    DECIMAL("decimal", BigDecimal.class);
    
    private final String sqlServerType;
    private final Class<?> javaType;
}
```

**Usage in Code Generation:**
```java
// Convert JDBC type to Java class
SQLServerDataTypeEnum dataType = SQLServerDataTypeEnum.fromJdbcType(jdbcType);
Class<?> javaType = dataType.getJavaType();

// Generate field with proper type
FieldSpec field = FieldSpec.builder(javaType, fieldName)
    .addModifiers(Modifier.PRIVATE)
    .build();
```

### Naming Conventions

**Java Classes**: PascalCase
- `CustomerInsertDAO`, `CustomerUpdateDTO`, `CustomerController`

**Java Fields**: camelCase
- `customerId`, `firstName`, `lastUpdate`

**SQL Parameters**: camelCase in maps, but preserve original names in SQL
```java
// Java parameter map
params.put("customerId", request.getCustomerId());
params.put("firstName", request.getFirstName());

// SQL preserves original column names
String sql = """
    INSERT INTO customer (customer_id, first_name) 
    VALUES (:customerId, :firstName)
    """;
```

**Package Structure**: lowercase with business domain
- `com.jfeatures.msg.customer.dao`
- `com.jfeatures.msg.customer.controller`

## Development Workflow

### Adding New SQL Statement Types

To add support for a new SQL statement type (e.g., MERGE, UPSERT):

**1. Create Metadata Extractor:**
```java
package com.jfeatures.msg.codegen.dbmetadata;

public class MergeMetadataExtractor {
    public MergeMetadata extractMetadata(String sql, DatabaseConnection connection) throws Exception {
        // 1. Parse MERGE statement structure
        // 2. Extract target table, source data, match conditions
        // 3. Determine insert/update column mappings
        // 4. Return metadata record
    }
}
```

**2. Create Metadata Value Object:**
```java
public record MergeMetadata(
    String targetTable,
    String sourceTable, 
    List<ColumnMetadata> matchColumns,
    List<ColumnMetadata> insertColumns,
    List<ColumnMetadata> updateColumns,
    String originalSql
) {}
```

**3. Create Code Generators:**
```java
// DTO for MERGE request body
public class GenerateMergeDTO {
    public static TypeSpec createMergeDTO(String businessName, MergeMetadata metadata) {
        // Generate DTO with match criteria and data fields
    }
}

// Controller for MERGE endpoint  
public class GenerateMergeController {
    public static TypeSpec createMergeController(String businessName, MergeMetadata metadata) {
        // Generate POST endpoint with MERGE semantics
    }
}

// DAO for MERGE operation
public class GenerateMergeDAO {
    public static TypeSpec createMergeDAO(String businessName, MergeMetadata metadata) {
        // Generate MERGE SQL with text blocks
    }
}
```

**4. Create Orchestrator:**
```java
package com.jfeatures.msg.codegen.generator;

public class MergeMicroserviceGenerator {
    public GeneratedMicroservice generateMicroservice(
        String businessName, 
        String destinationPath, 
        String sqlContent
    ) throws Exception {
        // 1. Extract metadata
        MergeMetadata metadata = extractor.extractMetadata(sqlContent, connection);
        
        // 2. Generate components
        TypeSpec dto = GenerateMergeDTO.createMergeDTO(businessName, metadata);
        TypeSpec controller = GenerateMergeController.createMergeController(businessName, metadata);
        TypeSpec dao = GenerateMergeDAO.createMergeDAO(businessName, metadata);
        
        // 3. Return complete microservice
        return GeneratedMicroservice.builder()
            .dto(dto)
            .controller(controller)
            .dao(dao)
            .build();
    }
}
```

**5. Update Main Generator:**
```java
// In MicroServiceGenerator.java
switch (statementType) {
    case "SELECT" -> new SelectMicroserviceGenerator().generateMicroservice(...);
    case "INSERT" -> new InsertMicroserviceGenerator().generateMicroservice(...);
    case "UPDATE" -> new UpdateMicroserviceGenerator().generateMicroservice(...);
    case "DELETE" -> new DeleteMicroserviceGenerator().generateMicroservice(...);
    case "MERGE" -> new MergeMicroserviceGenerator().generateMicroservice(...);  // Add this
    default -> throw new IllegalArgumentException("Unsupported SQL statement type: " + statementType);
}
```

**6. Add SQL File Constants:**
```java
// In SqlFileResolver.java
private static final String MERGE_SQL_FILE = "sample_merge_parameterized.sql";

// Add to fallback order
private static final List<String> SQL_FILE_FALLBACK_ORDER = Arrays.asList(
    UPDATE_SQL_FILE,
    INSERT_SQL_FILE, 
    DELETE_SQL_FILE,
    MERGE_SQL_FILE,  // Add here
    SELECT_SQL_FILE
);
```

### Parameter Extraction Strategy

**For Database Metadata Extraction (INSERT/UPDATE):**
```java
DatabaseMetaData metaData = connection.getConnection().getMetaData();
ResultSet columns = metaData.getColumns(null, null, tableName, null);

List<ColumnMetadata> columnList = new ArrayList<>();
while (columns.next()) {
    ColumnMetadata column = ColumnMetadata.builder()
        .columnName(columns.getString("COLUMN_NAME"))
        .columnType(columns.getInt("DATA_TYPE"))
        .columnTypeName(columns.getString("TYPE_NAME"))
        .precision(columns.getInt("COLUMN_SIZE"))
        .scale(columns.getInt("DECIMAL_DIGITS"))
        .isNullable(columns.getInt("NULLABLE") == DatabaseMetaData.columnNullable)
        .build();
    columnList.add(column);
}
```

**For JDBC Parameter Extraction (DELETE):**
```java
List<DBColumn> parameters = parameterExtractor.extractParameters(sql, connection);
// Returns parameter metadata with names and types
```

## Testing Strategy

### Test Coverage Requirements
- **Unit Tests**: All generators and utilities
- **Integration Tests**: Complete generation workflows
- **Reality-Based Testing**: Verify generated code compiles and runs

### Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=GenerateInsertDAOTest

# Run integration tests
mvn integration-test

# Generate coverage report
mvn jacoco:report
```

### Test Categories

**Unit Tests** (`src/test/java`):
- Individual generator classes
- Metadata extractors
- Utility classes
- Type mapping

**Integration Tests**:
- Complete generation workflows
- Database metadata extraction
- File system operations

**Reality-Based Testing Pattern**:
```java
@Test
void testGenerateInsertDAO_CreatesValidJavaCode() {
    // 1. Create test metadata
    InsertMetadata metadata = createTestInsertMetadata();
    
    // 2. Generate code
    TypeSpec daoClass = GenerateInsertDAO.createInsertDAO("Customer", metadata);
    
    // 3. Verify structure
    assertThat(daoClass.name).isEqualTo("CustomerInsertDAO");
    assertThat(daoClass.annotations).contains(Component.class);
    
    // 4. Verify generated code compiles (reality test)
    JavaFile javaFile = JavaFile.builder("com.test", daoClass).build();
    String generatedCode = javaFile.toString();
    
    // 5. Compile and verify
    CompilationResult result = compile(generatedCode);
    assertThat(result.isSuccess()).isTrue();
}
```

### Mock Database Testing

```java
@ExtendWith(MockitoExtension.class)
class InsertMetadataExtractorTest {
    
    @Mock
    private DatabaseConnection mockConnection;
    
    @Mock 
    private DatabaseMetaData mockMetaData;
    
    @Test
    void testExtractMetadata_ValidInsertSQL() throws Exception {
        // Setup mocks
        when(mockConnection.getConnection().getMetaData()).thenReturn(mockMetaData);
        when(mockMetaData.getColumns(any(), any(), eq("customer"), any()))
            .thenReturn(createMockResultSet());
            
        // Test extraction
        InsertMetadataExtractor extractor = new InsertMetadataExtractor();
        InsertMetadata metadata = extractor.extractMetadata(sql, mockConnection);
        
        // Verify results
        assertThat(metadata.tableName()).isEqualTo("customer");
        assertThat(metadata.insertColumns()).hasSize(3);
    }
}
```

## End-to-End Testing

### Overview

MSG includes a comprehensive End-to-End testing framework that validates the complete CRUD API generation workflow from SQL files to fully functional Spring Boot microservices. This ensures that all README commands work correctly and generate production-ready code.

### E2E Test Architecture

The E2E testing framework consists of three main components:

```
┌─────────────────────┐    ┌──────────────────────┐    ┌─────────────────────┐
│  E2E Test Classes   │───▶│  Code Generation     │───▶│  Validation &       │
│  - WorkingE2EGen    │    │  Orchestration       │    │  Verification       │
│  - EndToEndCrud     │    │  - SQL Files         │    │  - Structure Check  │
│  - ApiEndpointTester│    │  - CLI Commands      │    │  - Compilation Test │
└─────────────────────┘    └──────────────────────┘    └─────────────────────┘
```

### E2E Test Classes

**1. CompleteCrudGenerationE2ETest** (Primary E2E Test)
- Tests all 4 CRUD operations without Docker dependencies
- Validates complete generation workflow
- Runs quickly and reliably in any environment (~5 seconds)
- ✅ **STABLE** - Primary E2E test suite

**2. SqlStatementDetectionAndCrudGenerationE2ETest** (SQL Detection Test)
- Ultra-fast validation of SQL statement type detection
- Tests SQL parsing and validation for different structures
- No external dependencies (~0.2 seconds)
- ✅ **STABLE** - SQL parsing validation

**3. FullStackCrudGenerationWithDatabaseE2ETest** (Full Integration Test)
- Uses Testcontainers for real database testing
- Tests REST API endpoints with actual HTTP requests
- Requires Docker for execution (~5-10 minutes)
- 🟡 **MOSTLY STABLE** - 5/6 tests pass, REST API integration may occasionally fail

**4. GeneratedMicroserviceValidator** (Code Quality Validator)
- Validates Maven project structure
- Checks Java class generation and annotations
- Verifies Spring Boot configuration files

**5. ApiEndpointTester** (REST API Tester)
- Tests generated REST endpoints with HTTP requests
- Validates request/response handling
- Checks microservice startup and health

### Running E2E Tests

#### Quick E2E Tests (No Docker Required)
```bash
# Run all E2E tests (recommended)
mvn test -Pe2e-tests -Dtest=CompleteCrudGenerationE2ETest,FullStackCrudGenerationWithDatabaseE2ETest,SqlStatementDetectionAndCrudGenerationE2ETest

# Run fast E2E tests without Docker dependencies
mvn test -Pe2e-tests -Dtest=CompleteCrudGenerationE2ETest

# Run with Maven profile (includes unstable tests)
mvn test -Pe2e-tests

# Run specific test method
mvn test -Pe2e-tests -Dtest=CompleteCrudGenerationE2ETest#whenSelectSqlProvidedShouldGenerateCompleteSpringBootMicroserviceWithGetEndpoints
```

#### Full Integration Tests (Docker Required)
```bash
# Run full database integration test (mostly stable - REST API may occasionally fail)
mvn test -Pe2e-tests -Dtest=FullStackCrudGenerationWithDatabaseE2ETest

# Run using the convenience script (stable tests only)
./run-e2e-tests.sh

# Run with detailed output
mvn test -Pe2e-tests -X
```

#### E2E Test Profile Configuration

The E2E tests use a dedicated Maven profile in `pom.xml`:

```xml
<profile>
    <id>e2e-tests</id>
    <properties>
        <skip.unit.tests>false</skip.unit.tests>
        <skip.integration.tests>false</skip.integration.tests>
        <maven.test.includes>**/*E2E*Test.java,**/*EndToEnd*Test.java</maven.test.includes>
    </properties>
    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <configuration>
                    <includes>
                        <include>**/*E2E*Test.java</include>
                        <include>**/*EndToEnd*Test.java</include>
                    </includes>
                    <systemPropertyVariables>
                        <testcontainers.reuse.enable>true</testcontainers.reuse.enable>
                    </systemPropertyVariables>
                </configuration>
            </plugin>
        </plugins>
    </build>
</profile>
```

### E2E Test SQL Files

The E2E tests use dedicated SQL files that mirror real-world scenarios:

```bash
src/main/resources/
├── customer_select.sql     # SELECT with JOIN and parameters
├── customer_insert.sql     # INSERT with validation
├── customer_update.sql     # UPDATE with WHERE conditions
└── customer_delete.sql     # DELETE with safety parameters
```

**Example E2E SQL Files:**

```sql
-- customer_select.sql
SELECT c.customer_id, c.first_name, c.last_name, c.email 
FROM customer c 
INNER JOIN address a ON c.address_id = a.address_id 
WHERE c.active = :active AND c.customer_id = :customerId

-- customer_insert.sql
INSERT INTO customer (first_name, last_name, email, address_id, active, create_date) 
VALUES (:firstName, :lastName, :email, :addressId, :active, GETDATE())

-- customer_update.sql
UPDATE customer 
SET first_name = :firstName, last_name = :lastName, email = :email, last_update = :lastUpdate 
WHERE customer_id = :customerId AND active = :active

-- customer_delete.sql
DELETE FROM customer 
WHERE customer_id = :customerId AND active = :active
```

### E2E Test Database Schema

For Testcontainers-based tests, a minimal test schema is used:

```sql
-- sakila-test-schema.sql
CREATE TABLE country (
    country_id INT IDENTITY(1,1) PRIMARY KEY,
    country NVARCHAR(50) NOT NULL,
    last_update DATETIME2 DEFAULT GETDATE()
);

CREATE TABLE city (
    city_id INT IDENTITY(1,1) PRIMARY KEY,
    city NVARCHAR(50) NOT NULL,
    country_id INT FOREIGN KEY REFERENCES country(country_id),
    last_update DATETIME2 DEFAULT GETDATE()
);

CREATE TABLE address (
    address_id INT IDENTITY(1,1) PRIMARY KEY,
    address NVARCHAR(50) NOT NULL,
    city_id INT FOREIGN KEY REFERENCES city(city_id),
    last_update DATETIME2 DEFAULT GETDATE()
);

CREATE TABLE customer (
    customer_id INT IDENTITY(1,1) PRIMARY KEY,
    first_name NVARCHAR(50) NOT NULL,
    last_name NVARCHAR(50) NOT NULL,
    email NVARCHAR(50),
    address_id INT FOREIGN KEY REFERENCES address(address_id),
    active CHAR(1) DEFAULT 'Y',
    create_date DATETIME2 DEFAULT GETDATE(),
    last_update DATETIME2 DEFAULT GETDATE()
);

-- Sample test data
INSERT INTO country (country) VALUES ('United States'), ('Canada'), ('Mexico');
INSERT INTO city (city, country_id) VALUES ('New York', 1), ('Toronto', 2), ('Mexico City', 3);
INSERT INTO address (address, city_id) VALUES ('123 Main St', 1), ('456 Oak Ave', 2), ('789 Pine Rd', 3);
INSERT INTO customer (first_name, last_name, email, address_id, active) 
VALUES ('John', 'Doe', 'john.doe@example.com', 1, 'Y'),
       ('Jane', 'Smith', 'jane.smith@example.com', 2, 'Y'),
       ('Bob', 'Johnson', 'bob.johnson@example.com', 3, 'N');
```

### E2E Test Scenarios Covered

#### 1. Complete CRUD Generation Flow
```java
@Test
@DisplayName("1. Generate and Validate SELECT CRUD API")
void testGenerateSelectCrudApi() throws IOException {
    // Create dedicated directory for SELECT generation
    Path selectDir = Files.createTempDirectory(baseTestDir, "select-");
    
    String[] args = {
        "--name", businessName + "Select", 
        "--destination", selectDir.toString(),
        "--sql-file", "customer_select.sql"
    };
    
    MicroServiceGenerator generator = new MicroServiceGenerator();
    CommandLine commandLine = new CommandLine(generator);
    
    int exitCode = commandLine.execute(args);
    
    assertThat(exitCode)
            .as("SELECT generation should complete successfully")
            .isEqualTo(0);
    
    // Validate generated structure
    assertThat(selectDir.resolve("pom.xml")).exists();
    assertThat(selectDir.resolve("src/main/java")).exists();
    assertThat(selectDir.resolve("src/main/resources")).exists();
}
```

#### 2. Generated Code Structure Validation
```java
@Test
@DisplayName("5. Validate Generated Java Classes Structure")
void testGeneratedJavaClasses() throws IOException {
    // Generate and validate Java class structure
    Path javaRoot = testDir.resolve("src/main/java");
    
    // Check for expected Java files
    assertThat(Files.walk(javaRoot)
            .anyMatch(path -> path.getFileName().toString().contains("Application.java")))
            .as("Should have Application class")
            .isTrue();
    
    assertThat(Files.walk(javaRoot)
            .anyMatch(path -> path.getFileName().toString().contains("Controller.java")))
            .as("Should have Controller class")
            .isTrue();
    
    assertThat(Files.walk(javaRoot)
            .anyMatch(path -> path.getFileName().toString().contains("DAO.java")))
            .as("Should have DAO class")
            .isTrue();
}
```

#### 3. REST API Integration Testing
```java
@Test
@DisplayName("Test All CRUD Endpoints")
void testAllCrudEndpoints() {
    // Start generated microservice
    Process microserviceProcess = apiTester.whenProjectRootProvidedShouldStartGeneratedMicroserviceInSeparateProcess(projectRoot);
    
    try {
        // Test all CRUD operations
        apiTester.whenPostRequestSentShouldCreateCustomerThroughRestEndpointSuccessfully();
        apiTester.whenGetRequestSentShouldRetrieveCustomerDataThroughRestEndpointSuccessfully();
        apiTester.whenPutRequestSentShouldUpdateCustomerDataThroughRestEndpointSuccessfully();
        apiTester.whenDeleteRequestSentShouldRemoveCustomerThroughRestEndpointSuccessfully();
        
    } finally {
        apiTester.whenProcessRunningShouldStopMicroserviceGracefully(microserviceProcess);
    }
}
```

#### 4. Error Handling and Edge Cases
```java
@Test
@DisplayName("7. Test Error Handling and Edge Cases")
void testErrorHandlingAndEdgeCases() {
    // Test with invalid business name
    String[] invalidNameArgs = {"--name", "", "--destination", baseTestDir.toString()};
    
    int exitCode = new CommandLine(new MicroServiceGenerator()).execute(invalidNameArgs);
    
    assertThat(exitCode)
            .as("Invalid business name should fail gracefully")
            .isNotEqualTo(0);
    
    // Test with non-existent SQL file
    String[] invalidSqlArgs = {
        "--name", "TestBusiness", 
        "--destination", baseTestDir.toString(),
        "--sql-file", "non_existent_file.sql"
    };
    
    exitCode = new CommandLine(new MicroServiceGenerator()).execute(invalidSqlArgs);
    
    assertThat(exitCode)
            .as("Non-existent SQL file should fail gracefully")
            .isNotEqualTo(0);
}
```

### E2E Testing Best Practices

#### 1. Test Isolation
```java
@BeforeAll
static void setupTestEnvironment() throws IOException {
    // Create isolated test directory for each test run
    baseTestDir = Files.createTempDirectory("msg-working-e2e");
}

@AfterAll
static void cleanupTestEnvironment() throws IOException {
    // Clean up test directories after completion
    if (baseTestDir != null && Files.exists(baseTestDir)) {
        Files.walk(baseTestDir)
                .sorted(java.util.Comparator.reverseOrder())
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        System.err.println("Failed to delete: " + path);
                    }
                });
    }
}
```

#### 2. Comprehensive Validation
```java
private void validateGeneratedProject(Path projectDir) {
    // 1. Maven structure validation
    assertThat(projectDir.resolve("pom.xml")).exists();
    assertThat(projectDir.resolve("src/main/java")).exists();
    assertThat(projectDir.resolve("src/main/resources")).exists();
    
    // 2. Java class validation
    Path javaRoot = projectDir.resolve("src/main/java");
    assertThat(Files.walk(javaRoot).anyMatch(p -> p.toString().contains("Controller"))).isTrue();
    assertThat(Files.walk(javaRoot).anyMatch(p -> p.toString().contains("DAO"))).isTrue();
    assertThat(Files.walk(javaRoot).anyMatch(p -> p.toString().contains("DTO"))).isTrue();
    
    // 3. Configuration validation
    assertThat(projectDir.resolve("src/main/resources/application.properties")).exists();
}
```

#### 3. Test Execution Time Optimization
```java
// Use separate temporary directories for parallel test execution
Path testDir = Files.createTempDirectory(baseTestDir, operationType.toLowerCase() + "-");

// Enable Testcontainers reuse for faster test execution
@Container
static MSSQLServerContainer<?> sqlServer = new MSSQLServerContainer<>("mcr.microsoft.com/mssql/server:2022-latest")
        .withPassword("TestPassword@123")
        .withInitScript("e2e/sakila-test-schema.sql")
        .withReuse(true);  // Reuse containers across test runs
```

### Troubleshooting E2E Tests

#### Common Issues and Solutions

**1. Docker Connection Issues**
```bash
# Error: Could not find a valid Docker environment
# Solution: Ensure Docker is installed and running
docker info

# If Docker is unavailable, run non-Docker E2E tests
mvn test -Pe2e-tests -Dtest=CompleteCrudGenerationE2ETest
```

**2. Port Conflicts**
```bash
# Error: Port 8080 is already in use
# Solution: Stop services on port 8080 or configure different port
lsof -ti:8080 | xargs kill -9

# Or use random port in tests
@Container
static MSSQLServerContainer<?> sqlServer = new MSSQLServerContainer<>("mcr.microsoft.com/mssql/server:2022-latest")
        .withExposedPorts() // Use random available port
```

**3. Test Timeout Issues**
```bash
# Increase test timeout for slow environments
mvn test -Pe2e-tests -Dmaven.surefire.timeout=600

# Or configure in pom.xml
<configuration>
    <forkedProcessTimeoutInSeconds>600</forkedProcessTimeoutInSeconds>
</configuration>
```

**4. Resource Cleanup Issues**
```java
// Ensure proper cleanup in test teardown
@AfterEach
void cleanupTestResources() {
    // Stop any running microservices
    if (microserviceProcess != null && microserviceProcess.isAlive()) {
        microserviceProcess.destroyForcibly();
    }
    
    // Clean up temporary files
    cleanupTempDirectory(testDir);
}
```

### Continuous Integration (CI) Configuration

#### GitHub Actions Example
```yaml
name: E2E Tests
on: [push, pull_request]

jobs:
  e2e-tests:
    runs-on: ubuntu-latest
    services:
      docker:
        image: docker:19.03.12
        
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up JDK 21
        uses: actions/setup-java@v3
        with:
          java-version: '21'
          distribution: 'temurin'
          
      - name: Cache Maven dependencies
        uses: actions/cache@v3
        with:
          path: ~/.m2
          key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
          
      - name: Run E2E Tests
        run: |
          mvn clean compile
          mvn test -Pe2e-tests -Dtest=CompleteCrudGenerationE2ETest
          
      - name: Run Full Integration Tests (if Docker available)
        run: |
          if docker info > /dev/null 2>&1; then
            mvn test -Pe2e-tests -Dtest=FullStackCrudGenerationWithDatabaseE2ETest
          else
            echo "Docker not available, skipping Testcontainers tests"
          fi
          
      - name: Upload E2E Test Results
        uses: actions/upload-artifact@v3
        if: always()
        with:
          name: e2e-test-results
          path: target/surefire-reports/
```

### E2E Testing Metrics

The E2E tests provide comprehensive coverage of the MSG tool functionality:

- ✅ **CRUD API Generation**: All 4 operations (SELECT, INSERT, UPDATE, DELETE)
- ✅ **Project Structure**: Maven pom.xml, directory layout, Spring Boot configuration
- ✅ **Code Quality**: Java class generation, annotations, method signatures
- ✅ **Compilation**: Generated code compiles without errors
- ✅ **Runtime**: Generated microservices start and respond to HTTP requests
- ✅ **Error Handling**: Invalid inputs handled gracefully
- ✅ **Edge Cases**: Boundary conditions and error scenarios

**Typical Test Execution Times:**
- **Complete E2E Test Suite**: All tests: ~5-10 minutes
- **Individual Tests**:
  - CompleteCrudGenerationE2ETest: ~5 seconds ✅ STABLE
  - SqlStatementDetectionAndCrudGenerationE2ETest: ~0.2 seconds ✅ STABLE
  - FullStackCrudGenerationWithDatabaseE2ETest: ~5-10 minutes 🟡 MOSTLY STABLE (5/6 tests pass)
- **Convenience Script**: `./run-e2e-tests.sh` runs all E2E tests

This comprehensive E2E testing framework ensures that MSG generates high-quality, production-ready microservices that work correctly in real-world scenarios.

## Contributing Guidelines

### Code Standards

**1. Single Responsibility Principle**
- One public method per class
- Clear, focused functionality
- Self-documenting method names

**2. Naming Conventions**
- Classes: PascalCase (`GenerateInsertDAO`)
- Methods: camelCase (`createInsertDAO`)
- Constants: UPPER_SNAKE_CASE (`INSERT_SQL_FILE`)

**3. Documentation Requirements**
```java
/**
 * Generates a Spring Boot DAO class for INSERT operations.
 * 
 * Creates a component class with:
 * - @Component annotation for Spring dependency injection
 * - Named parameter JDBC template for SQL execution
 * - Text block SQL for readability
 * - Parameter mapping from DTO to SQL parameters
 * 
 * @param businessName The business domain name (e.g., "Customer", "Product")
 * @param metadata The INSERT metadata containing table and column information
 * @return TypeSpec representing the generated DAO class
 * @throws IllegalArgumentException if businessName or metadata is null
 */
public static TypeSpec createInsertDAO(String businessName, InsertMetadata metadata) {
    // Implementation
}
```

**4. Error Handling**
```java
// ✅ Good: Specific exceptions with context
if (metadata == null) {
    throw new IllegalArgumentException("InsertMetadata cannot be null for DAO generation");
}

// ❌ Bad: Generic exceptions
if (metadata == null) {
    throw new RuntimeException("Error");
}
```

### Pull Request Process

**1. Branch Naming**
- Features: `feature/add-postgresql-support`
- Bugs: `fix/parameter-mapping-bug`  
- Docs: `docs/update-developer-guide`

**2. Commit Messages**
```bash
# Good commit messages
feat: Add PostgreSQL database type mapping support
fix: Resolve parameter count mismatch in DELETE generation
docs: Update developer guide with testing patterns
refactor: Extract common DTO field generation logic

# Bad commit messages
fix stuff
update code
changes
```

**3. PR Requirements**
- [ ] All tests pass (`mvn test`)
- [ ] Code coverage maintained or improved
- [ ] Documentation updated (README, JavaDoc)
- [ ] Manual testing completed
- [ ] Backward compatibility preserved
- [ ] No security vulnerabilities introduced

**4. Review Checklist**
- Code follows single responsibility principle
- Generated code compiles and runs correctly
- Test coverage for new functionality
- Documentation is clear and comprehensive
- No hardcoded values or magic strings

### Development Best Practices

**1. IDE Setup**
```bash
# .editorconfig
root = true

[*.java]
indent_style = space
indent_size = 4
trim_trailing_whitespace = true
insert_final_newline = true
```

**2. Code Formatting**
```bash
# Use Maven formatter plugin
mvn formatter:format

# Or configure IDE auto-formatting
```

**3. Testing Guidelines**
- Write tests before implementing features (TDD)
- Use descriptive test method names
- Follow AAA pattern (Arrange, Act, Assert)
- Mock external dependencies
- Test both success and failure scenarios

## Extending MSG

### Adding Database Support

To add PostgreSQL support:

**1. Create PostgreSQL Type Mapping:**
```java
public enum PostgreSQLDataTypeEnum {
    INTEGER("integer", Integer.class),
    VARCHAR("varchar", String.class),
    TEXT("text", String.class),
    TIMESTAMP("timestamp", Timestamp.class),
    BOOLEAN("boolean", Boolean.class);
    
    // Implementation similar to SQLServerDataTypeEnum
}
```

**2. Create Database-Specific Configuration:**
```java
public class GeneratePostgreSQLConfig {
    public static TypeSpec createDatabaseConfig() {
        return TypeSpec.classBuilder("DatabaseConfig")
            .addAnnotation(Configuration.class)
            .addMethod(createDataSourceMethod())
            .build();
    }
    
    private static MethodSpec createDataSourceMethod() {
        return MethodSpec.methodBuilder("dataSource")
            .addAnnotation(Bean.class)
            .returns(DataSource.class)
            .addStatement("// PostgreSQL DataSource configuration")
            .build();
    }
}
```

**3. Update Metadata Extractors:**
```java
// Modify constructors to accept database type
public InsertMetadataExtractor(DatabaseType databaseType) {
    this.dataTypeMapper = switch(databaseType) {
        case SQL_SERVER -> new SQLServerDataTypeMapper();
        case POSTGRESQL -> new PostgreSQLDataTypeMapper();
        default -> throw new IllegalArgumentException("Unsupported database type");
    };
}
```

### Custom Code Templates

To customize generated code templates:

**1. Create Template Interface:**
```java
public interface CodeTemplate {
    String generateController(String businessName, Object metadata);
    String generateDAO(String businessName, Object metadata);
    String generateDTO(String businessName, Object metadata);
}
```

**2. Implement Custom Template:**
```java
public class ReactiveCodeTemplate implements CodeTemplate {
    @Override
    public String generateController(String businessName, Object metadata) {
        // Generate WebFlux reactive controllers
        return """
            @RestController
            @RequestMapping("/api")
            public class ${businessName}Controller {
                
                @PostMapping("/${businessName.toLowerCase()}")
                public Mono<ResponseEntity<String>> create(@RequestBody ${businessName}DTO dto) {
                    return service.create(dto)
                        .map(result -> ResponseEntity.ok("Created successfully"));
                }
            }
            """.replace("${businessName}", businessName);
    }
}
```

**3. Configure Template Selection:**
```java
// Add to MicroServiceGenerator
@Option(names = "--template", description = "Code generation template")
private String template = "default";

// Use appropriate template
CodeTemplate codeTemplate = switch(template) {
    case "reactive" -> new ReactiveCodeTemplate();
    case "default" -> new DefaultCodeTemplate();
    default -> throw new IllegalArgumentException("Unknown template: " + template);
};
```

This developer guide provides comprehensive information for contributors and maintainers. For usage instructions, see the [User Guide](USER_GUIDE.md).
