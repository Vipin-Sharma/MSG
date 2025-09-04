# MSG User Guide

Complete usage instructions for MSG (Microservice Generator) - transforming SQL statements into production-ready Spring Boot microservices.

## Table of Contents

- [Installation & Setup](#installation--setup)
- [Database Configuration](#database-configuration) 
- [CRUD API Generation](#crud-api-generation)
- [SQL File Requirements](#sql-file-requirements)
- [Generated Code Examples](#generated-code-examples)
- [Testing Generated Services](#testing-generated-services)
- [Advanced Usage](#advanced-usage)
- [Configuration & Customization](#configuration--customization)

## Installation & Setup

### Prerequisites
- Java 21 or later
- Maven 3.8+
- SQL Server database (for metadata extraction)
- Docker (recommended for easy database setup)

### Quick Setup

1. **Clone and Compile**
   ```bash
   git clone <repository-url>
   cd MSG
   mvn clean compile
   ```

2. **Database Setup (Choose One)**

   **Option 1: One-Click Setup (Recommended)**
   ```bash
   # Linux/Mac
   ./setup-db.sh
   
   # Windows
   setup-db.bat
   ```

   **Option 2: Docker Compose**
   ```bash
   docker-compose up -d --build
   ```

   **Option 3: Manual Docker**
   ```bash
   docker run -e "ACCEPT_EULA=Y" -e "MSSQL_SA_PASSWORD=Password@1" \
              -p 1433:1433 -d --name msgdb \
              mcr.microsoft.com/mssql/server:2022-latest
   ```

## Database Configuration

### Default Connection Settings
- **Host**: localhost:1433
- **Database**: sakila (created automatically)
- **Username**: sa
- **Password**: Password@1

### Connection String
```
jdbc:sqlserver://localhost:1433;databaseName=sakila;encrypt=true;trustServerCertificate=true
```

### Sample Data
The setup includes the Sakila database with:
- 599 customers with addresses and contact information
- 1000+ films with categories and ratings
- 16,000+ rental records with payment history
- Rich relational data perfect for testing JOINs and complex queries

## CRUD API Generation

MSG supports four types of CRUD operations, each generating specific HTTP endpoints.

### 1. SELECT API Generation (GET Endpoints)

**Purpose**: Creates GET endpoints for data retrieval with query parameters.

**SQL File**: `src/main/resources/sample_parameterized_sql.sql`
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

**Generation Command**:
```bash
mvn exec:java -Dexec.mainClass="com.jfeatures.msg.codegen.MicroServiceGenerator" \
  -Dexec.args="--name Customer --destination ./output --sql-file sample_parameterized_sql.sql"
```

**Generated API**:
- **Endpoint**: `GET /api/customer`
- **Query Parameters**: `active`, `createdDate`
- **Response**: JSON array of customer records

**Testing**:
```bash
cd ./output && mvn spring-boot:run

curl "http://localhost:8080/api/customer?active=Y&createdDate=2023-01-01"
```

### 2. INSERT API Generation (POST Endpoints)

**Purpose**: Creates POST endpoints for data creation with request body validation.

**SQL File**: `src/main/resources/sample_insert_parameterized.sql`
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

**Generation Command**:
```bash
mvn exec:java -Dexec.mainClass="com.jfeatures.msg.codegen.MicroServiceGenerator" \
  -Dexec.args="--name Customer --destination ./output --sql-file sample_insert_parameterized.sql"
```

**Generated API**:
- **Endpoint**: `POST /api/customer`
- **Content-Type**: `application/json`
- **Response**: `201 Created` on success, `400 Bad Request` for validation errors

**Testing**:
```bash
curl -X POST "http://localhost:8080/api/customer" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com", 
    "addressId": 123,
    "active": "Y",
    "createDate": "2023-12-01T10:30:00.000Z"
  }'
```

### 3. UPDATE API Generation (PUT Endpoints)

**Purpose**: Creates PUT endpoints for data modification with request body.

**SQL File**: `src/main/resources/sample_update_parameterized.sql`
```sql
UPDATE customer 
SET first_name = ?, 
    last_name = ?, 
    email = ?
WHERE customer_id = ? 
  AND active = ?
```

**Generation Command**:
```bash
mvn exec:java -Dexec.mainClass="com.jfeatures.msg.codegen.MicroServiceGenerator" \
  -Dexec.args="--name Customer --destination ./output --sql-file sample_update_parameterized.sql"
```

**Generated API**:
- **Endpoint**: `PUT /api/customer/{id}`
- **Path Variable**: Primary identifier from WHERE clause
- **Response**: `200 OK` on success, `404 Not Found` if no rows affected

**Testing**:
```bash
curl -X PUT "http://localhost:8080/api/customer/123" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Jane",
    "lastName": "Smith",
    "email": "jane.smith@example.com"
  }'
```

### 4. DELETE API Generation (DELETE Endpoints)

**Purpose**: Creates DELETE endpoints for data removal with query parameters.

**SQL File**: `src/main/resources/sample_delete_parameterized.sql`
```sql
DELETE FROM customer 
WHERE customer_id = ? 
  AND active = ?
```

**Generation Command**:
```bash
mvn exec:java -Dexec.mainClass="com.jfeatures.msg.codegen.MicroServiceGenerator" \
  -Dexec.args="--name Customer --destination ./output --sql-file sample_delete_parameterized.sql"
```

**Generated API**:
- **Endpoint**: `DELETE /api/customer`
- **Query Parameters**: Auto-generated from WHERE clause
- **Response**: `204 No Content` on success, `404 Not Found` if no rows found

**Testing**:
```bash
curl -X DELETE "http://localhost:8080/api/customer?customerId=123&active=Y"
```

## SQL File Requirements

### File Location
Place SQL files in `src/main/resources/` with these names:
- `sample_parameterized_sql.sql` (SELECT)
- `sample_insert_parameterized.sql` (INSERT)  
- `sample_update_parameterized.sql` (UPDATE)
- `sample_delete_parameterized.sql` (DELETE)

### Parameter Requirements
- Use `?` placeholders for all parameters
- Parameters must match expected database column types
- Complex WHERE clauses and JOINs are supported

### SQL Examples

**Complex SELECT with JOINs**:
```sql
SELECT 
    c.customer_id,
    c.first_name,
    c.last_name,
    c.email,
    a.address,
    ci.city,
    co.country
FROM customer c
JOIN address a ON c.address_id = a.address_id
JOIN city ci ON a.city_id = ci.city_id
JOIN country co ON ci.country_id = co.country_id
WHERE c.active = ? 
  AND c.create_date >= ?
  AND co.country = ?
```

**INSERT with Multiple Columns**:
```sql
INSERT INTO customer (
    store_id,
    first_name,
    last_name,
    email,
    address_id,
    active,
    create_date,
    last_update
) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
```

**UPDATE with Multiple Conditions**:
```sql
UPDATE customer 
SET first_name = ?,
    last_name = ?,
    email = ?,
    last_update = ?
WHERE customer_id = ? 
  AND store_id = ?
  AND active = ?
```

**DELETE with Conditions**:
```sql
DELETE FROM rental 
WHERE customer_id = ? 
  AND return_date IS NULL
  AND rental_date < ?
```

### Default Fallback Behavior

When no `--sql-file` is specified, MSG tries files in this order:
1. **UPDATE** (`sample_update_parameterized.sql`)
2. **INSERT** (`sample_insert_parameterized.sql`)  
3. **DELETE** (`sample_delete_parameterized.sql`)
4. **SELECT** (`sample_parameterized_sql.sql`)

## Generated Code Examples

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
    
    private Timestamp createDate;
}
```

### Generated Controller
```java
@RestController
@RequestMapping(path = "/api")
@Tag(name = "Customer", description = "Customer INSERT operations")
public class CustomerInsertController {
    
    @Autowired
    private CustomerInsertDAO customerInsertDAO;
    
    @PostMapping(value = "/customer", consumes = "application/json")
    @Operation(summary = "Create new customer entity")
    public ResponseEntity<String> createCustomer(@Valid @RequestBody CustomerInsertDTO request) {
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
    
    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    
    private static final String SQL = """
        INSERT INTO customer (
            first_name,
            last_name, 
            email,
            address_id,
            active,
            create_date
        ) VALUES (
            :firstName,
            :lastName,
            :email,
            :addressId,
            :active,
            :createDate
        )""";
    
    public int insertCustomer(CustomerInsertDTO request) {
        Map<String, Object> params = new HashMap<>();
        params.put("firstName", request.getFirstName());
        params.put("lastName", request.getLastName());
        params.put("email", request.getEmail());
        params.put("addressId", request.getAddressId());
        params.put("active", request.getActive());
        params.put("createDate", request.getCreateDate());
        
        return namedParameterJdbcTemplate.update(SQL, params);
    }
}
```

## Testing Generated Services

### Starting the Service
```bash
cd ./output  # Your generated microservice directory
mvn spring-boot:run
```

The service starts on port 8080 by default.

### API Testing Examples

**Test INSERT API**:
```bash
curl -X POST http://localhost:8080/api/customer \
  -H "Content-Type: application/json" \
  -d '{
    "firstName":"John",
    "lastName":"Doe",
    "email":"john@example.com",
    "addressId":1,
    "active":"Y",
    "createDate":"2023-12-01T10:30:00.000Z"
  }'
```

**Test SELECT API**:
```bash
curl "http://localhost:8080/api/customer?active=Y&createdDate=2023-01-01"
```

**Test UPDATE API**:
```bash
curl -X PUT http://localhost:8080/api/customer/123 \
  -H "Content-Type: application/json" \
  -d '{
    "firstName":"Jane",
    "lastName":"Smith",
    "email":"jane.smith@example.com"
  }'
```

**Test DELETE API**:
```bash
curl -X DELETE "http://localhost:8080/api/customer?customerId=123&active=Y"
```

### Swagger/OpenAPI Documentation

Generated services include Swagger UI accessible at:
```
http://localhost:8080/swagger-ui/index.html
```

## Advanced Usage

### Batch Generation for Multiple Domains

Create multiple microservices for different business domains:

```bash
# E-commerce system APIs
mvn exec:java -Dexec.mainClass="com.jfeatures.msg.codegen.MicroServiceGenerator" \
  -Dexec.args="--name Product --destination ./services/product-service --sql-file product_select.sql"

mvn exec:java -Dexec.mainClass="com.jfeatures.msg.codegen.MicroServiceGenerator" \
  -Dexec.args="--name Order --destination ./services/order-service --sql-file order_insert.sql"

mvn exec:java -Dexec.mainClass="com.jfeatures.msg.codegen.MicroServiceGenerator" \
  -Dexec.args="--name Customer --destination ./services/customer-service --sql-file customer_update.sql"
```

### Automated Batch Script

```bash
#!/bin/bash
DOMAINS=("Customer" "Product" "Order" "Invoice")
BASE_DIR="./generated-services"

for domain in "${DOMAINS[@]}"; do
    echo "Generating ${domain} service..."
    mvn exec:java -Dexec.mainClass="com.jfeatures.msg.codegen.MicroServiceGenerator" \
      -Dexec.args="--name $domain --destination $BASE_DIR/${domain,,}-service"
done
```

### Complex SQL Queries

MSG supports complex SQL including:
- **JOINs**: Inner, Left, Right, Full Outer
- **Subqueries**: Correlated and non-correlated
- **CTEs**: Common Table Expressions
- **Window Functions**: ROW_NUMBER, RANK, etc.
- **Aggregations**: GROUP BY, HAVING

**Example CTE Query**:
```sql
WITH customer_summary AS (
    SELECT 
        c.customer_id,
        c.first_name,
        c.last_name,
        COUNT(r.rental_id) as total_rentals,
        SUM(p.amount) as total_spent
    FROM customer c
    LEFT JOIN rental r ON c.customer_id = r.customer_id
    LEFT JOIN payment p ON r.rental_id = p.rental_id
    GROUP BY c.customer_id, c.first_name, c.last_name
)
SELECT 
    cs.customer_id,
    cs.first_name,
    cs.last_name,
    cs.total_rentals,
    cs.total_spent,
    a.address
FROM customer_summary cs
JOIN customer c ON cs.customer_id = c.customer_id
JOIN address a ON c.address_id = a.address_id
WHERE cs.total_spent > ?
  AND cs.total_rentals >= ?
ORDER BY cs.total_spent DESC
```

## Configuration & Customization

### Database Configuration

Update the generated `application.properties` for different environments:

```properties
# Development
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=sakila_dev
spring.datasource.username=dev_user
spring.datasource.password=dev_password

# Production  
spring.datasource.url=jdbc:sqlserver://prod-server:1433;databaseName=sakila_prod
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

# Connection Pool Settings
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.idle-timeout=300000
```

### Generated Project Structure

```
generated-microservice/
├── pom.xml                                    # Maven build configuration
├── src/
│   ├── main/
│   │   ├── java/com/jfeatures/msg/{domain}/
│   │   │   ├── Application.java               # Spring Boot main class
│   │   │   ├── controller/                    # REST controllers
│   │   │   │   └── {Domain}Controller.java
│   │   │   ├── dao/                          # Data access objects
│   │   │   │   └── {Domain}DAO.java
│   │   │   ├── dto/                          # Data transfer objects
│   │   │   │   └── {Domain}DTO.java
│   │   │   └── config/                       # Configuration classes
│   │   │       └── DatabaseConfig.java
│   │   └── resources/
│   │       └── application.properties         # Application configuration
│   └── test/java/com/jfeatures/              # Test classes (structure)
└── target/                                   # Build output
```

### Customizing Generated Code

#### Adding Custom Validation
```java
// Modify generated DTOs to add custom validation
@Pattern(regexp = "^[A-Za-z\\s]+$", message = "First name must contain only letters and spaces")
private String firstName;

@Email(message = "Invalid email format")
private String email;

@Min(value = 1, message = "Address ID must be positive")
private Integer addressId;
```

#### Custom Error Handling
```java
// Add to generated controllers
@ExceptionHandler(DataIntegrityViolationException.class)
public ResponseEntity<String> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body("Data integrity constraint violated: " + ex.getMessage());
}
```

#### Environment-Specific Configuration
```yaml
# application.yml (convert from properties)
spring:
  datasource:
    url: jdbc:sqlserver://${DB_HOST:localhost}:${DB_PORT:1433};databaseName=${DB_NAME:sakila}
    username: ${DB_USERNAME:sa}
    password: ${DB_PASSWORD:Password@1}
  
logging:
  level:
    com.jfeatures.msg: DEBUG
    org.springframework.jdbc: DEBUG
```

This user guide covers all essential aspects of using MSG effectively. For architectural details and contribution guidelines, see the [Developer Guide](DEVELOPER_GUIDE.md).