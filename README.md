# MSG - Microservice Generator

## 🎯 Project Philosophy & Purpose

### Why MSG Exists

Modern microservices development often involves repetitive boilerplate code creation - writing DTOs, Controllers, DAOs, and configuration classes for basic CRUD operations. **MSG (Microservice Generator)** was created to eliminate this tedium by generating complete, production-ready Spring Boot microservices directly from SQL statements.

### Core Philosophy: "From SQL to Service"

MSG follows a **metadata-driven approach** where a single SQL statement becomes a complete microservice:
- **Input**: SQL file (SELECT, INSERT, UPDATE, or DELETE)
- **Output**: Complete Spring Boot microservice with REST APIs, DTOs, DAOs, and configuration

### Design Principles

**1. Vipin's Principle**: Every class has exactly one public method, focused on a single responsibility.

**2. Clean Code Architecture**: Following SOLID, DRY, and YAGNI principles with:
- Self-documenting class and method names
- Single responsibility per class
- Orchestration pattern for complex workflows
- Value objects for data encapsulation

**3. Metadata-Driven Generation**: Uses database metadata and JDBC parameter extraction instead of complex SQL parsing for reliability and accuracy.

**4. Convention Over Configuration**: Follows Spring Boot and REST API conventions for predictable, maintainable output.

---

## 🏗️ Architecture Overview

### High-Level Architecture

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────────┐
│   SQL File      │───▶│ MicroService     │───▶│   Generated         │
│   (Input)       │    │ Generator        │    │   Microservice      │
│                 │    │ (Orchestrator)   │    │   (Spring Boot)     │
└─────────────────┘    └──────────────────┘    └─────────────────────┘
```

### Core Components

#### 1. Main Orchestrator
- **`MicroServiceGenerator`**: CLI entry point and orchestration hub
- **`SqlFileResolver`**: Locates and reads SQL files with intelligent fallback
- **`SqlStatementDetector`**: Identifies SQL statement type (SELECT/INSERT/UPDATE/DELETE)

#### 2. Operation-Specific Generators
Each CRUD operation has its own specialized generator following the orchestration pattern:

```
SelectMicroserviceGenerator     ┌─→ GenerateDTO
InsertMicroserviceGenerator ────┼─→ GenerateController  
UpdateMicroserviceGenerator     │  GenerateDAO
DeleteMicroserviceGenerator     └─→ GenerateSpringBootApp
```

#### 3. Metadata Extraction Layer
- **`SqlMetadata`**: Extracts SELECT result set metadata
- **`UpdateMetadataExtractor`**: Analyzes UPDATE SET and WHERE clauses
- **`InsertMetadataExtractor`**: Extracts INSERT column information
- **`DeleteMetadataExtractor`**: Analyzes DELETE WHERE conditions
- **`ParameterMetadataExtractor`**: Extracts SQL parameter information via JDBC

#### 4. Code Generation Layer
- **DTO Generators**: Create type-safe data transfer objects with validation
- **Controller Generators**: Generate REST endpoints with proper HTTP methods
- **DAO Generators**: Create data access objects with parameterized SQL
- **Configuration Generators**: Generate Spring Boot configuration classes

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

## 🚀 Getting Started

### Prerequisites

- Java 21 or later
- Maven 3.8+
- SQL Server database (for metadata extraction)
- IDE with Lombok support

### Build the Project

```bash
# Clone the repository
git clone <repository-url>
cd MSG

# Compile the project
mvn clean compile

# Build executable JAR (optional)
mvn clean package
```

---

## 💻 Usage Instructions

### Command Structure

```bash
mvn exec:java -Dexec.mainClass="com.jfeatures.msg.codegen.MicroServiceGenerator" \
  -Dexec.args="[OPTIONS]"
```

**Options:**
- `--name, -n`: Business domain name (default: "Customer")
- `--destination, -d`: Output directory (default: "/home/vipin/BusinessData")  
- `--sql-file, -f`: Specific SQL file to use (optional)

### 📋 Complete CRUD API Generation Guide

#### 1. SELECT API Generation

Creates **GET** endpoints for data retrieval with query parameters.

```bash
# Using specific SELECT SQL file
mvn exec:java -Dexec.mainClass="com.jfeatures.msg.codegen.MicroServiceGenerator" \
  -Dexec.args="--name Product --destination ./output --sql-file sample_parameterized_sql.sql"

# Using default fallback (will find sample_parameterized_sql.sql)
mvn exec:java -Dexec.mainClass="com.jfeatures.msg.codegen.MicroServiceGenerator" \
  -Dexec.args="--name Product --destination ./output"
```

**Generated API:**
- **Endpoint**: `GET /api/Product?param1=value1&param2=value2`
- **Response**: `200 OK` with JSON array
- **Features**: Query parameters from WHERE clause, result mapping to DTO

#### 2. INSERT API Generation

Creates **POST** endpoints for data creation with request body validation.

```bash
# Generate INSERT API
mvn exec:java -Dexec.mainClass="com.jfeatures.msg.codegen.MicroServiceGenerator" \
  -Dexec.args="--name Customer --destination ./output --sql-file sample_insert_parameterized.sql"
```

**Generated API:**
- **Endpoint**: `POST /api/Customer`
- **Request Body**: JSON with required fields
- **Response**: `201 Created` on success, `400 Bad Request` for validation errors
- **Features**: Jakarta validation annotations, proper HTTP status codes

**Example Request:**
```json
{
  "firstName": "John",
  "lastName": "Doe", 
  "email": "john.doe@example.com",
  "addressId": 123,
  "active": "Y"
}
```

#### 3. UPDATE API Generation

Creates **PUT** endpoints for data modification with request body.

```bash
# Generate UPDATE API  
mvn exec:java -Dexec.mainClass="com.jfeatures.msg.codegen.MicroServiceGenerator" \
  -Dexec.args="--name Order --destination ./output --sql-file sample_update_parameterized.sql"
```

**Generated API:**
- **Endpoint**: `PUT /api/Order`
- **Request Body**: JSON with fields to update + WHERE clause parameters
- **Response**: `200 OK` on success, `404 Not Found` if no rows affected
- **Features**: Separate DTOs for SET values and WHERE conditions

#### 4. DELETE API Generation

Creates **DELETE** endpoints for data removal with query parameters.

```bash
# Generate DELETE API
mvn exec:java -Dexec.mainClass="com.jfeatures.msg.codegen.MicroServiceGenerator" \
  -Dexec.args="--name Invoice --destination ./output --sql-file sample_delete_parameterized.sql"
```

**Generated API:**
- **Endpoint**: `DELETE /api/Invoice?customerId=123&status=active`
- **Response**: `204 No Content` on success, `404 Not Found` if no rows found
- **Features**: Query parameters from WHERE clause, proper deletion semantics

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

## 🛠️ Development & Extension

### Adding New SQL Statement Types
1. Create new metadata extractor in `dbmetadata` package
2. Create specialized generator classes (DTO, Controller, DAO)
3. Add orchestrator in `generator` package  
4. Update `MicroServiceGenerator` switch statement
5. Add SQL file constants and fallback logic

### Contributing Guidelines
- Follow Vipin's Principle: One public method per class
- Use self-documenting class and method names
- Add comprehensive JavaDoc documentation
- Include unit tests for new functionality
- Follow existing code formatting and patterns

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

## 📝 License & Contributing

**License**: [Specify your license]

**Contributing**: 
- Fork the repository
- Follow coding standards and architecture patterns
- Submit pull requests with comprehensive tests
- Maintain backward compatibility

---

## 👥 Team & Acknowledgments

**Created with clean code principles and architectural excellence in mind.**

**Special thanks to the principles that guide this project:**
- **Clean Code** by Robert Martin
- **SOLID Principles** for maintainable software design
- **Spring Boot** framework conventions
- **Vipin's Principle** for focused class design

---

*MSG: From SQL to Service - Eliminating microservices boilerplate, one API at a time.* 🚀