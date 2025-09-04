# MSG - Microservice Generator

## Code Quality & Metrics

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=Vipin-Sharma_MSG&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=Vipin-Sharma_MSG)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=Vipin-Sharma_MSG&metric=coverage)](https://sonarcloud.io/component_measures/metric/coverage/list?id=Vipin-Sharma_MSG)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=Vipin-Sharma_MSG&metric=sqale_rating)](https://sonarcloud.io/component_measures/metric/sqale_rating/list?id=Vipin-Sharma_MSG)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=Vipin-Sharma_MSG&metric=reliability_rating)](https://sonarcloud.io/component_measures/metric/reliability_rating/list?id=Vipin-Sharma_MSG)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=Vipin-Sharma_MSG&metric=security_rating)](https://sonarcloud.io/component_measures/metric/security_rating/list?id=Vipin-Sharma_MSG)
[![Duplicated Lines (%)](https://sonarcloud.io/api/project_badges/measure?project=Vipin-Sharma_MSG&metric=duplicated_lines_density)](https://sonarcloud.io/component_measures/metric/duplicated_lines_density/list?id=Vipin-Sharma_MSG)

*Click badges for detailed analysis and metrics on [SonarCloud](https://sonarcloud.io/project/overview?id=Vipin-Sharma_MSG)*

**From SQL to Service - Eliminating microservices boilerplate, one API at a time** 🚀

MSG transforms a single SQL statement into a complete, production-ready Spring Boot microservice with REST APIs, DTOs, DAOs, and configuration in seconds.

## 🎯 What is MSG?

Modern microservices development involves repetitive boilerplate code creation. MSG eliminates this by generating complete Spring Boot microservices directly from SQL statements using a **metadata-driven approach**.

### Core Capabilities
- **SELECT** → GET APIs with query parameters  
- **INSERT** → POST APIs with validated request bodies
- **UPDATE** → PUT APIs with path variables and request bodies
- **DELETE** → DELETE APIs with query parameters
- **Complete Projects** → Maven structure, dependencies, and configuration

### Key Differentiators
- **Metadata-driven**: Uses database metadata, not SQL parsing
- **Type-safe**: Generates strongly-typed DTOs with validation
- **Production-ready**: Includes error handling and proper HTTP semantics
- **Enterprise standards**: Follows Spring Boot and clean code conventions

## 🚀 Quick Start

### Prerequisites
- Java 21 or later
- Maven 3.8+
- SQL Server database
- Docker (recommended for database setup)

### 1. Database Setup (One Command)
```bash
# Linux/Mac
./setup-db.sh

# Windows  
setup-db.bat

# Manual
docker-compose up -d --build
```

This sets up SQL Server with sample Sakila database (599 customers, 1000 films, 16k+ rentals).

### 2. Generate Your First Microservice
```bash
# Clone and compile
git clone <repository-url>
cd MSG
mvn clean compile

# Generate SELECT API
mvn exec:java -Dexec.mainClass="com.jfeatures.msg.codegen.MicroServiceGenerator" \
  -Dexec.args="--name Customer --destination ./output --sql-file sample_parameterized_sql.sql"

# Test the generated service
cd ./output
mvn spring-boot:run
```

### 3. Test Your API
```bash
# Test GET endpoint
curl "http://localhost:8080/api/customer?active=Y&customerId=123"
```

## 🔧 Core Commands

### Generate All CRUD Operations
```bash
# SELECT API (GET endpoints)
mvn exec:java -Dexec.mainClass="com.jfeatures.msg.codegen.MicroServiceGenerator" \
  -Dexec.args="--name Customer --destination ./output --sql-file sample_parameterized_sql.sql"

# INSERT API (POST endpoints)  
mvn exec:java -Dexec.mainClass="com.jfeatures.msg.codegen.MicroServiceGenerator" \
  -Dexec.args="--name Customer --destination ./output --sql-file sample_insert_parameterized.sql"

# UPDATE API (PUT endpoints)
mvn exec:java -Dexec.mainClass="com.jfeatures.msg.codegen.MicroServiceGenerator" \
  -Dexec.args="--name Customer --destination ./output --sql-file sample_update_parameterized.sql"

# DELETE API (DELETE endpoints)
mvn exec:java -Dexec.mainClass="com.jfeatures.msg.codegen.MicroServiceGenerator" \
  -Dexec.args="--name Customer --destination ./output --sql-file sample_delete_parameterized.sql"
```

## 📁 SQL File Requirements

Place SQL files in `src/main/resources/` with parameterized queries:

```sql
-- sample_parameterized_sql.sql (SELECT)
SELECT c.customer_id, c.first_name, c.last_name 
FROM customer c 
WHERE c.active = ? AND c.created_date >= ?

-- sample_insert_parameterized.sql (INSERT)  
INSERT INTO customer (first_name, last_name, email) 
VALUES (?, ?, ?)

-- sample_update_parameterized.sql (UPDATE)
UPDATE customer SET first_name = ?, email = ? 
WHERE customer_id = ?

-- sample_delete_parameterized.sql (DELETE)
DELETE FROM customer WHERE customer_id = ? AND active = ?
```

## 🏗️ Generated Code Structure

```
generated-microservice/
├── pom.xml
├── src/main/java/com/jfeatures/msg/{domain}/
│   ├── Application.java
│   ├── controller/{Domain}Controller.java
│   ├── dao/{Domain}DAO.java  
│   ├── dto/{Domain}DTO.java
│   └── config/DatabaseConfig.java
└── src/main/resources/application.properties
```

## 📖 Documentation

- **[User Guide](docs/USER_GUIDE.md)** - Complete usage instructions and examples
- **[Developer Guide](docs/DEVELOPER_GUIDE.md)** - Architecture and contribution guide
- **[Security Guide](docs/SECURITY.md)** - Security best practices and known issues
- **[Troubleshooting](docs/TROUBLESHOOTING.md)** - Common issues and solutions

## 🎉 Success Stories

MSG transforms microservices development by:
- **90% Less Boilerplate**: Eliminates repetitive CRUD code writing
- **Type-Safe APIs**: Generates strongly-typed DTOs and controllers
- **Production Ready**: Includes validation, error handling, and proper HTTP semantics
- **Rapid Prototyping**: From SQL to running service in minutes


## 👥 Contributing

We welcome contributions! Please see our [Developer Guide](docs/DEVELOPER_GUIDE.md) for:
- Development setup and workflow
- Architecture overview and design principles  
- Adding new features and SQL statement types
- Code standards and pull request process

## 📝 License

This project is licensed under the **Apache License 2.0**. See the [LICENSE](LICENSE) file for details.

---

**Created with clean code principles and architectural excellence in mind.**

*MSG - Building the future of microservices generation* 🚀