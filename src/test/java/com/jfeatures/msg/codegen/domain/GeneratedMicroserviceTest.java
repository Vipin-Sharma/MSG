package com.jfeatures.msg.codegen.domain;

import com.jfeatures.msg.codegen.util.SqlStatementType;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link GeneratedMicroservice}.
 */
class GeneratedMicroserviceTest {

    @Test
    void shouldCreateValidGeneratedMicroservice() {
        // given
        String businessName = "Customer";
        JavaFile springBootApp = createMockJavaFile("CustomerApplication");
        JavaFile dto = createMockJavaFile("CustomerDTO");
        JavaFile controller = createMockJavaFile("CustomerController");
        JavaFile dao = createMockJavaFile("CustomerDAO");
        String dbConfig = "database config content";
        SqlStatementType statementType = SqlStatementType.SELECT;

        // when
        GeneratedMicroservice microservice = new GeneratedMicroservice(
            businessName, springBootApp, dto, controller, dao, dbConfig, statementType
        );

        // then
        assertThat(microservice.businessDomainName()).isEqualTo(businessName);
        assertThat(microservice.springBootApplication()).isEqualTo(springBootApp);
        assertThat(microservice.dtoFile()).isEqualTo(dto);
        assertThat(microservice.controllerFile()).isEqualTo(controller);
        assertThat(microservice.daoFile()).isEqualTo(dao);
        assertThat(microservice.databaseConfigContent()).isEqualTo(dbConfig);
        assertThat(microservice.statementType()).isEqualTo(statementType);
    }

    @Test
    void shouldThrowExceptionWhenBusinessDomainNameIsNull() {
        assertThatThrownBy(() -> new GeneratedMicroservice(
            null,
            createMockJavaFile("App"),
            createMockJavaFile("DTO"),
            createMockJavaFile("Controller"),
            createMockJavaFile("DAO"),
            "config",
            SqlStatementType.SELECT
        ))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Business domain name cannot be null or empty");
    }

    @Test
    void shouldThrowExceptionWhenBusinessDomainNameIsEmpty() {
        assertThatThrownBy(() -> new GeneratedMicroservice(
            "",
            createMockJavaFile("App"),
            createMockJavaFile("DTO"),
            createMockJavaFile("Controller"),
            createMockJavaFile("DAO"),
            "config",
            SqlStatementType.SELECT
        ))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Business domain name cannot be null or empty");
    }

    @Test
    void shouldThrowExceptionWhenBusinessDomainNameIsWhitespace() {
        assertThatThrownBy(() -> new GeneratedMicroservice(
            "   ",
            createMockJavaFile("App"),
            createMockJavaFile("DTO"),
            createMockJavaFile("Controller"),
            createMockJavaFile("DAO"),
            "config",
            SqlStatementType.SELECT
        ))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Business domain name cannot be null or empty");
    }

    @Test
    void shouldThrowExceptionWhenSpringBootApplicationIsNull() {
        assertThatThrownBy(() -> new GeneratedMicroservice(
            "Customer",
            null,
            createMockJavaFile("DTO"),
            createMockJavaFile("Controller"),
            createMockJavaFile("DAO"),
            "config",
            SqlStatementType.SELECT
        ))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Spring Boot application file cannot be null");
    }

    @Test
    void shouldThrowExceptionWhenDtoFileIsNull() {
        assertThatThrownBy(() -> new GeneratedMicroservice(
            "Customer",
            createMockJavaFile("App"),
            null,
            createMockJavaFile("Controller"),
            createMockJavaFile("DAO"),
            "config",
            SqlStatementType.SELECT
        ))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("DTO file cannot be null");
    }

    @Test
    void shouldThrowExceptionWhenControllerFileIsNull() {
        assertThatThrownBy(() -> new GeneratedMicroservice(
            "Customer",
            createMockJavaFile("App"),
            createMockJavaFile("DTO"),
            null,
            createMockJavaFile("DAO"),
            "config",
            SqlStatementType.SELECT
        ))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Controller file cannot be null");
    }

    @Test
    void shouldThrowExceptionWhenDaoFileIsNull() {
        assertThatThrownBy(() -> new GeneratedMicroservice(
            "Customer",
            createMockJavaFile("App"),
            createMockJavaFile("DTO"),
            createMockJavaFile("Controller"),
            null,
            "config",
            SqlStatementType.SELECT
        ))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("DAO file cannot be null");
    }

    @Test
    void shouldThrowExceptionWhenDatabaseConfigContentIsNull() {
        assertThatThrownBy(() -> new GeneratedMicroservice(
            "Customer",
            createMockJavaFile("App"),
            createMockJavaFile("DTO"),
            createMockJavaFile("Controller"),
            createMockJavaFile("DAO"),
            null,
            SqlStatementType.SELECT
        ))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Database config content cannot be null or empty");
    }

    @Test
    void shouldThrowExceptionWhenDatabaseConfigContentIsEmpty() {
        assertThatThrownBy(() -> new GeneratedMicroservice(
            "Customer",
            createMockJavaFile("App"),
            createMockJavaFile("DTO"),
            createMockJavaFile("Controller"),
            createMockJavaFile("DAO"),
            "",
            SqlStatementType.SELECT
        ))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Database config content cannot be null or empty");
    }

    @Test
    void shouldThrowExceptionWhenDatabaseConfigContentIsWhitespace() {
        assertThatThrownBy(() -> new GeneratedMicroservice(
            "Customer",
            createMockJavaFile("App"),
            createMockJavaFile("DTO"),
            createMockJavaFile("Controller"),
            createMockJavaFile("DAO"),
            "   ",
            SqlStatementType.SELECT
        ))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Database config content cannot be null or empty");
    }

    @Test
    void shouldThrowExceptionWhenStatementTypeIsNull() {
        assertThatThrownBy(() -> new GeneratedMicroservice(
            "Customer",
            createMockJavaFile("App"),
            createMockJavaFile("DTO"),
            createMockJavaFile("Controller"),
            createMockJavaFile("DAO"),
            "config",
            null
        ))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("SQL statement type cannot be null");
    }

    private JavaFile createMockJavaFile(String className) {
        TypeSpec typeSpec = TypeSpec.classBuilder(className).build();
        return JavaFile.builder("com.jfeatures.test", typeSpec).build();
    }
}
