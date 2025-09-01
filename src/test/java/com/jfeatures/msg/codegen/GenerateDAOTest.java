package com.jfeatures.msg.codegen;

import com.jfeatures.msg.codegen.dbmetadata.ColumnMetadata;
import com.jfeatures.msg.codegen.domain.DBColumn;
import com.jfeatures.msg.test.TestUtils;
import com.squareup.javapoet.JavaFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GenerateDAOTest {

    @Test
    void shouldGenerateBasicDAO() {
        // Given
        List<ColumnMetadata> columnMetadata = Arrays.asList(
            TestUtils.createColumnMetadata("id", "INT", java.sql.Types.INTEGER, false),
            TestUtils.createColumnMetadata("name", "VARCHAR", java.sql.Types.VARCHAR, true)
        );
        List<DBColumn> predicateLiterals = Arrays.asList(
            new DBColumn("customer", "customerId", "java.lang.Integer", "INTEGER")
        );
        String sql = "SELECT id, name FROM customer WHERE customer_id = ?";

        // When
        JavaFile result = GenerateDAO.createDaoFromMetadata("Customer", columnMetadata, predicateLiterals, sql);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.packageName).isEqualTo("com.jfeatures.msg.customer.dao");
        assertThat(result.typeSpec.name).isEqualTo("CustomerDAO");
        
        String generatedCode = result.toString();
        assertThat(generatedCode).contains("@Component");
        assertThat(generatedCode).contains("NamedParameterJdbcTemplate");
        assertThat(generatedCode).contains("getCustomer");
        assertThat(generatedCode).contains("List<CustomerDTO>");
    }

    @Test
    void shouldGenerateDAOWithMultipleParameters() {
        // Given
        List<ColumnMetadata> columnMetadata = Arrays.asList(
            TestUtils.createColumnMetadata("id", "INT", java.sql.Types.INTEGER, false),
            TestUtils.createColumnMetadata("order_date", "DATE", java.sql.Types.DATE, false)
        );
        List<DBColumn> predicateLiterals = Arrays.asList(
            new DBColumn("orders", "customerId", "java.lang.Integer", "INTEGER"),
            new DBColumn("orders", "status", "java.lang.String", "VARCHAR"),
            new DBColumn("orders", "minAmount", "java.math.BigDecimal", "DECIMAL")
        );
        String sql = "SELECT id, order_date FROM orders WHERE customer_id = ? AND status = ? AND amount >= ?";

        // When
        JavaFile result = GenerateDAO.createDaoFromMetadata("Order", columnMetadata, predicateLiterals, sql);

        // Then
        String generatedCode = result.toString();
        assertThat(generatedCode).contains("Integer customerid");
        assertThat(generatedCode).contains("String status");
        assertThat(generatedCode).contains("BigDecimal minamount");
        assertThat(generatedCode).contains("sqlParamMap.put(\"customerid\", customerid)");
        assertThat(generatedCode).contains("sqlParamMap.put(\"status\", status)");
        assertThat(generatedCode).contains("sqlParamMap.put(\"minamount\", minamount)");
    }

    @Test
    void shouldHandleComplexSQL() {
        // Given
        List<ColumnMetadata> columnMetadata = Arrays.asList(
            TestUtils.createColumnMetadata("total_amount", "DECIMAL", java.sql.Types.DECIMAL, false)
        );
        List<DBColumn> predicateLiterals = Arrays.asList(
            new DBColumn("orders", "year", "java.lang.Integer", "INTEGER")
        );
        String complexSQL = "SELECT SUM(amount) as total_amount FROM orders WHERE YEAR(order_date) = ? GROUP BY customer_id";

        // When
        JavaFile result = GenerateDAO.createDaoFromMetadata("OrderSummary", columnMetadata, predicateLiterals, complexSQL);

        // Then
        String generatedCode = result.toString();
        assertThat(generatedCode).contains("OrderSummaryDAO");
        assertThat(generatedCode).contains("getOrderSummary");
        assertThat(generatedCode).contains("Integer year");
    }

    @ParameterizedTest
    @ValueSource(strings = {"Product", "Customer", "OrderDetails"})
    void shouldGenerateDAOForDifferentBusinessPurposes(String businessPurpose) {
        // Given
        List<ColumnMetadata> columnMetadata = Arrays.asList(
            TestUtils.createColumnMetadata("id", "INT", java.sql.Types.INTEGER, false)
        );
        List<DBColumn> predicateLiterals = Arrays.asList();
        String sql = "SELECT id FROM test";

        // When
        JavaFile result = GenerateDAO.createDaoFromMetadata(businessPurpose, columnMetadata, predicateLiterals, sql);

        // Then
        assertThat(result.typeSpec.name).isEqualTo(businessPurpose + "DAO");
        assertThat(result.packageName).isEqualTo("com.jfeatures.msg." + businessPurpose.toLowerCase() + ".dao");
    }

    @Test
    void shouldValidateInputParameters() {
        // Given
        List<ColumnMetadata> validColumnMetadata = Arrays.asList(
            TestUtils.createColumnMetadata("id", "INT", java.sql.Types.INTEGER, false)
        );
        List<DBColumn> validPredicateLiterals = Arrays.asList();
        String validSQL = "SELECT id FROM test";

        // When & Then - Test null business purpose
        assertThatThrownBy(() -> GenerateDAO.createDaoFromMetadata(null, validColumnMetadata, validPredicateLiterals, validSQL))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Business purpose of SQL cannot be null or empty");

        // Test empty business purpose
        assertThatThrownBy(() -> GenerateDAO.createDaoFromMetadata("", validColumnMetadata, validPredicateLiterals, validSQL))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Business purpose of SQL cannot be null or empty");

        // Test null column metadata
        assertThatThrownBy(() -> GenerateDAO.createDaoFromMetadata("Test", null, validPredicateLiterals, validSQL))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Select column metadata cannot be null");

        // Test empty column metadata
        assertThatThrownBy(() -> GenerateDAO.createDaoFromMetadata("Test", Arrays.asList(), validPredicateLiterals, validSQL))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Select column metadata cannot be empty");

        // Test null predicate literals
        assertThatThrownBy(() -> GenerateDAO.createDaoFromMetadata("Test", validColumnMetadata, null, validSQL))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Predicate having literals cannot be null");

        // Test null SQL
        assertThatThrownBy(() -> GenerateDAO.createDaoFromMetadata("Test", validColumnMetadata, validPredicateLiterals, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("SQL cannot be null or empty");

        // Test empty SQL
        assertThatThrownBy(() -> GenerateDAO.createDaoFromMetadata("Test", validColumnMetadata, validPredicateLiterals, ""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("SQL cannot be null or empty");
    }

    @Test
    void shouldHandleEmptyPredicateLiterals() {
        // Given
        List<ColumnMetadata> columnMetadata = Arrays.asList(
            TestUtils.createColumnMetadata("count", "INT", java.sql.Types.INTEGER, false)
        );
        List<DBColumn> emptyPredicateLiterals = Arrays.asList();
        String sql = "SELECT COUNT(*) as count FROM customer";

        // When
        JavaFile result = GenerateDAO.createDaoFromMetadata("CustomerCount", columnMetadata, emptyPredicateLiterals, sql);

        // Then
        String generatedCode = result.toString();
        assertThat(generatedCode).contains("getCustomerCount()");
        assertThat(generatedCode).contains("Map<String, Object> sqlParamMap = new HashMap()");
    }

    @Test
    void shouldGenerateConstructorAndFields() {
        // Given
        List<ColumnMetadata> columnMetadata = Arrays.asList(
            TestUtils.createColumnMetadata("id", "INT", java.sql.Types.INTEGER, false)
        );
        List<DBColumn> predicateLiterals = Arrays.asList();
        String sql = "SELECT id FROM test";

        // When
        JavaFile result = GenerateDAO.createDaoFromMetadata("Test", columnMetadata, predicateLiterals, sql);

        // Then
        String generatedCode = result.toString();
        assertThat(generatedCode).contains("private final NamedParameterJdbcTemplate namedParameterJdbcTemplate");
        assertThat(generatedCode).contains("private static final String SQL =");
        assertThat(generatedCode).contains("TestDAO(NamedParameterJdbcTemplate namedParameterJdbcTemplate)");
        assertThat(generatedCode).contains("this.namedParameterJdbcTemplate = namedParameterJdbcTemplate");
    }
}