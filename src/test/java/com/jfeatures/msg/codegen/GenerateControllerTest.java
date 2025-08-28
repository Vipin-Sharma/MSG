package com.jfeatures.msg.codegen;

import com.jfeatures.msg.codegen.domain.DBColumn;
import com.squareup.javapoet.JavaFile;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GenerateControllerTest {

    @Test
    void shouldGenerateBasicController() throws Exception {
        // Given
        List<DBColumn> predicateLiterals = Arrays.asList(
            new DBColumn("customer", "customerId", "java.lang.Integer", "INTEGER")
        );

        // When
        JavaFile result = GenerateController.createController("Customer", predicateLiterals);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.packageName).isEqualTo("com.jfeatures.msg.customer.controller");
        assertThat(result.typeSpec.name).isEqualTo("CustomerController");
        
        String generatedCode = result.toString();
        assertThat(generatedCode).contains("@RestController");
        assertThat(generatedCode).contains("@RequestMapping(path = \"/api\")");
        assertThat(generatedCode).contains("@Tag(name = \"Customer\", description = \"Customer\")");
        assertThat(generatedCode).contains("getDataForCustomer");
        assertThat(generatedCode).contains("@GetMapping(value = \"/Customer\", produces = \"application/json\")");
    }

    @Test
    void shouldGenerateControllerWithMultipleParameters() throws Exception {
        // Given
        List<DBColumn> predicateLiterals = Arrays.asList(
            new DBColumn("orders", "customerId", "java.lang.Integer", "INTEGER"),
            new DBColumn("orders", "status", "java.lang.String", "VARCHAR"),
            new DBColumn("orders", "minAmount", "java.math.BigDecimal", "DECIMAL")
        );

        // When
        JavaFile result = GenerateController.createController("Order", predicateLiterals);

        // Then
        String generatedCode = result.toString();
        assertThat(generatedCode).contains("@RequestParam(value = \"customerId\") Integer customerId");
        assertThat(generatedCode).contains("@RequestParam(value = \"status\") String status");
        assertThat(generatedCode).contains("@RequestParam(value = \"minAmount\") BigDecimal minAmount");
        assertThat(generatedCode).contains("return orderDAO.getOrder(customerId, status, minAmount)");
    }

    @Test
    void shouldGenerateControllerWithNoParameters() throws Exception {
        // Given
        List<DBColumn> emptyPredicateLiterals = Arrays.asList();

        // When
        JavaFile result = GenerateController.createController("AllCustomers", emptyPredicateLiterals);

        // Then
        String generatedCode = result.toString();
        assertThat(generatedCode).contains("getDataForAllCustomers()");
        assertThat(generatedCode).contains("return allCustomersDAO.getAllCustomers()");
        assertThat(generatedCode).contains("List<AllCustomersDTO>");
    }

    @Test
    void shouldGenerateControllerWithSwaggerAnnotations() throws Exception {
        // Given
        List<DBColumn> predicateLiterals = Arrays.asList(
            new DBColumn("product", "productId", "java.lang.Long", "BIGINT")
        );

        // When
        JavaFile result = GenerateController.createController("Product", predicateLiterals);

        // Then
        String generatedCode = result.toString();
        assertThat(generatedCode).contains("@Operation(summary = \"Get API to fetch data for Product\")");
        assertThat(generatedCode).contains("@Tag(name = \"Product\", description = \"Product\")");
    }

    @Test
    void shouldHandleComplexBusinessPurpose() throws Exception {
        // Given
        List<DBColumn> predicateLiterals = Arrays.asList(
            new DBColumn("sales_report", "year", "java.lang.Integer", "INTEGER"),
            new DBColumn("sales_report", "quarter", "java.lang.Integer", "INTEGER")
        );

        // When
        JavaFile result = GenerateController.createController("QuarterlySalesReport", predicateLiterals);

        // Then
        assertThat(result.typeSpec.name).isEqualTo("QuarterlySalesReportController");
        assertThat(result.packageName).isEqualTo("com.jfeatures.msg.quarterlysalesreport.controller");
        
        String generatedCode = result.toString();
        assertThat(generatedCode).contains("getDataForQuarterlySalesReport");
        assertThat(generatedCode).contains("quarterlySalesReportDAO.getQuarterlySalesReport");
        assertThat(generatedCode).contains("List<QuarterlySalesReportDTO>");
    }

    @Test
    void shouldGenerateConstructorAndDependencyInjection() throws Exception {
        // Given
        List<DBColumn> predicateLiterals = Arrays.asList(
            new DBColumn("user", "id", "java.lang.Integer", "INTEGER")
        );

        // When
        JavaFile result = GenerateController.createController("User", predicateLiterals);

        // Then
        String generatedCode = result.toString();
        assertThat(generatedCode).contains("private final UserDAO userDAO");
        assertThat(generatedCode).contains("UserController(UserDAO userDAO)");
        assertThat(generatedCode).contains("this.userDAO = userDAO");
    }

    @Test
    void shouldHandleSnakeCaseParameterNames() throws Exception {
        // Given
        List<DBColumn> predicateLiterals = Arrays.asList(
            new DBColumn("user_activity", "user_id", "java.lang.Integer", "INTEGER"),
            new DBColumn("user_activity", "created_date", "java.sql.Date", "DATE")
        );

        // When
        JavaFile result = GenerateController.createController("UserActivity", predicateLiterals);

        // Then
        String generatedCode = result.toString();
        assertThat(generatedCode).contains("@RequestParam(value = \"userId\") Integer userId");
        assertThat(generatedCode).contains("@RequestParam(value = \"createdDate\") Date createdDate");
        assertThat(generatedCode).contains("return userActivityDAO.getUserActivity(userId, createdDate)");
    }

    @Test
    void shouldGenerateCorrectReturnType() throws Exception {
        // Given
        List<DBColumn> predicateLiterals = Arrays.asList();

        // When
        JavaFile result = GenerateController.createController("Report", predicateLiterals);

        // Then
        String generatedCode = result.toString();
        assertThat(generatedCode).contains("List<ReportDTO> getDataForReport()");
    }
}