package com.jfeatures.msg.codegen;

import static org.assertj.core.api.Assertions.assertThat;

import com.jfeatures.msg.codegen.domain.DBColumn;
import com.squareup.javapoet.JavaFile;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

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
        assertThat(result)
            .isNotNull()
            .returns("com.jfeatures.msg.customer.controller", javaFile -> javaFile.packageName)
            .returns("CustomerController", javaFile -> javaFile.typeSpec.name);
        
        String generatedCode = result.toString();
        assertThat(generatedCode)
            .contains("@RestController")
            .contains("path = \"/api\"")
            .contains("name = \"Customer\"")
            .contains("description = \"Customer\"")
            .contains("getDataForCustomer")
            .contains("value = \"/Customer\"")
            .contains("produces = \"application/json\"");
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
        assertThat(generatedCode)
            .contains("@RequestParam(\"customerid\") Integer customerid")
            .contains("@RequestParam(\"status\") String status")
            .contains("@RequestParam(\"minamount\") BigDecimal minamount")
            .contains("return orderDAO.getOrder(customerid, status, minamount)");
    }

    @Test
    void shouldGenerateControllerWithNoParameters() throws Exception {
        // Given
        List<DBColumn> emptyPredicateLiterals = Arrays.asList();

        // When
        JavaFile result = GenerateController.createController("AllCustomers", emptyPredicateLiterals);

        // Then
        String generatedCode = result.toString();
        assertThat(generatedCode)
            .contains("getDataForAllCustomers()")
            .contains("return allcustomersDAO.getAllCustomers()")
            .contains("List<AllCustomersDTO>");
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
        assertThat(generatedCode)
            .contains("summary = \"Get API to fetch data for Product\"")
            .contains("name = \"Product\"")
            .contains("description = \"Product\"");
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
        assertThat(result)
            .returns("QuarterlySalesReportController", javaFile -> javaFile.typeSpec.name)
            .returns("com.jfeatures.msg.quarterlysalesreport.controller", javaFile -> javaFile.packageName);
        
        String generatedCode = result.toString();
        assertThat(generatedCode)
            .contains("getDataForQuarterlySalesReport")
            .contains("quarterlysalesreportDAO.getQuarterlySalesReport")
            .contains("List<QuarterlySalesReportDTO>");
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
        assertThat(generatedCode)
            .contains("private final UserDAO userDAO")
            .contains("UserController(UserDAO userDAO)")
            .contains("this.userDAO = userDAO");
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
        assertThat(generatedCode)
            .contains("@RequestParam(\"user_id\") Integer user_id")
            .contains("@RequestParam(\"created_date\") Date created_date")
            .contains("return useractivityDAO.getUserActivity(user_id, created_date)");
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