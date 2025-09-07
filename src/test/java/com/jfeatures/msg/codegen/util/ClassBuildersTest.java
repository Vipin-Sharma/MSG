package com.jfeatures.msg.codegen.util;

import com.squareup.javapoet.TypeSpec;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.lang.model.element.Modifier;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Comprehensive tests for ClassBuilders utility class to achieve 90%+ coverage.
 * Tests class creation for various scenarios including DAOs, Controllers, DTOs, and Configuration classes.
 */
class ClassBuildersTest {

    // ============================= BASIC DAO CLASS TESTS =========================

    @Test
    void shouldCreateBasicDAOClass() {
        // When
        TypeSpec.Builder classBuilder = ClassBuilders.basicDAOClass("Customer");
        TypeSpec typeSpec = classBuilder.build();
        
        // Then
        assertThat(typeSpec.name).isEqualTo("CustomerDAO");
        assertThat(typeSpec.modifiers).containsExactlyInAnyOrder(Modifier.PUBLIC);
        assertThat(typeSpec.annotations).hasSize(2);
        
        // Check Component annotation
        assertThat(typeSpec.annotations.stream()
            .anyMatch(a -> a.type.toString().contains("Component"))).isTrue();
        
        // Check Slf4j annotation
        assertThat(typeSpec.annotations.stream()
            .anyMatch(a -> a.type.toString().contains("Slf4j"))).isTrue();
        
        // Check JavaDoc
        assertThat(typeSpec.javadoc.toString()).contains("Data Access Object for customer operations");
        assertThat(typeSpec.javadoc.toString()).contains("Follows single responsibility principle");
    }

    @Test
    void shouldThrowExceptionForNullBusinessNameInBasicDAO() {
        assertThrows(IllegalArgumentException.class, 
            () -> ClassBuilders.basicDAOClass(null));
    }

    @Test
    void shouldThrowExceptionForEmptyBusinessNameInBasicDAO() {
        assertThrows(IllegalArgumentException.class, 
            () -> ClassBuilders.basicDAOClass(""));
    }

    @ParameterizedTest
    @ValueSource(strings = {"   ", "\t", "\n", " \t\n "})
    void shouldThrowExceptionForWhitespaceBusinessNameInBasicDAO(String businessName) {
        assertThrows(IllegalArgumentException.class, 
            () -> ClassBuilders.basicDAOClass(businessName));
    }

    // ============================= OPERATION DAO CLASS TESTS ===========================

    @Test
    void shouldCreateOperationDAOClass() {
        // When
        TypeSpec.Builder classBuilder = ClassBuilders.operationDAOClass("Customer", "Insert");
        TypeSpec typeSpec = classBuilder.build();
        
        // Then
        assertThat(typeSpec.name).isEqualTo("CustomerInsertDAO");
        assertThat(typeSpec.modifiers).containsExactlyInAnyOrder(Modifier.PUBLIC);
        assertThat(typeSpec.annotations).hasSize(2);
        
        // Check Component annotation
        assertThat(typeSpec.annotations.stream()
            .anyMatch(a -> a.type.toString().contains("Component"))).isTrue();
        
        // Check Slf4j annotation
        assertThat(typeSpec.annotations.stream()
            .anyMatch(a -> a.type.toString().contains("Slf4j"))).isTrue();
        
        // Check JavaDoc
        assertThat(typeSpec.javadoc.toString()).contains("Data Access Object for customer insert operations");
        assertThat(typeSpec.javadoc.toString()).contains("Follows single responsibility principle - insert operations only");
    }

    @ParameterizedTest
    @MethodSource("provideOperationDAOTestArguments")
    void shouldCreateOperationDAOClassForDifferentOperations(String businessName, String operation, String expectedClassName) {
        // When
        TypeSpec.Builder classBuilder = ClassBuilders.operationDAOClass(businessName, operation);
        TypeSpec typeSpec = classBuilder.build();
        
        // Then
        assertThat(typeSpec.name).isEqualTo(expectedClassName);
        assertThat(typeSpec.javadoc.toString()).contains(businessName.toLowerCase() + " " + operation.toLowerCase() + " operations");
    }

    static Stream<Arguments> provideOperationDAOTestArguments() {
        return Stream.of(
            Arguments.of("Customer", "Insert", "CustomerInsertDAO"),
            Arguments.of("Order", "Update", "OrderUpdateDAO"),
            Arguments.of("Product", "Delete", "ProductDeleteDAO"),
            Arguments.of("Invoice", "Select", "InvoiceSelectDAO")
        );
    }

    @Test
    void shouldThrowExceptionForNullParametersInOperationDAO() {
        assertThrows(IllegalArgumentException.class, 
            () -> ClassBuilders.operationDAOClass(null, "Insert"));
        assertThrows(IllegalArgumentException.class, 
            () -> ClassBuilders.operationDAOClass("Customer", null));
        assertThrows(IllegalArgumentException.class, 
            () -> ClassBuilders.operationDAOClass("", "Insert"));
        assertThrows(IllegalArgumentException.class, 
            () -> ClassBuilders.operationDAOClass("Customer", ""));
    }

    // ============================= BASIC CONTROLLER CLASS TESTS ===========================

    @Test
    void shouldCreateBasicControllerClass() {
        // When
        TypeSpec.Builder classBuilder = ClassBuilders.basicControllerClass("Customer", "/api/v1");
        TypeSpec typeSpec = classBuilder.build();
        
        // Then
        assertThat(typeSpec.name).isEqualTo("CustomerController");
        assertThat(typeSpec.modifiers).containsExactlyInAnyOrder(Modifier.PUBLIC);
        assertThat(typeSpec.annotations).hasSize(3); // RestController, RequestMapping, Tag
        
        // Check RestController annotation
        assertThat(typeSpec.annotations.stream()
            .anyMatch(a -> a.type.toString().contains("RestController"))).isTrue();
        
        // Check RequestMapping annotation
        assertThat(typeSpec.annotations.stream()
            .anyMatch(a -> a.type.toString().contains("RequestMapping"))).isTrue();
        
        // Check Tag annotation
        assertThat(typeSpec.annotations.stream()
            .anyMatch(a -> a.type.toString().contains("Tag"))).isTrue();
        
        // Check JavaDoc
        assertThat(typeSpec.javadoc.toString()).contains("REST Controller for customer operations");
        assertThat(typeSpec.javadoc.toString()).contains("Provides HTTP endpoints for data access");
    }

    @Test
    void shouldCreateBasicControllerClassWithNullBasePath() {
        // When
        TypeSpec.Builder classBuilder = ClassBuilders.basicControllerClass("Customer", null);
        TypeSpec typeSpec = classBuilder.build();
        
        // Then
        assertThat(typeSpec.name).isEqualTo("CustomerController");
        assertThat(typeSpec.annotations).hasSize(3);
    }

    @Test
    void shouldThrowExceptionForNullBusinessNameInBasicController() {
        assertThrows(IllegalArgumentException.class, 
            () -> ClassBuilders.basicControllerClass(null, "/api"));
    }

    @Test
    void shouldThrowExceptionForEmptyBusinessNameInBasicController() {
        assertThrows(IllegalArgumentException.class, 
            () -> ClassBuilders.basicControllerClass("", "/api"));
    }

    // ============================= OPERATION CONTROLLER CLASS TESTS ===========================

    @Test
    void shouldCreateOperationControllerClass() {
        // When
        TypeSpec.Builder classBuilder = ClassBuilders.operationControllerClass("Customer", "Insert", "/api/v1");
        TypeSpec typeSpec = classBuilder.build();
        
        // Then
        assertThat(typeSpec.name).isEqualTo("CustomerInsertController");
        assertThat(typeSpec.modifiers).containsExactlyInAnyOrder(Modifier.PUBLIC);
        assertThat(typeSpec.annotations).hasSize(3); // RestController, RequestMapping, Tag
        
        // Check JavaDoc
        assertThat(typeSpec.javadoc.toString()).contains("REST Controller for customer insert operations");
        assertThat(typeSpec.javadoc.toString()).contains("Follows single responsibility principle - insert operations only");
    }

    @Test
    void shouldCreateOperationControllerClassWithNullBasePath() {
        // When
        TypeSpec.Builder classBuilder = ClassBuilders.operationControllerClass("Customer", "Update", null);
        TypeSpec typeSpec = classBuilder.build();
        
        // Then
        assertThat(typeSpec.name).isEqualTo("CustomerUpdateController");
        assertThat(typeSpec.annotations).hasSize(3);
    }

    @Test
    void shouldThrowExceptionForNullParametersInOperationController() {
        assertThrows(IllegalArgumentException.class, 
            () -> ClassBuilders.operationControllerClass(null, "Insert", "/api"));
        assertThrows(IllegalArgumentException.class, 
            () -> ClassBuilders.operationControllerClass("Customer", null, "/api"));
        assertThrows(IllegalArgumentException.class, 
            () -> ClassBuilders.operationControllerClass("", "Insert", "/api"));
        assertThrows(IllegalArgumentException.class, 
            () -> ClassBuilders.operationControllerClass("Customer", "", "/api"));
    }

    // ============================= BASIC DTO CLASS TESTS ===========================

    @Test
    void shouldCreateBasicDTOClass() {
        // When
        TypeSpec.Builder classBuilder = ClassBuilders.basicDTOClass("Customer");
        TypeSpec typeSpec = classBuilder.build();
        
        // Then
        assertThat(typeSpec.name).isEqualTo("CustomerDTO");
        assertThat(typeSpec.modifiers).containsExactlyInAnyOrder(Modifier.PUBLIC);
        assertThat(typeSpec.annotations).hasSize(3); // Builder, Value, Jacksonized
        
        // Check Builder annotation
        assertThat(typeSpec.annotations.stream()
            .anyMatch(a -> a.type.toString().contains("Builder"))).isTrue();
        
        // Check Value annotation
        assertThat(typeSpec.annotations.stream()
            .anyMatch(a -> a.type.toString().contains("Value"))).isTrue();
        
        // Check Jacksonized annotation
        assertThat(typeSpec.annotations.stream()
            .anyMatch(a -> a.type.toString().contains("Jacksonized"))).isTrue();
        
        // Check JavaDoc
        assertThat(typeSpec.javadoc.toString()).contains("Data Transfer Object for customer");
        assertThat(typeSpec.javadoc.toString()).contains("Immutable class with builder pattern support");
    }

    @Test
    void shouldThrowExceptionForNullBusinessNameInBasicDTO() {
        assertThrows(IllegalArgumentException.class, 
            () -> ClassBuilders.basicDTOClass(null));
    }

    @Test
    void shouldThrowExceptionForEmptyBusinessNameInBasicDTO() {
        assertThrows(IllegalArgumentException.class, 
            () -> ClassBuilders.basicDTOClass(""));
    }

    // ============================= OPERATION DTO CLASS TESTS ===========================

    @Test
    void shouldCreateOperationDTOClass() {
        // When
        TypeSpec.Builder classBuilder = ClassBuilders.operationDTOClass("Customer", "Create");
        TypeSpec typeSpec = classBuilder.build();
        
        // Then
        assertThat(typeSpec.name).isEqualTo("CustomerCreateDTO");
        assertThat(typeSpec.modifiers).containsExactlyInAnyOrder(Modifier.PUBLIC);
        assertThat(typeSpec.annotations).hasSize(3); // Builder, Value, Jacksonized
        
        // Check JavaDoc
        assertThat(typeSpec.javadoc.toString()).contains("Data Transfer Object for customer create operations");
        assertThat(typeSpec.javadoc.toString()).contains("Immutable class with builder pattern support");
    }

    @Test
    void shouldThrowExceptionForNullParametersInOperationDTO() {
        assertThrows(IllegalArgumentException.class, 
            () -> ClassBuilders.operationDTOClass(null, "Create"));
        assertThrows(IllegalArgumentException.class, 
            () -> ClassBuilders.operationDTOClass("Customer", null));
        assertThrows(IllegalArgumentException.class, 
            () -> ClassBuilders.operationDTOClass("", "Create"));
        assertThrows(IllegalArgumentException.class, 
            () -> ClassBuilders.operationDTOClass("Customer", ""));
    }

    // ============================= POJO DTO CLASS TESTS ===========================

    @Test
    void shouldCreatePojoDTOClass() {
        // When
        TypeSpec.Builder classBuilder = ClassBuilders.pojoDTOClass("Customer");
        TypeSpec typeSpec = classBuilder.build();
        
        // Then
        assertThat(typeSpec.name).isEqualTo("CustomerDTO");
        assertThat(typeSpec.modifiers).containsExactlyInAnyOrder(Modifier.PUBLIC);
        assertThat(typeSpec.annotations).isEmpty(); // No Lombok annotations for POJO
        
        // Check JavaDoc
        assertThat(typeSpec.javadoc.toString()).contains("Data Transfer Object for customer");
        assertThat(typeSpec.javadoc.toString()).contains("Standard POJO implementation due to field count limitations");
    }

    @Test
    void shouldThrowExceptionForNullBusinessNameInPojoDTO() {
        assertThrows(IllegalArgumentException.class, 
            () -> ClassBuilders.pojoDTOClass(null));
    }

    @Test
    void shouldThrowExceptionForEmptyBusinessNameInPojoDTO() {
        assertThrows(IllegalArgumentException.class, 
            () -> ClassBuilders.pojoDTOClass(""));
    }

    // ============================= CONFIGURATION CLASS TESTS ===========================

    @Test
    void shouldCreateConfigurationClass() {
        // When
        TypeSpec.Builder classBuilder = ClassBuilders.configurationClass("Database");
        TypeSpec typeSpec = classBuilder.build();
        
        // Then
        assertThat(typeSpec.name).isEqualTo("DatabaseConfig");
        assertThat(typeSpec.modifiers).containsExactlyInAnyOrder(Modifier.PUBLIC);
        assertThat(typeSpec.annotations).hasSize(1); // Configuration
        
        // Check Configuration annotation
        assertThat(typeSpec.annotations.stream()
            .anyMatch(a -> a.type.toString().contains("Configuration"))).isTrue();
        
        // Check JavaDoc
        assertThat(typeSpec.javadoc.toString()).contains("Configuration class for database");
        assertThat(typeSpec.javadoc.toString()).contains("Defines beans and configuration settings");
    }

    @Test
    void shouldThrowExceptionForNullConfigNameInConfiguration() {
        assertThrows(IllegalArgumentException.class, 
            () -> ClassBuilders.configurationClass(null));
    }

    @Test
    void shouldThrowExceptionForEmptyConfigNameInConfiguration() {
        assertThrows(IllegalArgumentException.class, 
            () -> ClassBuilders.configurationClass(""));
    }

    // ============================= CONSTRUCTOR TESTS =================================

    @Test
    void shouldNotAllowInstantiation() throws Exception {
        // Verify utility class cannot be instantiated
        // Use reflection to try to create instance
        java.lang.reflect.Constructor<ClassBuilders> constructor =
            ClassBuilders.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        Exception exception = assertThrows(Exception.class, constructor::newInstance);
        
        // The actual exception will be InvocationTargetException wrapping UnsupportedOperationException
        assertThat(exception.getCause()).isInstanceOf(UnsupportedOperationException.class);
        assertThat(exception.getCause().getMessage()).contains("This is a utility class and cannot be instantiated");
    }

    // ============================= EDGE CASE TESTS ===================================

    @Test
    void shouldHandleSpecialCharactersInBusinessName() {
        // When
        TypeSpec.Builder classBuilder = ClassBuilders.basicDAOClass("Customer_Order");
        TypeSpec typeSpec = classBuilder.build();
        
        // Then - CaseUtils.toCamelCase converts Customer_Order to Customer_order
        assertThat(typeSpec.name).isEqualTo("Customer_orderDAO");
    }

    @Test
    void shouldHandleLongBusinessNames() {
        // Given
        String longBusinessName = "VeryLongBusinessNameThatExceedsNormalLengthButShouldStillWork";
        
        // When
        TypeSpec.Builder classBuilder = ClassBuilders.basicDTOClass(longBusinessName);
        TypeSpec typeSpec = classBuilder.build();
        
        // Then
        assertThat(typeSpec.name).isEqualTo(longBusinessName + "DTO");
    }

    @Test
    void shouldHandleDifferentCasingInBusinessNames() {
        // Test various casing patterns - CaseUtils.toCamelCase normalizes them
        TypeSpec dtoClass1 = ClassBuilders.basicDTOClass("customerOrder").build();
        TypeSpec daoClass1 = ClassBuilders.basicDAOClass("PRODUCT").build();
        TypeSpec controllerClass1 = ClassBuilders.basicControllerClass("Invoice_Item", "/api").build();
        
        assertThat(dtoClass1.name).isEqualTo("CustomerorderDTO");
        assertThat(daoClass1.name).isEqualTo("PRODUCTDAO"); // Already uppercase, no conversion needed
        assertThat(controllerClass1.name).isEqualTo("Invoice_itemController");
    }

    @ParameterizedTest
    @ValueSource(strings = {"   ", "\t\n", " \t ", "    \n\n  "})
    void shouldValidateWhitespaceOnlyInputsInAllMethods(String whitespaceInput) {
        // Test all methods reject whitespace-only inputs
        assertThrows(IllegalArgumentException.class, 
            () -> ClassBuilders.basicDAOClass(whitespaceInput));
        assertThrows(IllegalArgumentException.class, 
            () -> ClassBuilders.operationDAOClass(whitespaceInput, "Insert"));
        assertThrows(IllegalArgumentException.class, 
            () -> ClassBuilders.operationDAOClass("Customer", whitespaceInput));
        assertThrows(IllegalArgumentException.class, 
            () -> ClassBuilders.basicControllerClass(whitespaceInput, "/api"));
        assertThrows(IllegalArgumentException.class, 
            () -> ClassBuilders.operationControllerClass(whitespaceInput, "Insert", "/api"));
        assertThrows(IllegalArgumentException.class, 
            () -> ClassBuilders.operationControllerClass("Customer", whitespaceInput, "/api"));
        assertThrows(IllegalArgumentException.class, 
            () -> ClassBuilders.basicDTOClass(whitespaceInput));
        assertThrows(IllegalArgumentException.class, 
            () -> ClassBuilders.operationDTOClass(whitespaceInput, "Create"));
        assertThrows(IllegalArgumentException.class, 
            () -> ClassBuilders.operationDTOClass("Customer", whitespaceInput));
        assertThrows(IllegalArgumentException.class, 
            () -> ClassBuilders.pojoDTOClass(whitespaceInput));
        assertThrows(IllegalArgumentException.class, 
            () -> ClassBuilders.configurationClass(whitespaceInput));
    }

    @Test
    void shouldCreateClassesWithComplexBusinessNames() {
        // Test with complex business names that might be used in real scenarios
        TypeSpec orderDetailDAO = ClassBuilders.basicDAOClass("OrderDetail").build();
        TypeSpec customerAddressController = ClassBuilders.basicControllerClass("CustomerAddress", "/api/v2").build();
        TypeSpec productCategoryDTO = ClassBuilders.basicDTOClass("ProductCategory").build();
        
        assertThat(orderDetailDAO.name).isEqualTo("OrderDetailDAO");
        assertThat(customerAddressController.name).isEqualTo("CustomerAddressController");
        assertThat(productCategoryDTO.name).isEqualTo("ProductCategoryDTO");
    }

    @Test
    void shouldCreateOperationClassesWithLowercaseOperations() {
        // Test that operations work with different casing
        TypeSpec insertDAO = ClassBuilders.operationDAOClass("Customer", "insert").build();
        TypeSpec updateController = ClassBuilders.operationControllerClass("Product", "update", "/api").build();
        TypeSpec createDTO = ClassBuilders.operationDTOClass("Order", "create").build();
        
        assertThat(insertDAO.name).isEqualTo("CustomerinsertDAO");
        assertThat(updateController.name).isEqualTo("ProductupdateController");
        assertThat(createDTO.name).isEqualTo("OrdercreateDTO");
    }
}
