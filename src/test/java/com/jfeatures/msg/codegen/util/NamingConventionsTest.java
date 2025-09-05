package com.jfeatures.msg.codegen.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Comprehensive tests for NamingConventions utility class to achieve 90%+ coverage.
 * Tests all naming convention methods for consistency and edge cases.
 */
class NamingConventionsTest {

    // ============================= PACKAGE NAME TESTS ===============================

    @ParameterizedTest
    @MethodSource("providePackageNameArguments")
    void shouldBuildPackageNames(String businessName, String suffix, String expected) {
        // When
        String result = NamingConventions.buildPackageName(businessName, suffix);
        
        // Then
        assertThat(result).isEqualTo(expected);
    }

    static Stream<Arguments> providePackageNameArguments() {
        return Stream.of(
            Arguments.of("Customer", "dao", "com.jfeatures.msg.customer.dao"),
            Arguments.of("OrderDetail", "dto", "com.jfeatures.msg.orderdetail.dto"),
            Arguments.of("USER", "controller", "com.jfeatures.msg.user.controller"),
            Arguments.of("Product", "service", "com.jfeatures.msg.product.service"),
            Arguments.of("MyCustomEntity", "repository", "com.jfeatures.msg.mycustomentity.repository"),
            Arguments.of("", "dao", "com.jfeatures.msg..dao"),
            Arguments.of("Test", "", "com.jfeatures.msg.test.")
        );
    }

    @Test
    void shouldThrowExceptionForNullBusinessNameInPackage() {
        assertThrows(IllegalArgumentException.class, 
            () -> NamingConventions.buildPackageName(null, "dao"));
    }

    @Test
    void shouldThrowExceptionForNullSuffixInPackage() {
        assertThrows(IllegalArgumentException.class, 
            () -> NamingConventions.buildPackageName("Customer", null));
    }

    // ============================= CLASS NAME TESTS ==================================

    @ParameterizedTest
    @MethodSource("provideClassNameArguments")
    void shouldBuildClassNames(String businessName, String suffix, String expected) {
        // When
        String result = NamingConventions.buildClassName(businessName, suffix);
        
        // Then
        assertThat(result).isEqualTo(expected);
    }

    static Stream<Arguments> provideClassNameArguments() {
        return Stream.of(
            Arguments.of("Customer", "DAO", "CustomerDAO"),
            Arguments.of("OrderDetail", "DTO", "OrderDetailDTO"),
            Arguments.of("user", "Controller", "UserController"),
            Arguments.of("PRODUCT", "Service", "PRODUCTService"), // ALLCAPS stays as-is since it doesn't contain _ or -
            Arguments.of("myEntity", "Repository", "MyentityRepository"), // CaseUtils converts to "Myentity"
            Arguments.of("order_item", "DAO", "Order_itemDAO"), // CaseUtils converts underscores
            Arguments.of("user-profile", "Service", "User-profileService"), // CaseUtils converts hyphens
            Arguments.of("", "DAO", "DAO")
        );
    }

    @Test
    void shouldThrowExceptionForNullBusinessNameInClassName() {
        assertThrows(IllegalArgumentException.class, 
            () -> NamingConventions.buildClassName(null, "DAO"));
    }

    @Test
    void shouldThrowExceptionForNullSuffixInClassName() {
        assertThrows(IllegalArgumentException.class, 
            () -> NamingConventions.buildClassName("Customer", null));
    }

    // ============================= DAO/DTO/CONTROLLER CLASS NAME TESTS ===============

    @Test
    void shouldCreateDaoClassName() {
        assertThat(NamingConventions.daoClassName("Customer")).isEqualTo("CustomerDAO");
        assertThat(NamingConventions.daoClassName("OrderDetail")).isEqualTo("OrderDetailDAO");
    }

    @Test
    void shouldCreateControllerClassName() {
        assertThat(NamingConventions.controllerClassName("Customer")).isEqualTo("CustomerController");
        assertThat(NamingConventions.controllerClassName("OrderDetail")).isEqualTo("OrderDetailController");
    }

    @Test
    void shouldCreateDtoClassName() {
        assertThat(NamingConventions.dtoClassName("Customer")).isEqualTo("CustomerDTO");
        assertThat(NamingConventions.dtoClassName("OrderDetail")).isEqualTo("OrderDetailDTO");
    }

    // ============================= FIELD NAME TESTS ==================================

    @ParameterizedTest
    @MethodSource("provideFieldNameArguments")
    void shouldCreateJdbcTemplateFieldNames(String businessName, String expected) {
        // When
        String result = NamingConventions.jdbcTemplateFieldName(businessName);
        
        // Then
        assertThat(result).isEqualTo(expected);
    }

    static Stream<Arguments> provideFieldNameArguments() {
        return Stream.of(
            Arguments.of("Customer", "customerNamedParameterJdbcTemplate"),
            Arguments.of("OrderDetail", "orderdetailNamedParameterJdbcTemplate"),
            Arguments.of("USER", "userNamedParameterJdbcTemplate"),
            Arguments.of("Product", "productNamedParameterJdbcTemplate"),
            Arguments.of("order_item", "order_itemNamedParameterJdbcTemplate")
        );
    }

    @Test
    void shouldCreateDaoFieldName() {
        // When & Then
        assertThat(NamingConventions.daoFieldName("Customer")).isEqualTo("customerDAO");
        assertThat(NamingConventions.daoFieldName("OrderDetail")).isEqualTo("orderdetailDAO");
        assertThat(NamingConventions.daoFieldName("USER")).isEqualTo("userDAO");
    }

    @Test
    void shouldCreateSqlFieldName() {
        // When & Then
        assertThat(NamingConventions.sqlFieldName("SELECT")).isEqualTo("SELECT_SQL");
        assertThat(NamingConventions.sqlFieldName("insert")).isEqualTo("INSERT_SQL");
        assertThat(NamingConventions.sqlFieldName("UPDATE")).isEqualTo("UPDATE_SQL");
        assertThat(NamingConventions.sqlFieldName("delete")).isEqualTo("DELETE_SQL");
        
        // Test with empty/null operation
        assertThat(NamingConventions.sqlFieldName("")).isEqualTo("SQL");
        assertThat(NamingConventions.sqlFieldName(null)).isEqualTo("SQL");
    }

    @Test
    void shouldThrowExceptionForNullBusinessNameInFieldName() {
        assertThrows(IllegalArgumentException.class, 
            () -> NamingConventions.jdbcTemplateFieldName(null));
        assertThrows(IllegalArgumentException.class, 
            () -> NamingConventions.daoFieldName(null));
    }

    // ============================= METHOD NAME TESTS =================================

    @Test
    void shouldCreateGetterMethodNames() {
        // When & Then
        assertThat(NamingConventions.getterMethodName("customerId")).isEqualTo("getCustomerid");
        assertThat(NamingConventions.getterMethodName("customer_name")).isEqualTo("getCustomerName");
        assertThat(NamingConventions.getterMethodName("first_name")).isEqualTo("getFirstName");
        assertThat(NamingConventions.getterMethodName("isActive")).isEqualTo("getIsactive");
    }

    @Test
    void shouldCreateSetterMethodNames() {
        // When & Then
        assertThat(NamingConventions.setterMethodName("customerId")).isEqualTo("setCustomerid");
        assertThat(NamingConventions.setterMethodName("customer_name")).isEqualTo("setCustomerName");
        assertThat(NamingConventions.setterMethodName("first_name")).isEqualTo("setFirstName");
        assertThat(NamingConventions.setterMethodName("isActive")).isEqualTo("setIsactive");
    }

    @Test
    void shouldCreateDaoMethodNames() {
        // When & Then
        assertThat(NamingConventions.daoMethodName("find", "Customer")).isEqualTo("findCustomer");
        assertThat(NamingConventions.daoMethodName("save", "OrderDetail")).isEqualTo("saveOrderDetail");
        assertThat(NamingConventions.daoMethodName("DELETE", "Product")).isEqualTo("deleteProduct");
    }

    @Test
    void shouldCreateControllerMethodNames() {
        // When & Then
        assertThat(NamingConventions.controllerMethodName("get", "Customer")).isEqualTo("getDataForCustomer");
        assertThat(NamingConventions.controllerMethodName("post", "OrderDetail")).isEqualTo("postDataForOrderDetail");
        assertThat(NamingConventions.controllerMethodName("UPDATE", "Product")).isEqualTo("updateDataForProduct");
    }

    @Test
    void shouldThrowExceptionForNullFieldNameInMethods() {
        assertThrows(IllegalArgumentException.class, 
            () -> NamingConventions.getterMethodName(null));
        assertThrows(IllegalArgumentException.class, 
            () -> NamingConventions.setterMethodName(null));
    }

    @Test
    void shouldThrowExceptionForNullOperationInMethods() {
        assertThrows(IllegalArgumentException.class, 
            () -> NamingConventions.daoMethodName(null, "Customer"));
        assertThrows(IllegalArgumentException.class, 
            () -> NamingConventions.controllerMethodName(null, "Customer"));
    }

    @Test
    void shouldThrowExceptionForNullBusinessNameInMethods() {
        assertThrows(IllegalArgumentException.class, 
            () -> NamingConventions.daoMethodName("find", null));
        assertThrows(IllegalArgumentException.class, 
            () -> NamingConventions.controllerMethodName("get", null));
    }

    // ============================= PARAMETER NAME TESTS ==============================

    @ParameterizedTest
    @MethodSource("provideParameterNameArguments")
    void shouldCreateParameterNames(String columnName, String expected) {
        // When
        String result = NamingConventions.parameterName(columnName);
        
        // Then
        assertThat(result).isEqualTo(expected);
    }

    static Stream<Arguments> provideParameterNameArguments() {
        return Stream.of(
            Arguments.of("customer_id", "customerId"),
            Arguments.of("first_name", "firstName"),
            Arguments.of("email_address", "emailAddress"),
            Arguments.of("is_active", "isActive"),
            Arguments.of("created_date", "createdDate"),
            Arguments.of("ID", "id"),
            Arguments.of("NAME", "name")
        );
    }

    @Test
    void shouldCreateFieldNames() {
        // When & Then
        assertThat(NamingConventions.fieldName("customer_id")).isEqualTo("customerId");
        assertThat(NamingConventions.fieldName("first_name")).isEqualTo("firstName");
        assertThat(NamingConventions.fieldName("email_address")).isEqualTo("emailAddress");
    }

    @Test
    void shouldThrowExceptionForNullColumnName() {
        assertThrows(IllegalArgumentException.class, 
            () -> NamingConventions.parameterName(null));
        assertThrows(IllegalArgumentException.class, 
            () -> NamingConventions.fieldName(null));
    }

    // ============================= EDGE CASE TESTS ===================================

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "  "})
    void shouldHandleEmptyOrWhitespaceStrings(String input) {
        // Should not throw exceptions for empty strings, but may produce unusual results
        String trimmed = input.trim();
        
        if (!trimmed.isEmpty()) {
            assertThat(NamingConventions.buildClassName(trimmed, "DAO")).isNotNull();
            assertThat(NamingConventions.buildPackageName(trimmed, "dao")).isNotNull();
        } else {
            // Empty strings should work as demonstrated in existing tests
            assertThat(NamingConventions.buildClassName("", "DAO")).isEqualTo("DAO");
            assertThat(NamingConventions.buildPackageName("", "dao")).isEqualTo("com.jfeatures.msg..dao");
        }
    }

    @Test
    void shouldHandleSpecialCharactersInBusinessName() {
        // Given names with special characters
        String result1 = NamingConventions.buildClassName("Customer$Order", "DAO");
        String result2 = NamingConventions.buildPackageName("User-Profile", "service");
        String result3 = NamingConventions.jdbcTemplateFieldName("Order_Item");
        
        // Should handle them gracefully
        assertThat(result1).isEqualTo("Customer$OrderDAO");
        assertThat(result2).isEqualTo("com.jfeatures.msg.user-profile.service");
        assertThat(result3).isEqualTo("order_itemNamedParameterJdbcTemplate");
    }

    @Test
    void shouldHandleNumericBusinessNames() {
        // Given numeric business names
        String result1 = NamingConventions.buildClassName("Entity123", "DAO");
        String result2 = NamingConventions.jdbcTemplateFieldName("Service456");
        String result3 = NamingConventions.buildPackageName("Item789", "dto");
        
        // Should handle them correctly
        assertThat(result1).isEqualTo("Entity123DAO");
        assertThat(result2).isEqualTo("service456NamedParameterJdbcTemplate");
        assertThat(result3).isEqualTo("com.jfeatures.msg.item789.dto");
    }

    @Test
    void shouldMaintainCaseConsistency() {
        // Test that case transformations are consistent
        assertThat(NamingConventions.buildClassName("ALLCAPS", "DAO"))
            .isEqualTo("ALLCAPSDAO"); // ALLCAPS stays as-is since no _ or -
        assertThat(NamingConventions.buildPackageName("ALLCAPS", "dao"))
            .isEqualTo("com.jfeatures.msg.allcaps.dao");
        assertThat(NamingConventions.jdbcTemplateFieldName("ALLCAPS"))
            .isEqualTo("allcapsNamedParameterJdbcTemplate");
    }

    @Test
    void shouldHandleMixedCaseAndUnderscores() {
        // Test mixed case with underscores
        assertThat(NamingConventions.parameterName("Customer_ID")).isEqualTo("customerId");
        assertThat(NamingConventions.parameterName("FIRST_NAME")).isEqualTo("firstName");
        assertThat(NamingConventions.getterMethodName("user_Email")).isEqualTo("getUserEmail");
        assertThat(NamingConventions.setterMethodName("order_Total")).isEqualTo("setOrderTotal");
    }

    @Test
    void shouldHandleEmptyStringsInMethods() {
        // Based on current validation logic, empty strings are allowed (only null is checked)
        // Test empty strings in methods - these should work since validation only checks for null
        assertThat(NamingConventions.getterMethodName("")).isEqualTo("get");
        assertThat(NamingConventions.setterMethodName("")).isEqualTo("set");
        assertThat(NamingConventions.daoMethodName("", "Customer")).isEqualTo("Customer");
        assertThat(NamingConventions.controllerMethodName("get", "")).isEqualTo("getDataFor");
        assertThat(NamingConventions.parameterName("")).isEqualTo("");
    }

    @Test
    void shouldHandleWhitespaceInMethods() {
        // Test whitespace strings in methods - CaseUtils will transform whitespace
        assertThat(NamingConventions.getterMethodName("   ")).isEqualTo("get");
        assertThat(NamingConventions.setterMethodName("   ")).isEqualTo("set");
        assertThat(NamingConventions.parameterName("   ")).isEqualTo("");
    }

    // ============================= CONSTRUCTOR TESTS =================================

    @Test
    void shouldNotAllowInstantiation() {
        // Verify utility class cannot be instantiated
        Exception exception = assertThrows(Exception.class, () -> {
            // Use reflection to try to create instance
            java.lang.reflect.Constructor<NamingConventions> constructor = 
                NamingConventions.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            constructor.newInstance();
        });
        
        // The actual exception will be InvocationTargetException wrapping UnsupportedOperationException
        assertThat(exception.getCause()).isInstanceOf(UnsupportedOperationException.class);
        assertThat(exception.getCause().getMessage()).contains("This is a utility class and cannot be instantiated");
    }
}