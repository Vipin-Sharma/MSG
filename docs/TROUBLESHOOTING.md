# MSG Troubleshooting Guide

Common issues, solutions, and debugging techniques for MSG (Microservice Generator).

## Table of Contents

- [Common Issues & Solutions](#common-issues--solutions)
- [Database Connection Problems](#database-connection-problems)
- [Code Generation Issues](#code-generation-issues)
- [Generated Code Problems](#generated-code-problems)
- [Performance Issues](#performance-issues)
- [Debug Mode & Logging](#debug-mode--logging)
- [Environment-Specific Issues](#environment-specific-issues)

## Common Issues & Solutions

### 1. "SQL File Not Found" Error

**Error Message:**
```
Exception: SQL file not found: sample_parameterized_sql.sql
```

**Root Cause:**
SQL file is not in the expected location or has incorrect naming.

**Solutions:**

✅ **Check File Location**
```bash
# Files must be in src/main/resources/
ls -la src/main/resources/
```

✅ **Verify File Names**
Expected file names:
- `sample_parameterized_sql.sql` (SELECT)
- `sample_insert_parameterized.sql` (INSERT)
- `sample_update_parameterized.sql` (UPDATE)
- `sample_delete_parameterized.sql` (DELETE)

✅ **Create Missing File**
```bash
# Create the resources directory if it doesn't exist
mkdir -p src/main/resources

# Create a basic SELECT SQL file
cat > src/main/resources/sample_parameterized_sql.sql << 'EOF'
SELECT 
    customer_id,
    first_name,
    last_name,
    email
FROM customer 
WHERE active = ? 
  AND customer_id = ?
EOF
```

✅ **Specify Exact File Path**
```bash
mvn exec:java -Dexec.mainClass="com.jfeatures.msg.codegen.MicroServiceGenerator" \
  -Dexec.args="--name Customer --destination ./output --sql-file your_custom_file.sql"
```

### 2. Database Connection Failed

**Error Message:**
```
Exception: Connection refused: localhost:1433
Exception: Login failed for user 'sa'
```

**Root Causes:**
- SQL Server not running
- Incorrect credentials
- Port conflicts
- Network connectivity issues

**Solutions:**

✅ **Verify SQL Server is Running**
```bash
# Check if SQL Server container is running
docker ps | grep sqlserver

# Start SQL Server if not running
docker start msgdb

# Or restart with docker-compose
docker-compose up -d
```

✅ **Check Port Availability**
```bash
# Check if port 1433 is in use
netstat -an | grep 1433
lsof -i :1433

# If port is occupied, use different port
docker run -p 1434:1433 -e "ACCEPT_EULA=Y" -e "MSSQL_SA_PASSWORD=Password@1" ...
```

✅ **Test Database Connection**
```bash
# Test connection using sqlcmd (if available)
docker exec -it msgdb /opt/mssql-tools/bin/sqlcmd \
  -S localhost -U SA -P 'Password@1' \
  -Q "SELECT @@VERSION"

# Alternative: Use telnet to test port connectivity  
telnet localhost 1433
```

✅ **Check Docker Logs**
```bash
# View SQL Server startup logs
docker logs msgdb

# Follow logs in real-time
docker logs -f msgdb
```

✅ **Database Connection Configuration**
```properties
# Update application.properties with correct settings
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=sakila;encrypt=true;trustServerCertificate=true
spring.datasource.username=sa
spring.datasource.password=Password@1
spring.datasource.driver-class-name=com.microsoft.sqlserver.jdbc.SQLServerDriver
```

### 3. Parameter Count Mismatch

**Error Message:**
```
Exception: Parameter count mismatch. Expected 3 but got 2 parameters
Exception: Invalid parameter number
```

**Root Cause:**
Number of `?` placeholders in SQL doesn't match extracted parameters.

**Solutions:**

✅ **Count Parameters Manually**
```sql
-- This SQL has 3 parameters
SELECT customer_id, first_name, last_name 
FROM customer 
WHERE active = ?        -- Parameter 1
  AND store_id = ?      -- Parameter 2  
  AND customer_id = ?   -- Parameter 3
```

✅ **Verify SQL Syntax**
```sql
-- ❌ Bad: Missing parameter placeholder
UPDATE customer SET first_name = 'John' WHERE customer_id = ?

-- ✅ Good: All values parameterized
UPDATE customer SET first_name = ? WHERE customer_id = ?
```

✅ **Test SQL in Database**
```bash
# Test your SQL directly in SQL Server
docker exec -it msgdb /opt/mssql-tools/bin/sqlcmd -S localhost -U SA -P 'Password@1'

# Try your SQL with actual values first
SELECT customer_id, first_name FROM customer WHERE active = 'Y' AND customer_id = 1;

# Then convert to parameterized version
SELECT customer_id, first_name FROM customer WHERE active = ? AND customer_id = ?;
```

✅ **Debug Parameter Extraction**
Enable debug logging to see extracted parameters:
```bash
mvn -X exec:java -Dexec.mainClass="com.jfeatures.msg.codegen.MicroServiceGenerator" \
  -Dexec.args="--name Customer --destination ./output"
```

### 4. Compilation Errors in Generated Code

**Error Message:**
```
[ERROR] Generated code compilation failed
[ERROR] Cannot find symbol: class CustomerInsertDTO
```

**Root Causes:**
- Missing imports in generated code
- Type mapping errors
- Package name conflicts

**Solutions:**

✅ **Check Generated Project Structure**
```bash
cd ./output
find . -name "*.java" -type f
```

Expected structure:
```
output/
├── pom.xml
└── src/main/java/com/jfeatures/msg/customer/
    ├── Application.java
    ├── controller/CustomerController.java
    ├── dao/CustomerDAO.java
    ├── dto/CustomerDTO.java
    └── config/DatabaseConfig.java
```

✅ **Verify Generated Code Compiles**
```bash
cd ./output
mvn clean compile

# If compilation fails, check specific errors
mvn compile -X
```

✅ **Check for Type Mapping Issues**
Look for unsupported database types in generated code:
```java
// If you see 'Object' type instead of specific type
private Object unknownField; // ❌ Indicates type mapping problem

// Should be specific type
private String firstName;    // ✅ Correct type mapping
private Integer customerId;  // ✅ Correct type mapping
```

✅ **Regenerate with Clean Destination**
```bash
# Clean the output directory
rm -rf ./output

# Regenerate
mvn exec:java -Dexec.mainClass="com.jfeatures.msg.codegen.MicroServiceGenerator" \
  -Dexec.args="--name Customer --destination ./output"
```

### 5. Generated Service Won't Start

**Error Message:**
```
org.springframework.beans.factory.BeanCreationException
java.lang.ClassNotFoundException: com.microsoft.sqlserver.jdbc.SQLServerDriver
```

**Solutions:**

✅ **Check Dependencies in Generated pom.xml**
```xml
<!-- Ensure SQL Server driver is included -->
<dependency>
    <groupId>com.microsoft.sqlserver</groupId>
    <artifactId>mssql-jdbc</artifactId>
    <version>12.4.2.jre11</version>
</dependency>
```

✅ **Verify Database Configuration**
```properties
# Check application.properties in generated project
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=sakila;encrypt=true;trustServerCertificate=true
spring.datasource.username=sa  
spring.datasource.password=Password@1
spring.datasource.driver-class-name=com.microsoft.sqlserver.jdbc.SQLServerDriver
```

✅ **Test Database Connection**
```bash
cd ./output
mvn spring-boot:run

# If it fails, check logs for specific error
# Look for connection errors, port conflicts, etc.
```

## Database Connection Problems

### Docker-Related Issues

**Container Won't Start:**
```bash
# Check Docker daemon is running
docker version

# Check if port is already used
docker ps | grep 1433

# Remove existing container if corrupted
docker rm -f msgdb

# Start fresh container
docker run -e "ACCEPT_EULA=Y" -e "MSSQL_SA_PASSWORD=Password@1" \
           -p 1433:1433 -d --name msgdb \
           mcr.microsoft.com/mssql/server:2022-latest
```

**Container Starts But Connection Fails:**
```bash
# Wait for SQL Server to fully initialize (can take 30-60 seconds)
sleep 30

# Check container health
docker exec msgdb /opt/mssql-tools/bin/sqlcmd -S localhost -U SA -P 'Password@1' -Q "SELECT 1"

# Check if database exists
docker exec msgdb /opt/mssql-tools/bin/sqlcmd -S localhost -U SA -P 'Password@1' -Q "SELECT name FROM sys.databases"
```

### Network Configuration Issues

**Firewall/Port Problems:**
```bash
# Test network connectivity
telnet localhost 1433

# Check if Docker is exposing port correctly
docker port msgdb

# For Linux: Check firewall rules
sudo ufw status
sudo firewall-cmd --list-ports

# For Windows: Check Windows Firewall
# For macOS: Check if Docker Desktop has necessary permissions
```

### SSL/TLS Issues

**Certificate Errors:**
```bash
# If you get SSL errors, try with trustServerCertificate=true
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=sakila;encrypt=true;trustServerCertificate=true

# Or disable encryption for local development
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=sakila;encrypt=false
```

## Code Generation Issues

### SQL Parsing Problems

**Complex SQL Not Supported:**
```sql
-- ❌ Very complex queries might not work
WITH RECURSIVE complex_cte AS (
  SELECT * FROM table1
  UNION ALL
  SELECT * FROM table2 t2 JOIN complex_cte c ON t2.id = c.parent_id
)
SELECT * FROM complex_cte WHERE complex_condition = ?

-- ✅ Simplify for initial generation
SELECT t1.id, t1.name, t2.description
FROM table1 t1
JOIN table2 t2 ON t1.id = t2.table1_id
WHERE t1.active = ? AND t2.status = ?
```

**Unsupported SQL Features:**
- Dynamic SQL
- Temporary tables  
- Stored procedures
- Functions in WHERE clauses
- Complex CASE statements

### Metadata Extraction Failures

**Table/Column Not Found:**
```bash
# Verify table exists in database
docker exec msgdb /opt/mssql-tools/bin/sqlcmd -S localhost -U SA -P 'Password@1' -d sakila -Q "SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'customer'"

# Check column names
docker exec msgdb /opt/mssql-tools/bin/sqlcmd -S localhost -U SA -P 'Password@1' -d sakila -Q "SELECT COLUMN_NAME, DATA_TYPE FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'customer'"
```

**Type Mapping Issues:**
```java
// If you see unknown types, add to SQLServerDataTypeEnum
public enum SQLServerDataTypeEnum {
    // Add missing type mappings
    UNIQUEIDENTIFIER("uniqueidentifier", String.class),
    XML("xml", String.class),
    GEOGRAPHY("geography", String.class);
}
```

## Generated Code Problems

### Runtime Exceptions

**NullPointerException in Generated Code:**
```java
// Common cause: null parameter values
// Solution: Add null checks in generated DAOs
public int insertCustomer(CustomerInsertDTO request) {
    if (request == null) {
        throw new IllegalArgumentException("Request cannot be null");
    }
    
    // Additional null checks for required fields
    if (request.getFirstName() == null) {
        throw new IllegalArgumentException("firstName cannot be null");
    }
    
    // Continue with insertion logic
}
```

**SQL Syntax Errors:**
```sql
-- Check generated SQL for syntax issues
-- Common problems: missing spaces, incorrect quoting
-- Debug by logging the actual SQL being executed

-- Enable SQL logging in application.properties
logging.level.org.springframework.jdbc.core.JdbcTemplate=DEBUG
logging.level.org.springframework.jdbc.core.StatementCreatorUtils=TRACE
```

### Validation Errors

**Bean Validation Not Working:**
```xml
<!-- Ensure validation dependency is in generated pom.xml -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

```java
// Controller must use @Valid annotation
public ResponseEntity<String> createCustomer(@Valid @RequestBody CustomerInsertDTO request) {
    // Implementation
}
```

## Performance Issues

### Slow Code Generation

**Large SQL Files:**
```bash
# For large SQL files, increase JVM memory
export MAVEN_OPTS="-Xmx2g -Xms1g"
mvn exec:java -Dexec.mainClass="com.jfeatures.msg.codegen.MicroServiceGenerator" ...
```

**Many Columns/Parameters:**
```bash
# For tables with many columns (>50), generation might be slow
# Consider breaking into smaller, focused SQL queries
```

### Memory Issues

**OutOfMemoryError During Generation:**
```bash
# Increase heap size for Maven
export MAVEN_OPTS="-Xmx4g -Xms2g -XX:MetaspaceSize=512m"

# Or set in command
mvn -Dexec.jvmArgs="-Xmx4g" exec:java -Dexec.mainClass="com.jfeatures.msg.codegen.MicroServiceGenerator" ...
```

### Database Connection Exhaustion

**Too Many Connections:**
```properties
# Limit connection pool size in generated application.properties
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=2
spring.datasource.hikari.idle-timeout=300000
```

## Debug Mode & Logging

### Enable Verbose Logging

**Maven Debug Mode:**
```bash
# Enable Maven debug logging
mvn -X exec:java -Dexec.mainClass="com.jfeatures.msg.codegen.MicroServiceGenerator" \
  -Dexec.args="--name Customer --destination ./output"
```

**Application Debug Logging:**
```properties
# Add to generated application.properties
logging.level.com.jfeatures.msg=DEBUG
logging.level.org.springframework.jdbc=DEBUG
logging.level.org.springframework.web=DEBUG

# Enable SQL parameter logging
logging.level.org.springframework.jdbc.core.StatementCreatorUtils=TRACE
```

### Custom Logging Configuration

**logback-spring.xml for Generated Projects:**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/application.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>logs/application.%d{yyyy-MM-dd}.log</fileNamePattern>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <logger name="com.jfeatures.msg" level="DEBUG"/>
    <logger name="org.springframework.jdbc" level="DEBUG"/>
    
    <root level="INFO">
        <appender-ref ref="STDOUT"/>
        <appender-ref ref="FILE"/>
    </root>
</configuration>
```

### Debugging Generated Code

**Add Debug Endpoints:**
```java
// Add to generated controller for debugging
@GetMapping("/debug/info")
public ResponseEntity<Map<String, Object>> getDebugInfo() {
    Map<String, Object> info = new HashMap<>();
    info.put("timestamp", System.currentTimeMillis());
    info.put("database.url", dataSource.getConnection().getMetaData().getURL());
    info.put("active.profiles", environment.getActiveProfiles());
    return ResponseEntity.ok(info);
}
```

## Environment-Specific Issues

### Windows-Specific Problems

**Path Issues:**
```bash
# Use proper path separators
--destination "C:\Users\username\generated-services"

# Or use forward slashes (Java accepts both)
--destination "C:/Users/username/generated-services"
```

**Docker Desktop Issues:**
```bash
# Ensure Docker Desktop is running and has proper permissions
# Check WSL2 integration if using Windows with WSL

# Verify Docker is accessible
docker version
```

### macOS-Specific Problems

**Docker Desktop Permissions:**
```bash
# Ensure Docker Desktop has necessary permissions
# Go to System Preferences > Security & Privacy > Privacy > Full Disk Access
# Add Docker Desktop

# Check Docker is running
docker info
```

**Java Version Issues:**
```bash
# Verify Java 21 is installed and active
java --version
javac --version

# Set JAVA_HOME if necessary
export JAVA_HOME=/Library/Java/JavaVirtualMachines/openjdk-21.jdk/Contents/Home
```

### Linux-Specific Problems

**Docker Permissions:**
```bash
# Add user to docker group to avoid sudo
sudo usermod -aG docker $USER
newgrp docker

# Or run with sudo if needed
sudo docker run ...
```

**Firewall Issues:**
```bash
# Check if port 1433 is blocked
sudo ufw status
sudo iptables -L

# Allow port if needed
sudo ufw allow 1433
```

## Getting Additional Help

### Collect Debug Information

When reporting issues, include:

**System Information:**
```bash
java --version
mvn --version
docker --version
uname -a  # Linux/macOS
systeminfo  # Windows
```

**Project State:**
```bash
ls -la src/main/resources/
cat pom.xml | grep -A5 -B5 "version\|dependency"
```

**Error Details:**
- Complete error message and stack trace
- Command that caused the error
- SQL file contents
- Generated code (if relevant)

**Database State:**
```bash
docker ps -a | grep sql
docker logs msgdb | tail -20
```

### Community Support

- **GitHub Issues**: Report bugs and feature requests
- **Documentation**: Check latest documentation updates
- **Stack Overflow**: Tag questions with `msg-microservice-generator`

This troubleshooting guide covers the most common issues encountered when using MSG. If you encounter an issue not covered here, please create a GitHub issue with detailed information.