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

## Audit Field Support

MSG automatically handles audit fields (created_at, created_by, updated_at, updated_by) in generated code.

### ⚠️ **PREREQUISITES - DATABASE SCHEMA REQUIREMENT**

**CRITICAL:** Before using audit field functionality, your database tables MUST include the audit columns:

```sql
-- Add these columns to your tables BEFORE code generation:
created_at datetime DEFAULT CURRENT_TIMESTAMP,
created_by varchar(100),
updated_at datetime,
updated_by varchar(100)
```

**Why This Matters:**
- MSG uses **database metadata extraction** (not SQL parsing) to generate code
- If audit columns don't exist in the database schema, they will be **silently skipped**
- Code generation will **fail fast** with a clear error message if SQL references missing columns
- This ensures you never accidentally deploy code with incomplete audit functionality

**Verification Steps:**
1. After running DDL scripts, verify columns exist:
   ```sql
   SELECT COLUMN_NAME, DATA_TYPE
   FROM INFORMATION_SCHEMA.COLUMNS
   WHERE TABLE_NAME = 'your_table_name'
     AND COLUMN_NAME IN ('created_at', 'created_by', 'updated_at', 'updated_by');
   ```
2. Run code generation - it will fail fast if columns are missing
3. Check generated code includes `HttpServletRequest` parameter and audit logic

### How It Works

1. **Detection**: Audit fields are detected based on column names configured in `src/main/resources/audit-config.yaml`
2. **DTO Generation**: Audit fields are **excluded** from DTOs (users don't provide them)
3. **DAO Generation**: Audit fields are **auto-populated** in the DAO layer:
   - Timestamp fields: `LocalDateTime.now()`
   - User ID fields: Extracted from JWT token or `X-User-Id` header via `AuthenticationUtil`

### Configuration

Edit `src/main/resources/audit-config.yaml` to configure audit field detection:

```yaml
auditFields:
  createdAt: [created_at, create_date, created_timestamp]
  createdBy: [created_by, creator_id]
  updatedAt: [updated_at, last_update, last_modified]
  updatedBy: [updated_by, modifier_id, last_updated_by]
```

### Example Usage

**SQL with Audit Fields**:
```sql
INSERT INTO customer (
    first_name, last_name, email,
    created_at,    -- Auto-populated
    created_by     -- Auto-populated
) VALUES (?, ?, ?, ?, ?)
```

**Generated DTO** (audit fields excluded):
```java
@Value @Builder
public class CustomerInsertDTO {
    private String firstName;
    private String lastName;
    private String email;
    // NO created_at or created_by
}
```

**Generated DAO** (audit fields auto-populated):
```java
public int insertCustomer(CustomerInsertDTO insertRequest, HttpServletRequest request) {
    Map<String, Object> sqlParamMap = new HashMap<>();
    sqlParamMap.put("firstName", insertRequest.getFirstName());
    sqlParamMap.put("lastName", insertRequest.getLastName());
    sqlParamMap.put("email", insertRequest.getEmail());

    // Auto-populate audit fields
    sqlParamMap.put("createdAt", LocalDateTime.now());
    sqlParamMap.put("createdBy", AuthenticationUtil.getUserId(request));

    return namedParameterJdbcTemplate.update(SQL, sqlParamMap);
}
```

**Generated Controller** (passes HttpServletRequest):
```java
@PostMapping("/customer")
public ResponseEntity<Void> insertCustomer(
        @Valid @RequestBody CustomerInsertDTO insertRequest,
        HttpServletRequest request) {
    int rowsAffected = customerInsertDAO.insertCustomer(insertRequest, request);
    return ResponseEntity.status(HttpStatus.CREATED).build();
}
```

### Authentication

The generated `AuthenticationUtil` extracts user ID from:
1. **JWT Token** (primary): Parses `Authorization: Bearer <token>` header
   - Checks claims: `userId`, `sub`, `user_id`
2. **HTTP Header** (fallback): Reads `X-User-Id` header
3. **Default**: Returns `"system"` if no authentication found

### Sample SQL Files

- `src/main/resources/sample_insert_with_audit.sql` - INSERT with audit fields
- `src/main/resources/sample_update_with_audit.sql` - UPDATE with audit fields

### Troubleshooting Audit Fields

**Problem:** Generated code doesn't include audit field auto-population (no `HttpServletRequest`, no `LocalDateTime.now()`)

**Common Causes & Solutions:**

1. **Database columns are missing** ❌ **MOST COMMON**
   - **Check:** Do the audit columns exist in your database table?
   - **Fix:** Run DDL to add columns to the table schema, then regenerate code
   - **Verify:**
     ```sql
     SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_NAME = 'your_table' AND COLUMN_NAME LIKE '%created%' OR COLUMN_NAME LIKE '%updated%';
     ```

2. **Column names don't match audit-config.yaml patterns**
   - **Check:** Review `src/main/resources/audit-config.yaml` for configured column name patterns
   - **Fix:** Either rename database columns to match patterns OR update `audit-config.yaml`
   - **Example:** If your column is `creation_date`, add it to the `createdAt` list in YAML

3. **SQL file references columns not in database**
   - **Check:** Does your INSERT/UPDATE SQL reference columns that don't exist in the table?
   - **Error Message:** `Column 'created_at' specified in INSERT statement does not exist in table 'customer'`
   - **Fix:** Add missing columns to database schema OR remove them from SQL file

4. **Code generation succeeded but no audit logic**
   - **Check:** Did you see any warnings during generation about "Could not extract metadata"?
   - **Fix:** This now throws an exception (as of latest version) - you must fix schema first

5. **Audit config not loading**
   - **Check:** Is `audit-config.yaml` in `src/main/resources/`?
   - **Error Message:** `Audit configuration is empty or failed to load from audit-config.yaml`
   - **Fix:** Ensure file exists and has valid YAML syntax

**Debugging Tips:**
- Run code generation with `-X` flag to see detailed logs
- Check if `auditFields` map is populated in metadata extraction logs
- Verify generated DAO has `// Auto-populate audit fields` comment
- Look for `HttpServletRequest request` parameter in DAO method signature

