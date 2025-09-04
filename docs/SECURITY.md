# MSG Security Guide

Security considerations, known vulnerabilities, and best practices for MSG (Microservice Generator).

## Table of Contents

- [Critical Security Issues](#critical-security-issues)
- [Security Best Practices](#security-best-practices)
- [Generated Code Security](#generated-code-security)
- [Database Security](#database-security)
- [Vulnerability Assessment](#vulnerability-assessment)
- [Secure Configuration](#secure-configuration)
- [Security Monitoring](#security-monitoring)

## Critical Security Issues

⚠️ **IMMEDIATE ATTENTION REQUIRED** - These issues must be addressed before production use.

### 1. Hardcoded Database Credentials (CRITICAL)

**Location**: `src/main/java/com/jfeatures/msg/config/DataSourceConfig.java`

**Issue**: Database credentials exposed in source code
```java
// ❌ CRITICAL VULNERABILITY
spring.datasource.username=sa
spring.datasource.password=Password@1
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=sakila
```

**Impact**:
- Credentials exposed in version control
- Anyone with code access gains database access
- Potential data breach and unauthorized access

**Mitigation**:
```java
// ✅ Use environment variables
@Value("${DB_USERNAME:}")
private String dbUsername;

@Value("${DB_PASSWORD:}")  
private String dbPassword;

@Value("${DB_URL:}")
private String dbUrl;

@Bean
public DataSource dataSource() {
    if (dbUsername.isEmpty() || dbPassword.isEmpty()) {
        throw new IllegalStateException("Database credentials not configured");
    }
    
    HikariDataSource dataSource = new HikariDataSource();
    dataSource.setJdbcUrl(dbUrl);
    dataSource.setUsername(dbUsername);
    dataSource.setPassword(dbPassword);
    return dataSource;
}
```

**Environment Configuration**:
```bash
# .env file (never commit to version control)
DB_USERNAME=your_username
DB_PASSWORD=secure_password
DB_URL=jdbc:sqlserver://localhost:1433;databaseName=sakila;encrypt=true;trustServerCertificate=false

# Docker environment
docker run -e DB_USERNAME=user -e DB_PASSWORD=pass ...

# Kubernetes secret
apiVersion: v1
kind: Secret
metadata:
  name: db-credentials
type: Opaque
stringData:
  username: your_username
  password: secure_password
```

### 2. Resource Leak Vulnerability (CRITICAL)

**Location**: `src/main/java/com/jfeatures/msg/sql/ReadFileFromResources.java`

**Issue**: Potential NPE if resource file not found
```java
// ❌ VULNERABLE CODE
public static String readFileContent(String fileName) throws Exception {
    InputStream inputStream = ReadFileFromResources.class.getResourceAsStream("/" + fileName);
    return new String(inputStream.readAllBytes()); // NPE if inputStream is null
}
```

**Impact**:
- Application crashes with NullPointerException
- Potential denial of service
- Information disclosure through stack traces

**Mitigation**:
```java
// ✅ SECURE IMPLEMENTATION
public static String readFileContent(String fileName) throws Exception {
    try (InputStream inputStream = ReadFileFromResources.class.getResourceAsStream("/" + fileName)) {
        if (inputStream == null) {
            throw new FileNotFoundException("Resource file not found: " + fileName);
        }
        return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException e) {
        throw new Exception("Failed to read resource file: " + fileName, e);
    }
}
```

### 3. SQL Injection Risk (HIGH)

**Location**: `src/main/java/com/jfeatures/msg/codegen/dbmetadata/SqlMetadata.java`

**Issue**: Direct query execution without validation
```java
// ❌ POTENTIAL SQL INJECTION
String sql = userProvidedSql; // Could contain malicious SQL
PreparedStatement statement = connection.prepareStatement(sql);
ResultSet resultSet = statement.executeQuery();
```

**Impact**:
- Unauthorized data access
- Data manipulation or deletion
- Potential system compromise

**Mitigation**:
```java
// ✅ INPUT VALIDATION AND SANITIZATION
public class SqlValidator {
    private static final Set<String> ALLOWED_STATEMENTS = Set.of(
        "SELECT", "INSERT", "UPDATE", "DELETE"
    );
    
    private static final Pattern DANGEROUS_PATTERNS = Pattern.compile(
        "(?i)(;\\s*(DROP|CREATE|ALTER|TRUNCATE|EXEC|EXECUTE))|" +
        "(--)|" +
        "(/\\*.*\\*/)|" +
        "(xp_|sp_)"
    );
    
    public static void validateSql(String sql) throws SecurityException {
        if (sql == null || sql.trim().isEmpty()) {
            throw new SecurityException("SQL statement cannot be null or empty");
        }
        
        // Check for dangerous patterns
        if (DANGEROUS_PATTERNS.matcher(sql).find()) {
            throw new SecurityException("SQL statement contains potentially dangerous patterns");
        }
        
        // Validate statement type
        String trimmedSql = sql.trim().toUpperCase();
        boolean isAllowed = ALLOWED_STATEMENTS.stream()
            .anyMatch(stmt -> trimmedSql.startsWith(stmt));
            
        if (!isAllowed) {
            throw new SecurityException("SQL statement type not allowed");
        }
    }
}

// Usage in SqlMetadata
public List<ColumnMetadata> extractMetadata(String sql) throws Exception {
    SqlValidator.validateSql(sql); // Validate before execution
    
    try (PreparedStatement statement = connection.prepareStatement(sql)) {
        // Safe to execute validated SQL
        ResultSet resultSet = statement.executeQuery();
        return extractColumnMetadata(resultSet);
    }
}
```

### 4. Directory Traversal Vulnerability (HIGH)

**Location**: `src/main/java/com/jfeatures/msg/codegen/filesystem/MicroserviceDirectoryCleaner.java`

**Issue**: No path validation for destination directories
```java
// ❌ VULNERABLE TO PATH TRAVERSAL
public static void cleanDirectory(String destinationPath) {
    File directory = new File(destinationPath); // Could be ../../../etc/passwd
    deleteRecursively(directory);
}
```

**Impact**:
- Unauthorized file system access
- Deletion of system files
- Potential system compromise

**Mitigation**:
```java
// ✅ PATH VALIDATION AND SANDBOXING
public class PathValidator {
    private static final String ALLOWED_BASE_PATH = System.getProperty("user.home") + "/generated-services";
    
    public static Path validateAndNormalizePath(String userPath) throws SecurityException {
        try {
            Path basePath = Paths.get(ALLOWED_BASE_PATH).toAbsolutePath().normalize();
            Path targetPath = Paths.get(userPath).toAbsolutePath().normalize();
            
            // Ensure path is within allowed directory
            if (!targetPath.startsWith(basePath)) {
                throw new SecurityException("Path outside allowed directory: " + userPath);
            }
            
            // Additional validation
            if (targetPath.toString().contains("..")) {
                throw new SecurityException("Path contains directory traversal: " + userPath);
            }
            
            return targetPath;
            
        } catch (InvalidPathException e) {
            throw new SecurityException("Invalid path format: " + userPath, e);
        }
    }
}

// Usage in MicroserviceDirectoryCleaner
public static void cleanDirectory(String destinationPath) throws SecurityException {
    Path validatedPath = PathValidator.validateAndNormalizePath(destinationPath);
    File directory = validatedPath.toFile();
    
    // Safe to proceed with validated path
    if (directory.exists()) {
        deleteRecursively(directory);
    }
}
```

## Security Best Practices

### 1. Input Validation

**Validate all external inputs**:
```java
public class InputValidator {
    public static void validateBusinessName(String businessName) {
        if (businessName == null || businessName.trim().isEmpty()) {
            throw new IllegalArgumentException("Business name cannot be null or empty");
        }
        
        // Validate format (alphanumeric, start with letter)
        if (!businessName.matches("^[A-Za-z][A-Za-z0-9]*$")) {
            throw new IllegalArgumentException("Business name must be alphanumeric and start with letter");
        }
        
        // Length validation
        if (businessName.length() > 50) {
            throw new IllegalArgumentException("Business name too long (max 50 characters)");
        }
    }
    
    public static void validateSqlFileName(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new IllegalArgumentException("SQL file name cannot be null or empty");
        }
        
        // Only allow .sql files in resources
        if (!fileName.matches("^[a-zA-Z0-9_-]+\\.sql$")) {
            throw new IllegalArgumentException("Invalid SQL file name format");
        }
    }
}
```

### 2. Secure File Handling

**Safe file operations**:
```java
public class SecureFileOperations {
    private static final long MAX_FILE_SIZE = 1024 * 1024; // 1MB limit
    
    public static String readSqlFile(String fileName) throws Exception {
        InputValidator.validateSqlFileName(fileName);
        
        try (InputStream inputStream = SecureFileOperations.class
                .getResourceAsStream("/sql/" + fileName)) {
            
            if (inputStream == null) {
                throw new FileNotFoundException("SQL file not found: " + fileName);
            }
            
            // Size validation to prevent DoS
            byte[] content = inputStream.readNBytes((int) MAX_FILE_SIZE + 1);
            if (content.length > MAX_FILE_SIZE) {
                throw new SecurityException("SQL file too large (max 1MB)");
            }
            
            return new String(content, StandardCharsets.UTF_8);
        }
    }
}
```

### 3. Connection Security

**Secure database connections**:
```java
@Configuration
public class SecureDatabaseConfig {
    
    @Bean
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        
        // Connection URL with security settings
        config.setJdbcUrl(dbUrl + 
            ";encrypt=true" +                    // Force encryption
            ";trustServerCertificate=false" +   // Validate certificates
            ";integratedSecurity=false" +       // Explicit authentication
            ";loginTimeout=30" +                // Connection timeout
            ";socketTimeout=30"                 // Socket timeout
        );
        
        // Connection pool security
        config.setMaximumPoolSize(20);          // Limit connections
        config.setMinimumIdle(2);               // Minimum pool size
        config.setIdleTimeout(300000);          // 5 minutes idle timeout
        config.setMaxLifetime(1800000);         // 30 minutes max lifetime
        config.setLeakDetectionThreshold(60000); // Detect connection leaks
        
        return new HikariDataSource(config);
    }
}
```

## Generated Code Security

### 1. Input Validation in Generated DTOs

MSG generates secure DTOs with proper validation:

```java
// Generated DTO with security annotations
@Builder
@Value
@Jacksonized
public class CustomerInsertDTO {
    
    @NotNull(message = "firstName is required")
    @Size(min = 1, max = 50, message = "firstName must be 1-50 characters")
    @Pattern(regexp = "^[A-Za-z\\s'-]+$", message = "firstName contains invalid characters")
    private String firstName;
    
    @Email(message = "Invalid email format")
    @Size(max = 100, message = "email must not exceed 100 characters")
    private String email;
    
    @Min(value = 1, message = "addressId must be positive")
    @Max(value = Integer.MAX_VALUE, message = "addressId too large")
    private Integer addressId;
}
```

### 2. Secure SQL Generation

Generated DAOs use parameterized queries:

```java
// Generated DAO with secure SQL
@Component
public class CustomerInsertDAO {
    
    private static final String SQL = """
        INSERT INTO customer (
            first_name,
            last_name,
            email,
            address_id
        ) VALUES (
            :firstName,
            :lastName,
            :email,
            :addressId
        )""";
    
    public int insertCustomer(CustomerInsertDTO request) {
        // Input validation at DAO level
        validateInsertRequest(request);
        
        Map<String, Object> params = new HashMap<>();
        params.put("firstName", sanitizeInput(request.getFirstName()));
        params.put("lastName", sanitizeInput(request.getLastName()));
        params.put("email", sanitizeEmail(request.getEmail()));
        params.put("addressId", request.getAddressId());
        
        return namedParameterJdbcTemplate.update(SQL, params);
    }
    
    private void validateInsertRequest(CustomerInsertDTO request) {
        if (request == null) {
            throw new IllegalArgumentException("Insert request cannot be null");
        }
        // Additional business logic validation
    }
    
    private String sanitizeInput(String input) {
        if (input == null) return null;
        return input.trim().replaceAll("[<>\"'&]", ""); // Basic XSS prevention
    }
}
```

### 3. Secure Controllers

Generated controllers include security headers and validation:

```java
@RestController
@RequestMapping(path = "/api")
@Validated
public class CustomerController {
    
    @PostMapping(value = "/customer", 
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('USER')") // If using Spring Security
    public ResponseEntity<String> createCustomer(
            @Valid @RequestBody CustomerInsertDTO request,
            HttpServletRequest httpRequest) {
        
        // Security logging
        log.info("Customer creation requested from IP: {}", 
                getClientIpAddress(httpRequest));
        
        try {
            int rowsAffected = customerInsertDAO.insertCustomer(request);
            if (rowsAffected > 0) {
                return ResponseEntity.status(HttpStatus.CREATED)
                    .header("X-Content-Type-Options", "nosniff")
                    .header("X-Frame-Options", "DENY")
                    .header("X-XSS-Protection", "1; mode=block")
                    .body("{\"message\":\"Customer created successfully\"}");
            } else {
                log.warn("Customer creation failed - no rows affected");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Failed to create customer\"}");
            }
        } catch (Exception e) {
            log.error("Customer creation error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"error\":\"Internal server error\"}");
        }
    }
    
    private String getClientIpAddress(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        return xff != null ? xff.split(",")[0] : request.getRemoteAddr();
    }
}
```

## Database Security

### 1. Principle of Least Privilege

Create dedicated database users with minimal permissions:

```sql
-- Create application user with limited permissions
CREATE LOGIN msg_app WITH PASSWORD = 'SecureRandomPassword123!';
CREATE USER msg_app FOR LOGIN msg_app;

-- Grant only necessary permissions
GRANT SELECT, INSERT, UPDATE, DELETE ON customer TO msg_app;
GRANT SELECT, INSERT, UPDATE, DELETE ON address TO msg_app;
-- Do not grant DDL permissions (CREATE, DROP, ALTER)

-- Deny dangerous permissions
DENY EXECUTE TO msg_app; -- Prevent stored procedure execution
DENY CREATE TABLE TO msg_app;
DENY DROP TO msg_app;
```

### 2. Connection String Security

**Secure connection configuration**:
```properties
# Production configuration
spring.datasource.url=jdbc:sqlserver://${DB_HOST}:${DB_PORT};databaseName=${DB_NAME};encrypt=true;trustServerCertificate=false;hostNameInCertificate=${DB_HOST};
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

# Connection pool security
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=2
spring.datasource.hikari.idle-timeout=300000
spring.datasource.hikari.max-lifetime=1800000
spring.datasource.hikari.leak-detection-threshold=60000
```

### 3. Data Encryption

**Encrypt sensitive data at rest**:
```sql
-- Enable Transparent Data Encryption (TDE) - SQL Server
ALTER DATABASE sakila SET ENCRYPTION ON;

-- Column-level encryption for sensitive fields
CREATE TABLE customer (
    customer_id INT IDENTITY(1,1) PRIMARY KEY,
    first_name NVARCHAR(50),
    last_name NVARCHAR(50),
    email VARBINARY(MAX), -- Encrypted email
    ssn VARBINARY(MAX),   -- Encrypted SSN
    created_date DATETIME2 DEFAULT GETDATE()
);
```

## Vulnerability Assessment

### 1. Regular Security Scanning

**Dependency vulnerability scanning**:
```bash
# Maven dependency check
mvn dependency-check:check

# OWASP Dependency Check
mvn org.owasp:dependency-check-maven:check

# Snyk vulnerability scanning
snyk test

# GitHub Security Advisories
# (automatically scans dependencies in GitHub repositories)
```

**Example pom.xml security configuration**:
```xml
<plugin>
    <groupId>org.owasp</groupId>
    <artifactId>dependency-check-maven</artifactId>
    <version>8.4.2</version>
    <configuration>
        <failBuildOnCVSS>7</failBuildOnCVSS>
        <suppressionFile>suppress-false-positives.xml</suppressionFile>
    </configuration>
    <executions>
        <execution>
            <goals>
                <goal>check</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

### 2. Code Security Analysis

**Static Application Security Testing (SAST)**:
```bash
# SpotBugs security rules
mvn spotbugs:check -Dspotbugs.includeTests=true

# SonarQube security analysis
mvn sonar:sonar -Dsonar.projectKey=MSG -Dsonar.security.hotspots=true

# Checkmarx or Veracode scanning (enterprise tools)
```

### 3. Generated Code Security Review

**Security checklist for generated code**:
- [ ] All inputs validated with appropriate annotations
- [ ] SQL parameters properly bound (no string concatenation)
- [ ] Error messages don't reveal sensitive information
- [ ] Proper HTTP security headers included
- [ ] Authentication/authorization checks present
- [ ] Logging includes security events
- [ ] No sensitive data in logs

## Secure Configuration

### 1. Production Configuration

**Secure application.properties**:
```properties
# Server configuration
server.port=8443
server.ssl.enabled=true
server.ssl.key-store=classpath:keystore.p12
server.ssl.key-store-password=${SSL_KEYSTORE_PASSWORD}
server.ssl.key-store-type=PKCS12
server.ssl.key-alias=msg

# Security headers
server.servlet.session.cookie.secure=true
server.servlet.session.cookie.http-only=true
server.servlet.session.cookie.same-site=strict

# Database security
spring.datasource.url=jdbc:sqlserver://${DB_HOST}:${DB_PORT};databaseName=${DB_NAME};encrypt=true;trustServerCertificate=false
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

# Logging security
logging.level.org.springframework.security=DEBUG
logging.level.org.springframework.jdbc=WARN
logging.pattern.console=%d{HH:mm:ss.SSS} [%thread] %-5level [%X{requestId}] %logger{36} - %msg%n

# Disable unnecessary endpoints
management.endpoints.web.exposure.include=health,info
management.endpoint.health.show-details=when_authorized
```

### 2. Docker Security

**Secure Dockerfile**:
```dockerfile
FROM openjdk:21-jre-slim

# Create non-root user
RUN groupadd -r msgapp && useradd -r -g msgapp msgapp

# Copy application
COPY target/msg-*.jar app.jar

# Set ownership and permissions
RUN chown msgapp:msgapp app.jar && chmod 500 app.jar

# Switch to non-root user
USER msgapp

# Security settings
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

# Run application
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### 3. Network Security

**Docker Compose with network isolation**:
```yaml
version: '3.8'
services:
  app:
    build: .
    ports:
      - "8443:8443"
    networks:
      - app-network
    environment:
      - DB_HOST=database
      - DB_USERNAME_FILE=/run/secrets/db_username
      - DB_PASSWORD_FILE=/run/secrets/db_password
    secrets:
      - db_username
      - db_password
    
  database:
    image: mcr.microsoft.com/mssql/server:2022-latest
    networks:
      - app-network
    environment:
      - ACCEPT_EULA=Y
      - MSSQL_SA_PASSWORD_FILE=/run/secrets/sa_password
    secrets:
      - sa_password

networks:
  app-network:
    driver: bridge
    internal: true

secrets:
  db_username:
    external: true
  db_password:
    external: true
  sa_password:
    external: true
```

## Security Monitoring

### 1. Security Logging

**Comprehensive security logging**:
```java
@Component
public class SecurityLogger {
    private static final Logger securityLog = LoggerFactory.getLogger("SECURITY");
    
    public void logLoginAttempt(String username, String ipAddress, boolean success) {
        if (success) {
            securityLog.info("LOGIN_SUCCESS: user={}, ip={}", username, ipAddress);
        } else {
            securityLog.warn("LOGIN_FAILURE: user={}, ip={}", username, ipAddress);
        }
    }
    
    public void logDataAccess(String operation, String table, String user, String ipAddress) {
        securityLog.info("DATA_ACCESS: operation={}, table={}, user={}, ip={}", 
                        operation, table, user, ipAddress);
    }
    
    public void logSecurityViolation(String violation, String details, String ipAddress) {
        securityLog.error("SECURITY_VIOLATION: type={}, details={}, ip={}", 
                         violation, details, ipAddress);
    }
}
```

### 2. Intrusion Detection

**Rate limiting and anomaly detection**:
```java
@Component
public class SecurityMonitor {
    private final Map<String, AtomicInteger> requestCounts = new ConcurrentHashMap<>();
    private final Map<String, Long> lastRequestTime = new ConcurrentHashMap<>();
    
    public boolean isRateLimited(String clientIp) {
        long currentTime = System.currentTimeMillis();
        long lastTime = lastRequestTime.getOrDefault(clientIp, 0L);
        
        // Reset counter every minute
        if (currentTime - lastTime > 60000) {
            requestCounts.put(clientIp, new AtomicInteger(0));
            lastRequestTime.put(clientIp, currentTime);
        }
        
        int count = requestCounts.computeIfAbsent(clientIp, k -> new AtomicInteger(0))
                                .incrementAndGet();
        
        // Limit: 100 requests per minute
        if (count > 100) {
            log.warn("Rate limit exceeded for IP: {}", clientIp);
            return true;
        }
        
        return false;
    }
}
```

### 3. Security Alerts

**Automated security alerting**:
```java
@EventListener
public class SecurityEventHandler {
    
    @Async
    @EventListener
    public void handleSecurityViolation(SecurityViolationEvent event) {
        // Send alert to security team
        sendSecurityAlert(event);
        
        // Log to SIEM system
        logToSiem(event);
        
        // Auto-block if critical
        if (event.getSeverity() == Severity.CRITICAL) {
            blockIpAddress(event.getSourceIp());
        }
    }
    
    private void sendSecurityAlert(SecurityViolationEvent event) {
        // Implementation: Email, Slack, PagerDuty integration
    }
}
```

This security guide provides comprehensive coverage of security considerations for MSG. Regular review and updates of these security measures are essential for maintaining a secure system.