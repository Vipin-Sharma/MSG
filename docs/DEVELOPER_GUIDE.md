# MSG Developer Guide

Architecture, design principles, and contribution guidelines for MSG (Microservice Generator).

## Table of Contents

- [Project Philosophy & Design Principles](#project-philosophy--design-principles)
- [System Architecture](#system-architecture)
- [Development Environment Setup](#development-environment-setup)
- [Code Generation Patterns](#code-generation-patterns)
- [Development Workflow](#development-workflow)
- [Testing Strategy](#testing-strategy)
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

## JSQLParser 5.x Notes

We use JSQLParser primarily to identify statement types and to extract light structure (tables, columns, update sets). Version 5.x introduces a few API changes compared to 4.x that are reflected in our code:

- Insert columns
  - 4.x: `insert.getColumns()` returned `List<Column>`
  - 5.x: `insert.getColumns()` returns `ExpressionList<Column>`
  - Access pattern used in code: `ExpressionList<Column> cols = insert.getColumns(); List<Column> list = (cols != null) ? cols.getExpressions() : List.of();`

- Update sets (SET clause)
  - `update.getUpdateSets()` still returns `List<UpdateSet>`
  - 5.x: `UpdateSet.getColumns()` returns `ExpressionList<Column>` and `UpdateSet.getValues()` returns `ExpressionList<Expression>`
  - Iterate with `for (Column c : updateSet.getColumns().getExpressions()) { ... }`
  - Count JDBC parameters in SET using: `for (Expression e : updateSet.getValues().getExpressions()) if (e instanceof JdbcParameter) { ... }`

- Visitor generics
  - `ExpressionVisitorAdapter` in 5.x is generic. Our fallback WHERE-clause parsing uses `new ExpressionVisitorAdapter<Void>() { ... }` and overrides the generic `visitBinaryExpression` accordingly.

- Statement type detection
  - We continue to use `CCJSqlParserUtil.parse(sql)` and `instanceof Select/Insert/Update/Delete` where needed. A keyword-based fallback remains in `SqlStatementDetector` for robustness when parsing fails.

If you touch metadata extractors, keep them aligned with these patterns to remain compatible with JSQLParser 5.x.

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
