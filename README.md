# MSG - Microservice Generator

**From SQL to Service - Eliminating microservices boilerplate, one API at a time** 🚀

MSG transforms a single SQL statement into a complete, production-ready Spring Boot microservice with REST APIs, DTOs, DAOs, and configuration in seconds.

---

## 📖 Documentation Structure

- **[User Guide](#-user-guide)** - For developers using MSG to generate microservices
- **[Developer Guide](#-developer-guide)** - For contributors and maintainers of MSG

---

# 👤 User Guide

## 🎯 What is MSG?

Modern microservices development involves repetitive boilerplate code creation - writing DTOs, Controllers, DAOs, and configuration classes for basic CRUD operations. **MSG (Microservice Generator)** eliminates this tedium by generating complete, production-ready Spring Boot microservices directly from SQL statements.

### Core Philosophy: "From SQL to Service"

MSG follows a **metadata-driven approach** where a single SQL statement becomes a complete microservice:
- **Input**: SQL file (SELECT, INSERT, UPDATE, or DELETE)
- **Output**: Complete Spring Boot microservice with REST APIs, DTOs, DAOs, and configuration

## 🚀 Quick Start

### Prerequisites
- Java 21 or later
- Maven 3.8+
- SQL Server database (for metadata extraction)

### Installation
```bash
# Clone the repository
git clone <repository-url>
cd MSG

# Compile the project
mvn clean compile
```

### Basic Usage
```bash
# Generate a microservice from SQL
mvn exec:java -Dexec.mainClass="com.jfeatures.msg.codegen.MicroServiceGenerator" \
  -Dexec.args="--name Customer --destination ./output --sql-file sample_select.sql"
```

---

### 1. 🔍 SELECT API Generation (GET Endpoints)

Creates **GET** endpoints for data retrieval with query parameters.

**SQL File Required:** `src/main/resources/sample_parameterized_sql.sql`
```sql
SELECT 
    c.customer_id,
    c.first_name,
    c.last_name,
    c.email,
    a.address
FROM customer c
JOIN address a ON c.address_id = a.address_id  
WHERE c.active = ? 
  AND c.created_date >= ?
```

**Generation Command:**
```bash
# Generate SELECT API
mvn exec:java -Dexec.mainClass="com.jfeatures.msg.codegen.MicroServiceGenerator" \
  -Dexec.args="--name Customer --destination ./output --sql-file sample_parameterized_sql.sql"
```

**Generated API Features:**
- **Endpoint**: `GET /api/customer?active=Y&createdDate=2023-01-01`
- **Response**: `200 OK` with JSON array of customer records
- **Query Parameters**: Auto-generated from WHERE clause parameters

**Testing the Generated SELECT API:**
```bash
# Start the generated microservice
cd ./output && mvn spring-boot:run

# Test the GET endpoint with query parameters
curl -X GET "http://localhost:8080/api/customer?active=Y&createdDate=2023-01-01" \
  -H "Accept: application/json"

# Expected Response:
[
  {
    "customerId": 1,
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "address": "123 Main St"
  }
]
```

---

Creates **POST** endpoints for data creation with request body validation.

**SQL File Required:** `src/main/resources/sample_insert_parameterized.sql`
```sql
INSERT INTO customer (
    first_name, 
    last_name, 
    email, 
    address_id, 
    active,
    create_date
) VALUES (?, ?, ?, ?, ?, ?)
```

**Generation Command:**
```bash
# Generate INSERT API
mvn exec:java -Dexec.mainClass="com.jfeatures.msg.codegen.MicroServiceGenerator" \
  -Dexec.args="--name Customer --destination ./output --sql-file sample_insert_parameterized.sql"
```

**Generated API Features:**
- **Endpoint**: `POST /api/customer`
- **Request Body**: JSON with validated fields
- **Response**: `201 Created` on success, `400 Bad Request` for validation errors

**Testing the Generated INSERT API:**
```bash
# Start the generated microservice
cd ./output && mvn spring-boot:run

# Test the POST endpoint with JSON payload
curl -X POST "http://localhost:8080/api/customer" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com", 
    "addressId": 123,
    "active": "Y",
    "createDate": "2023-12-01T10:30:00.000Z"
  }'

# Expected Response (201 Created):
"customer created successfully"

# Test validation error (missing required field)
curl -X POST "http://localhost:8080/api/customer" \
  -H "Content-Type: application/json" \
  -d '{
    "lastName": "Doe",
    "email": "john.doe@example.com"
  }'

# Expected Response (400 Bad Request):
{
  "timestamp": "2023-12-01T10:30:00.000Z",
  "status": 400,
  "error": "Bad Request",
  "message": "firstName is required for customer creation"
}
```

---

Creates **PUT** endpoints for data modification with request body.

**SQL File Required:** `src/main/resources/sample_update_parameterized.sql`
```sql
UPDATE customer 
SET first_name = ?, 
    last_name = ?, 
    email = ?
WHERE customer_id = ? 
  AND active = ?
```

**Generation Command:**
```bash
# Generate UPDATE API
mvn exec:java -Dexec.mainClass="com.jfeatures.msg.codegen.MicroServiceGenerator" \
  -Dexec.args="--name Customer --destination ./output --sql-file sample_update_parameterized.sql"
```

**Generated API Features:**
- **Endpoint**: `PUT /api/customer/{id}`
- **Path Variable**: Primary identifier from WHERE clause
- **Request Body**: JSON with fields to update
- **Response**: `200 OK` on success, `404 Not Found` if no rows affected

**Testing the Generated UPDATE API:**
```bash
# Start the generated microservice
cd ./output && mvn spring-boot:run

# Test the PUT endpoint with path variable and JSON payload
curl -X PUT "http://localhost:8080/api/customer/123" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d '{
    "firstName": "Jane",
    "lastName": "Smith",
    "email": "jane.smith@example.com"
  }'

# Expected Response (200 OK):
"customer updated successfully"

# Test with non-existent ID
curl -X PUT "http://localhost:8080/api/customer/999999" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Test",
    "lastName": "User",
    "email": "test@example.com"
  }'

# Expected Response (404 Not Found):
"customer not found"

# Test with invalid request body (empty JSON)
curl -X PUT "http://localhost:8080/api/customer/123" \
  -H "Content-Type: application/json" \
  -d '{}'

# Expected Response (400 Bad Request):
{
  "timestamp": "2023-12-01T10:30:00.000Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed for object='customerUpdateDTO'"
}
```

---

Creates **DELETE** endpoints for data removal with query parameters.

**SQL File Required:** `src/main/resources/sample_delete_parameterized.sql`
```sql
DELETE FROM customer 
WHERE customer_id = ? 
  AND active = ?
```

**Generation Command:**
```bash
# Generate DELETE API
mvn exec:java -Dexec.mainClass="com.jfeatures.msg.codegen.MicroServiceGenerator" \
  -Dexec.args="--name Customer --destination ./output --sql-file sample_delete_parameterized.sql"
```

**Generated API Features:**
- **Endpoint**: `DELETE /api/customer?customerId=123&active=Y`
- **Query Parameters**: Auto-generated from WHERE clause
- **Response**: `204 No Content` on success, `404 Not Found` if no rows found

**Testing the Generated DELETE API:**
```bash
# Start the generated microservice
cd ./output && mvn spring-boot:run

# Test the DELETE endpoint with query parameters
curl -X DELETE "http://localhost:8080/api/customer?customerId=123&active=Y" \
  -H "Accept: application/json"

# Expected Response (204 No Content):
"customer deleted successfully"

# Test with non-existent record
curl -X DELETE "http://localhost:8080/api/customer?customerId=999999&active=Y" \
  -H "Accept: application/json"

# Expected Response (404 Not Found):
"customer not found"

# Test with missing required parameters
curl -X DELETE "http://localhost:8080/api/customer?customerId=123" \
  -H "Accept: application/json"

# Expected Response (400 Bad Request):
{
  "timestamp": "2023-12-01T10:30:00.000Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Required request parameter 'active' is missing"
}

# Test with invalid parameter types
curl -X DELETE "http://localhost:8080/api/customer?customerId=abc&active=Y" \
  -H "Accept: application/json"

# Expected Response (400 Bad Request):
{
  "timestamp": "2023-12-01T10:30:00.000Z", 
  "status": 400,
  "error": "Bad Request",
  "message": "Failed to convert value of type 'java.lang.String' to required type 'java.lang.Integer'"
}
```

### 📁 SQL File Requirements

#### Expected SQL File Location
Place your SQL files in `src/main/resources/`:
- `sample_parameterized_sql.sql` (SELECT)
- `sample_insert_parameterized.sql` (INSERT)  
- `sample_update_parameterized.sql` (UPDATE)
- `sample_delete_parameterized.sql` (DELETE)

#### SQL File Format Examples

**SELECT Example:**
```sql
SELECT 
    c.customer_id,
    c.first_name,
    c.last_name,
    c.email,
    a.address
FROM customer c
JOIN address a ON c.address_id = a.address_id  
WHERE c.active = ? 
  AND c.created_date >= ?
```

**INSERT Example:**
```sql
INSERT INTO customer (
    first_name, 
    last_name, 
    email, 
    address_id, 
    active
) VALUES (?, ?, ?, ?, ?)
```

**UPDATE Example:**
```sql
UPDATE customer 
SET first_name = ?, 
    last_name = ?, 
    email = ?
WHERE customer_id = ? 
  AND active = ?
```

**DELETE Example:**
```sql
DELETE FROM customer 
WHERE customer_id = ? 
  AND active = ?
```

### 🔄 Default Fallback Behavior

When no `--sql-file` is specified, MSG tries files in priority order:
1. **UPDATE** (`sample_update_parameterized.sql`)
2. **INSERT** (`sample_insert_parameterized.sql`)  
3. **DELETE** (`sample_delete_parameterized.sql`)
4. **SELECT** (`sample_parameterized_sql.sql`)

### 🎯 Advanced Usage Examples

#### Generate Multiple APIs for Different Domains

```bash
# E-commerce system APIs
mvn exec:java -Dexec.mainClass="com.jfeatures.msg.codegen.MicroServiceGenerator" \
  -Dexec.args="--name Product --destination ./ecommerce/product-service --sql-file product_select.sql"

mvn exec:java -Dexec.mainClass="com.jfeatures.msg.codegen.MicroServiceGenerator" \
  -Dexec.args="--name Order --destination ./ecommerce/order-service --sql-file order_insert.sql"

mvn exec:java -Dexec.mainClass="com.jfeatures.msg.codegen.MicroServiceGenerator" \
  -Dexec.args="--name Customer --destination ./ecommerce/customer-service --sql-file customer_update.sql"
```

#### Batch Generation Script
```bash
#!/bin/bash
DOMAINS=("Customer" "Product" "Order" "Invoice")
BASE_DIR="./generated-services"

for domain in "${DOMAINS[@]}"; do
    mvn exec:java -Dexec.mainClass="com.jfeatures.msg.codegen.MicroServiceGenerator" \
      -Dexec.args="--name $domain --destination $BASE_DIR/${domain,,}-service"
done
```

---

## 📖 Generated Code Examples

### Generated Controller (INSERT)
```java
@RestController
@RequestMapping(path = "/api")
@Tag(name = "Customer", description = "Customer INSERT operations")
public class CustomerInsertController {
    
    @PostMapping(value = "/customer", consumes = "application/json")
    @Operation(summary = "Create new customer entity")
    public ResponseEntity createCustomer(@Valid @RequestBody CustomerInsertDTO request) {
        int rowsAffected = customerInsertDAO.insertCustomer(request);
        if (rowsAffected > 0) {
            return ResponseEntity.status(HttpStatus.CREATED)
                .body("customer created successfully");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to create customer");
        }
    }
}
```

### Generated DAO with Text Block SQL
```java
@Component
public class CustomerInsertDAO {
    private static final String SQL = """
        INSERT INTO customer (
            first_name,
            last_name, 
            email,
            address_id,
            active
        ) VALUES (
            :firstName,
            :lastName,
            :email,
            :addressId,
            :active
        )""";
    
    public int insertCustomer(CustomerInsertDTO request) {
        Map<String, Object> params = new HashMap<>();
        params.put("firstName", request.getFirstName());
        params.put("lastName", request.getLastName());
        params.put("email", request.getEmail());
        params.put("addressId", request.getAddressId());
        params.put("active", request.getActive());
        
        return namedParameterJdbcTemplate.update(SQL, params);
    }
}
```

### Generated DTO with Validation
```java
@Builder
@Value
@Jacksonized
public class CustomerInsertDTO {
    @NotNull(message = "firstName is required for customer creation")
    private String firstName;
    
    @NotNull(message = "lastName is required for customer creation")  
    private String lastName;
    
    private String email; // Nullable field
    
    @NotNull(message = "addressId is required for customer creation")
    private Integer addressId;
    
    @NotNull(message = "active is required for customer creation")
    private String active;
}
```

---

## 🔧 Configuration & Customization

### Database Configuration
Update database connection in generated `application.properties`:
```properties
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=your_db
spring.datasource.username=your_username  
spring.datasource.password=your_password
spring.datasource.driver-class-name=com.microsoft.sqlserver.jdbc.SQLServerDriver
```

### Customizing Generated Code
- **Business Domain Names**: Use meaningful `--name` values like "Product", "Order", "Customer"
- **Package Structure**: Generated code follows `com.jfeatures.msg.{domain}.{layer}` pattern
- **Validation Rules**: Modify generated DTOs to add custom validation annotations
- **HTTP Endpoints**: Controllers generate REST endpoints following `{HTTP_METHOD} /api/{domain}` pattern

---

## 🧪 Testing Generated Microservices

### Running Generated Service
```bash
cd ./output  # Your generated microservice directory
mvn spring-boot:run
```

### API Testing Examples

**Test INSERT API:**
```bash
curl -X POST http://localhost:8080/api/customer \
  -H "Content-Type: application/json" \
  -d '{"firstName":"John","lastName":"Doe","email":"john@example.com","addressId":1,"active":"Y"}'
```

**Test SELECT API:**
```bash
curl "http://localhost:8080/api/customer?active=Y&customerId=123"
```

**Test UPDATE API:**
```bash
curl -X PUT http://localhost:8080/api/customer \
  -H "Content-Type: application/json" \
  -d '{"firstName":"Jane","lastName":"Smith","customerId":123}'
```

**Test DELETE API:**
```bash
curl -X DELETE "http://localhost:8080/api/customer?customerId=123&active=Y"
```

---

## 🎯 All 4 CRUD API Generation Commands

Here are the complete command-line examples for generating all 4 types of CRUD APIs:

### 1. SELECT API Generation
```bash
mvn exec:java -Dexec.mainClass="com.jfeatures.msg.codegen.MicroServiceGenerator" \
  -Dexec.args="--name Customer --destination ./output --sql-file sample_parameterized_sql.sql"
```

### 2. INSERT API Generation  
```bash
mvn exec:java -Dexec.mainClass="com.jfeatures.msg.codegen.MicroServiceGenerator" \
  -Dexec.args="--name Customer --destination ./output --sql-file sample_insert_parameterized.sql"
```

### 3. UPDATE API Generation
```bash
mvn exec:java -Dexec.mainClass="com.jfeatures.msg.codegen.MicroServiceGenerator" \
  -Dexec.args="--name Customer --destination ./output --sql-file sample_update_parameterized.sql"
```

### 4. DELETE API Generation
```bash
mvn exec:java -Dexec.mainClass="com.jfeatures.msg.codegen.MicroServiceGenerator" \
  -Dexec.args="--name Customer --destination ./output --sql-file sample_delete_parameterized.sql"
```

---

## 🔄 Batch Generation & Advanced Usage

### Generate Multiple APIs for Different Domains
```bash
# E-commerce system APIs
mvn exec:java -Dexec.mainClass="com.jfeatures.msg.codegen.MicroServiceGenerator" \
  -Dexec.args="--name Product --destination ./ecommerce/product-service --sql-file product_select.sql"

mvn exec:java -Dexec.mainClass="com.jfeatures.msg.codegen.MicroServiceGenerator" \
  -Dexec.args="--name Order --destination ./ecommerce/order-service --sql-file order_insert.sql"

mvn exec:java -Dexec.mainClass="com.jfeatures.msg.codegen.MicroServiceGenerator" \
  -Dexec.args="--name Customer --destination ./ecommerce/customer-service --sql-file customer_update.sql"

mvn exec:java -Dexec.mainClass="com.jfeatures.msg.codegen.MicroServiceGenerator" \
  -Dexec.args="--name Invoice --destination ./ecommerce/invoice-service --sql-file invoice_delete.sql"
```

### Automated Batch Generation Script
```bash
#!/bin/bash
DOMAINS=("Customer" "Product" "Order" "Invoice")
BASE_DIR="./generated-services"

for domain in "${DOMAINS[@]}"; do
    mvn exec:java -Dexec.mainClass="com.jfeatures.msg.codegen.MicroServiceGenerator" \
      -Dexec.args="--name $domain --destination $BASE_DIR/${domain,,}-service"
done
```

### Default Fallback Behavior
When no `--sql-file` is specified, MSG tries files in priority order:
1. **UPDATE** (`sample_update_parameterized.sql`)
2. **INSERT** (`sample_insert_parameterized.sql`)  
3. **DELETE** (`sample_delete_parameterized.sql`)
4. **SELECT** (`sample_parameterized_sql.sql`)

---

## 🚨 Troubleshooting

### Common Issues

**1. Database Connection Errors**
```
Solution: Verify database URL, credentials, and SQL Server driver
Check: application.properties and database service status
```

**2. SQL File Not Found**
```
Solution: Ensure SQL files are in src/main/resources/
Check: File names match expected patterns (sample_*_parameterized.sql)
```

**3. Parameter Count Mismatch**  
```
Solution: Verify SQL parameter count matches ? placeholders
Check: Complex WHERE clauses and JOIN conditions
```

**4. Compilation Errors in Generated Code**
```
Solution: Check database metadata extraction and column name mapping
Check: SQL statement syntax and parameter types
```

### Debug Mode
Add `-X` flag to Maven commands for verbose output:
```bash
mvn -X exec:java -Dexec.mainClass="com.jfeatures.msg.codegen.MicroServiceGenerator" \
  -Dexec.args="--name Customer --destination ./output"
```

---

## 🎉 Success Stories

MSG transforms microservices development by:
- **90% Less Boilerplate**: Eliminates repetitive CRUD code writing
- **Type-Safe APIs**: Generates strongly-typed DTOs and controllers  
- **Production Ready**: Includes validation, error handling, and proper HTTP semantics
- **Consistent Architecture**: Follows Spring Boot and REST conventions
- **Rapid Prototyping**: From SQL to running service in minutes

---

## 📝 License

This project is licensed under the **Apache License 2.0**. See the [LICENSE](LICENSE) file for details.

```
Copyright 2023 MSG Contributors

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

---

## 👥 Team & Contributors

MSG is developed and maintained by a dedicated team of contributors:

### 🏗️ Project Lead & Core Developer
- **Vipin Sharma** ([@vipinsharma85](mailto:vipinsharma85@gmail.com))

### 💻 Code Contributors
- **Helber Belmiro** ([@helber-belmiro](mailto:helber.belmiro@gmail.com))
- **Bruno Alves** ([@brunobaiano](mailto:brunobaiano@users.noreply.github.com))
- **Nikita Koselev** ([@nikitakoselev](mailto:nikitakoselev@users.noreply.github.com))
- **Bruno Souza**

### 📋 Contributing

We welcome contributions from the community! Here's how to get involved:

**Getting Started:**
- Fork the repository
- Follow our coding standards and architecture patterns
- Ensure comprehensive test coverage for new functionality
- Maintain backward compatibility with existing features

**Contribution Guidelines:**
- Follow single responsibility principle: One public method per class
- Write self-documenting class and method names
- Add comprehensive JavaDoc documentation
- Include unit tests for all new functionality
- Follow existing code formatting and architectural patterns

**Pull Request Process:**
1. Create a feature branch from `main`
2. Make your changes following the established patterns
3. Add tests and ensure all tests pass
4. Update documentation including README and JavaDoc
5. Submit a pull request with a detailed description

### 🏆 Acknowledgments

**Created with clean code principles and architectural excellence in mind.**

**Special thanks to the principles and technologies that guide this project:**
- **Clean Code** by Robert Martin - Foundation for maintainable code
- **SOLID Principles** - Core design principles for sustainable software
- **Spring Boot** framework conventions - Industry-standard practices
- **JavaPoet** - Elegant code generation capabilities

---

# 🔧 Developer Guide

## 🎯 Project Philosophy & Design Principles

### Core Philosophy: "From SQL to Service"
MSG eliminates microservices boilerplate by transforming SQL statements into complete Spring Boot applications through metadata-driven generation.

### Design Principles

**1. Single Responsibility Principle**: Every class has exactly one public method, focused on a single responsibility.

**2. Clean Code Architecture**: Following SOLID, DRY, and YAGNI principles with:
- Self-documenting class and method names
- Single responsibility per class  
- Orchestration pattern for complex workflows
- Value objects for data encapsulation

**3. Metadata-Driven Generation**: Uses database metadata and JDBC parameter extraction instead of complex SQL parsing for reliability and accuracy.

**4. Convention Over Configuration**: Follows Spring Boot and REST API conventions for predictable, maintainable output.

---

## 🏗️ System Architecture

### High-Level Architecture

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────────┐
│   SQL File      │───▶│ MicroService     │───▶│   Generated         │
│   (Input)       │    │ Generator        │    │   Microservice      │
│                 │    │ (Orchestrator)   │    │   (Spring Boot)     │
└─────────────────┘    └──────────────────┘    └─────────────────────┘
```

### Core Components

#### 1. Main Orchestrator Layer
- **`MicroServiceGenerator`**: CLI entry point and orchestration hub
- **`SqlFileResolver`**: Locates and reads SQL files with intelligent fallback
- **`SqlStatementDetector`**: Identifies SQL statement type (SELECT/INSERT/UPDATE/DELETE)

#### 2. Operation-Specific Generator Layer
Each CRUD operation has its own specialized generator following the orchestration pattern:

```
SelectMicroserviceGenerator     ┌─→ GenerateDTO
InsertMicroserviceGenerator ────┼─→ GenerateController  
UpdateMicroserviceGenerator     │  GenerateDAO
DeleteMicroserviceGenerator     └─→ GenerateSpringBootApp
```

**Key Classes:**
- `SelectMicroserviceGenerator.java`
- `InsertMicroserviceGenerator.java` 
- `UpdateMicroserviceGenerator.java`
- `DeleteMicroserviceGenerator.java`

#### 3. Metadata Extraction Layer
- **`SqlMetadata`**: Extracts SELECT result set metadata
- **`UpdateMetadataExtractor`**: Analyzes UPDATE SET and WHERE clauses
- **`InsertMetadataExtractor`**: Extracts INSERT column information
- **`DeleteMetadataExtractor`**: Analyzes DELETE WHERE conditions (legacy)
- **`ParameterMetadataExtractor`**: Extracts SQL parameter information via JDBC (used by DELETE)

#### 4. Code Generation Layer
**DTO Generators:**
- `GenerateDTO.java` (SELECT)
- `GenerateInsertDTO.java` (INSERT)
- `GenerateUpdateDTO.java` (UPDATE)
- `GenerateDeleteDTO.java` (DELETE)

**Controller Generators:**
- `GenerateController.java` (SELECT)
- `GenerateInsertController.java` (INSERT)
- `GenerateUpdateController.java` (UPDATE) 
- `GenerateDeleteController.java` (DELETE)

**DAO Generators:**
- `GenerateDAO.java` (SELECT)
- `GenerateInsertDAO.java` (INSERT)
- `GenerateUpdateDAO.java` (UPDATE)
- `GenerateDeleteDAO.java` (DELETE)

**Configuration Generators:**
- `GenerateSpringBootApp.java`
- `GenerateDatabaseConfig.java`

#### 5. File System Layer
- **`MicroserviceProjectWriter`**: Orchestrates complete project writing
- **`ProjectDirectoryBuilder`**: Creates Maven-standard directory structure
- **`MicroserviceDirectoryCleaner`**: Cleans target directories safely

### Generated Project Structure

```
generated-microservice/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/jfeatures/msg/{domain}/
│   │   │       ├── Application.java
│   │   │       ├── controller/
│   │   │       │   └── {Domain}Controller.java
│   │   │       ├── dao/
│   │   │       │   └── {Domain}DAO.java
│   │   │       ├── dto/
│   │   │       │   └── {Domain}DTO.java
│   │   │       └── config/
│   │   │           └── DatabaseConfig.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/java/com/jfeatures/
└── target/
```

### Technology Stack

- **Java 21**: Modern Java features including text blocks and records
- **Spring Boot 3.x**: Latest Spring Boot framework
- **Spring Data JDBC**: For database operations
- **JavaPoet**: Code generation library
- **Lombok**: Reduces boilerplate code
- **Jakarta Validation**: Input validation
- **OpenAPI/Swagger**: API documentation
- **Picocli**: Command-line interface
- **Maven**: Build and dependency management

---

## 🔧 Development Workflow

### Adding New SQL Statement Types

1. **Create Metadata Extractor** in `dbmetadata` package:
   ```java
   public class NewStatementMetadataExtractor {
       public NewStatementMetadata extractMetadata(String sql) throws Exception {
           // Implementation following single responsibility principle
       }
   }
   ```

2. **Create Metadata Value Object**:
   ```java
   public record NewStatementMetadata(
       String tableName,
       List<ColumnMetadata> columns,
       String originalSql
   ) {}
   ```

3. **Create Code Generators**:
   - `GenerateNewStatementDTO.java`
   - `GenerateNewStatementController.java`
   - `GenerateNewStatementDAO.java`

4. **Create Orchestrator** in `generator` package:
   ```java
   public class NewStatementMicroserviceGenerator {
       public GeneratedMicroservice generateMicroservice(...) {
           // Orchestration logic following established patterns
       }
   }
   ```

5. **Update Main Generator** switch statement in `MicroServiceGenerator.java`

6. **Add SQL File Constants** and fallback logic in `SqlFileResolver.java`

### Code Generation Patterns

#### JavaPoet Usage Examples

**Field Generation:**
```java
FieldSpec fieldSpec = FieldSpec.builder(String.class, "fieldName")
    .addModifiers(Modifier.PRIVATE)
    .addAnnotation(AnnotationSpec.builder(NotNull.class)
        .addMember("message", "$S", "Field is required")
        .build())
    .build();
```

**Method Generation:**
```java
MethodSpec methodSpec = MethodSpec.methodBuilder("methodName")
    .addModifiers(Modifier.PUBLIC)
    .returns(ResponseEntity.class)
    .addParameter(ParameterSpec.builder(RequestDTO.class, "request")
        .addAnnotation(Valid.class)
        .addAnnotation(RequestBody.class)
        .build())
    .addStatement("// Implementation")
    .build();
```

**SQL Text Blocks:**
```java
String sql = """
    SELECT column1, column2 
    FROM table_name 
    WHERE condition = :parameter
    """;
```

### Database Type Mapping

MSG uses `SQLServerDataTypeEnum` for database-to-Java type mapping:

```java
// JDBC Type → Database Type → Java Type
"INTEGER" → "INT" → Integer.class
"VARCHAR" → "NVARCHAR" → String.class  
"CHAR" → "CHAR" → String.class
"TIMESTAMP" → "DATETIME2" → Timestamp.class
```

### Parameter Extraction Strategy

**SELECT/UPDATE/INSERT**: Database metadata extraction via `InsertMetadataExtractor`, `UpdateMetadataExtractor`

**DELETE**: JDBC parameter extraction via `ParameterMetadataExtractor` for reliability
```java
List<DBColumn> parameters = parameterExtractor.extractParameters(sql);
// Handles parameter-to-column name mapping automatically
```

---

## 🧪 Testing & Quality Assurance

### Running Tests

```bash
# Run unit tests
mvn test

# Run integration tests  
mvn integration-test

# Generate test coverage report
mvn jacoco:report
```

### Code Quality Checks

```bash
# Static analysis
mvn spotbugs:check

# Code formatting
mvn formatter:format

# Dependency vulnerability scan
mvn dependency-check:check
```

### Manual Testing Workflow

1. **Generate Sample Microservices:**
   ```bash
   # Test each CRUD operation
   mvn exec:java -Dexec.args="--name TestSelect --sql-file sample_parameterized_sql.sql"
   mvn exec:java -Dexec.args="--name TestInsert --sql-file sample_insert_parameterized.sql"
   mvn exec:java -Dexec.args="--name TestUpdate --sql-file sample_update_parameterized.sql"  
   mvn exec:java -Dexec.args="--name TestDelete --sql-file sample_delete_parameterized.sql"
   ```

2. **Verify Generated Code Compiles:**
   ```bash
   cd ./generated-output && mvn clean compile
   ```

3. **Test Runtime Functionality:**
   ```bash
   mvn spring-boot:run
   # Test APIs with curl commands from User Guide
   ```

---

## 🛠️ Extending MSG

### Custom Database Support

To add support for PostgreSQL/MySQL:

1. **Create Database-Specific Enum:**
   ```java
   public enum PostgreSQLDataTypeEnum {
       INTEGER("integer", Integer.class),
       VARCHAR("varchar", String.class),
       // ... other types
   }
   ```

2. **Update Metadata Extractors** to use appropriate type mapping

3. **Create Database-Specific Configuration** generators

### Adding New Annotation Support

For additional validation annotations:

1. **Extend DTO Generators** with new annotation logic
2. **Update Field Generation** patterns
3. **Add Configuration** for annotation behavior

### Custom Code Templates

MSG uses JavaPoet for code generation. To customize templates:

1. **Modify Generator Classes** in respective packages
2. **Update CodeBlock/MethodSpec** builders
3. **Test Generated Output** thoroughly

---

## 📋 Contributing Guidelines

### Code Standards

- **Follow single responsibility principle**: One public method per class
- **Use self-documenting names**: Class and method names should describe purpose
- **Add comprehensive JavaDoc**: Document all public methods and classes
- **Include unit tests**: Test coverage for new functionality
- **Follow existing patterns**: Maintain consistency with established architecture

### Pull Request Process

1. **Fork the repository** and create feature branch
2. **Follow naming conventions**: `feature/add-postgresql-support`
3. **Write comprehensive tests** for new functionality
4. **Update documentation** including README and JavaDoc
5. **Ensure backward compatibility** with existing functionality
6. **Submit PR** with detailed description of changes

### Development Environment Setup

```bash
# Clone repository
git clone <repository-url>
cd MSG

# Install dependencies
mvn clean install

# Run tests to verify setup
mvn test

# Setup IDE with:
# - Lombok plugin
# - Java 21 support
# - Maven integration
```

---

## 🚨 Debugging & Troubleshooting

### Common Development Issues

**1. JavaPoet Compilation Errors**
```java
// ❌ Incorrect annotation syntax
.member("value", "string")

// ✅ Correct annotation syntax  
.addMember("value", "$S", "string")
```

**2. Parameter Count Mismatches**
- Use `ParameterMetadataExtractor` for reliable parameter detection
- Implement JDBC-to-database type mapping for consistency

**3. SQL Text Block Formatting**
```java
// ✅ Proper formatting with consistent indentation
String sql = """
    SELECT column1, column2
    FROM table_name  
    WHERE condition = :parameter
    """;
```

### Debug Mode Analysis

Enable verbose logging:
```bash
mvn -X exec:java -Dexec.args="--name Debug --destination ./debug-output"
```

### Performance Considerations

- **Database Connection Pooling**: Use for metadata extraction
- **Caching Strategy**: Consider caching metadata for repeated generations
- **Memory Usage**: Monitor JavaPoet object creation for large schemas

---

*MSG Developer Guide - Building the future of microservices generation* 🚀