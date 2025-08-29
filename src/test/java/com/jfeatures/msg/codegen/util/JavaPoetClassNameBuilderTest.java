package com.jfeatures.msg.codegen.util;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JavaPoetClassNameBuilderTest {

    @Test
    void testBuildJavaPoetTypeName_ValidInputs_ReturnsCorrectClassName() {
        // Given
        String packageName = "com.example.customer.dto";
        String businessDomainName = "Customer";
        String classType = "DTO";
        
        // When
        TypeName result = JavaPoetClassNameBuilder.buildJavaPoetTypeName(packageName, businessDomainName, classType);
        
        // Then
        assertNotNull(result);
        assertTrue(result instanceof ClassName);
        
        ClassName className = (ClassName) result;
        assertEquals("com.example.customer.dto", className.packageName());
        assertEquals("CustomerDTO", className.simpleName());
        assertEquals("com.example.customer.dto.CustomerDTO", className.toString());
    }
    
    @Test
    void testBuildJavaPoetTypeName_ControllerType_ReturnsCorrectClassName() {
        // Given
        String packageName = "com.example.order.controller";
        String businessDomainName = "Order";
        String classType = "Controller";
        
        // When
        TypeName result = JavaPoetClassNameBuilder.buildJavaPoetTypeName(packageName, businessDomainName, classType);
        
        // Then
        assertNotNull(result);
        assertTrue(result instanceof ClassName);
        
        ClassName className = (ClassName) result;
        assertEquals("com.example.order.controller", className.packageName());
        assertEquals("OrderController", className.simpleName());
        assertEquals("com.example.order.controller.OrderController", className.toString());
    }
    
    @Test
    void testBuildJavaPoetTypeName_DAOType_ReturnsCorrectClassName() {
        // Given
        String packageName = "com.example.product.dao";
        String businessDomainName = "Product";
        String classType = "DAO";
        
        // When
        TypeName result = JavaPoetClassNameBuilder.buildJavaPoetTypeName(packageName, businessDomainName, classType);
        
        // Then
        assertNotNull(result);
        assertTrue(result instanceof ClassName);
        
        ClassName className = (ClassName) result;
        assertEquals("com.example.product.dao", className.packageName());
        assertEquals("ProductDAO", className.simpleName());
    }
    
    @Test
    void testBuildJavaPoetTypeName_MultiWordBusinessDomain_ReturnsCorrectClassName() {
        // Given
        String packageName = "com.example.dto";
        String businessDomainName = "CustomerOrder";
        String classType = "DTO";
        
        // When
        TypeName result = JavaPoetClassNameBuilder.buildJavaPoetTypeName(packageName, businessDomainName, classType);
        
        // Then
        assertNotNull(result);
        assertTrue(result instanceof ClassName);
        
        ClassName className = (ClassName) result;
        assertEquals("com.example.dto", className.packageName());
        assertEquals("CustomerOrderDTO", className.simpleName());
    }
    
    @Test
    void testBuildJavaPoetTypeName_CamelCaseBusinessDomain_ReturnsCorrectClassName() {
        // Given
        String packageName = "com.example.service";
        String businessDomainName = "productCategory";
        String classType = "Service";
        
        // When
        TypeName result = JavaPoetClassNameBuilder.buildJavaPoetTypeName(packageName, businessDomainName, classType);
        
        // Then
        assertNotNull(result);
        assertTrue(result instanceof ClassName);
        
        ClassName className = (ClassName) result;
        assertEquals("com.example.service", className.packageName());
        assertEquals("productCategoryService", className.simpleName());
    }
    
    @Test
    void testBuildJavaPoetTypeName_SingleLevelPackage_ReturnsCorrectClassName() {
        // Given
        String packageName = "dto";
        String businessDomainName = "User";
        String classType = "DTO";
        
        // When
        TypeName result = JavaPoetClassNameBuilder.buildJavaPoetTypeName(packageName, businessDomainName, classType);
        
        // Then
        assertNotNull(result);
        assertTrue(result instanceof ClassName);
        
        ClassName className = (ClassName) result;
        assertEquals("dto", className.packageName());
        assertEquals("UserDTO", className.simpleName());
        assertEquals("dto.UserDTO", className.toString());
    }
    
    @Test
    void testBuildJavaPoetTypeName_NullPackageName_ThrowsException() {
        // Given
        String packageName = null;
        String businessDomainName = "Customer";
        String classType = "DTO";
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> JavaPoetClassNameBuilder.buildJavaPoetTypeName(packageName, businessDomainName, classType)
        );
        
        assertEquals("Target Java package name cannot be null or empty", exception.getMessage());
    }
    
    @Test
    void testBuildJavaPoetTypeName_EmptyPackageName_ThrowsException() {
        // Given
        String packageName = "";
        String businessDomainName = "Customer";
        String classType = "DTO";
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> JavaPoetClassNameBuilder.buildJavaPoetTypeName(packageName, businessDomainName, classType)
        );
        
        assertEquals("Target Java package name cannot be null or empty", exception.getMessage());
    }
    
    @Test
    void testBuildJavaPoetTypeName_WhitespaceOnlyPackageName_ThrowsException() {
        // Given
        String packageName = "   ";
        String businessDomainName = "Customer";
        String classType = "DTO";
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> JavaPoetClassNameBuilder.buildJavaPoetTypeName(packageName, businessDomainName, classType)
        );
        
        assertEquals("Target Java package name cannot be null or empty", exception.getMessage());
    }
    
    @Test
    void testBuildJavaPoetTypeName_NullBusinessDomainName_ThrowsException() {
        // Given
        String packageName = "com.example.dto";
        String businessDomainName = null;
        String classType = "DTO";
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> JavaPoetClassNameBuilder.buildJavaPoetTypeName(packageName, businessDomainName, classType)
        );
        
        assertEquals("SQL business domain name cannot be null or empty", exception.getMessage());
    }
    
    @Test
    void testBuildJavaPoetTypeName_EmptyBusinessDomainName_ThrowsException() {
        // Given
        String packageName = "com.example.dto";
        String businessDomainName = "";
        String classType = "DTO";
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> JavaPoetClassNameBuilder.buildJavaPoetTypeName(packageName, businessDomainName, classType)
        );
        
        assertEquals("SQL business domain name cannot be null or empty", exception.getMessage());
    }
    
    @Test
    void testBuildJavaPoetTypeName_WhitespaceOnlyBusinessDomainName_ThrowsException() {
        // Given
        String packageName = "com.example.dto";
        String businessDomainName = "   ";
        String classType = "DTO";
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> JavaPoetClassNameBuilder.buildJavaPoetTypeName(packageName, businessDomainName, classType)
        );
        
        assertEquals("SQL business domain name cannot be null or empty", exception.getMessage());
    }
    
    @Test
    void testBuildJavaPoetTypeName_NullClassType_ThrowsException() {
        // Given
        String packageName = "com.example.dto";
        String businessDomainName = "Customer";
        String classType = null;
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> JavaPoetClassNameBuilder.buildJavaPoetTypeName(packageName, businessDomainName, classType)
        );
        
        assertEquals("Class type cannot be null or empty", exception.getMessage());
    }
    
    @Test
    void testBuildJavaPoetTypeName_EmptyClassType_ThrowsException() {
        // Given
        String packageName = "com.example.dto";
        String businessDomainName = "Customer";
        String classType = "";
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> JavaPoetClassNameBuilder.buildJavaPoetTypeName(packageName, businessDomainName, classType)
        );
        
        assertEquals("Class type cannot be null or empty", exception.getMessage());
    }
    
    @Test
    void testBuildJavaPoetTypeName_WhitespaceOnlyClassType_ThrowsException() {
        // Given
        String packageName = "com.example.dto";
        String businessDomainName = "Customer";
        String classType = "   ";
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> JavaPoetClassNameBuilder.buildJavaPoetTypeName(packageName, businessDomainName, classType)
        );
        
        assertEquals("Class type cannot be null or empty", exception.getMessage());
    }
    
    @Test
    void testBuildJavaPoetTypeName_SpecialCharactersInInputs_HandlesCorrectly() {
        // Given - Test with inputs that might contain special characters
        String packageName = "com.example.customer_order.dto";
        String businessDomainName = "Customer";
        String classType = "DTO";
        
        // When
        TypeName result = JavaPoetClassNameBuilder.buildJavaPoetTypeName(packageName, businessDomainName, classType);
        
        // Then
        assertNotNull(result);
        assertTrue(result instanceof ClassName);
        
        ClassName className = (ClassName) result;
        assertEquals("com.example.customer_order.dto", className.packageName());
        assertEquals("CustomerDTO", className.simpleName());
    }
    
    @Test
    void testBuildJavaPoetTypeName_LowerCaseBusinessDomain_ConcatenatesDirectly() {
        // Given
        String packageName = "com.example.dto";
        String businessDomainName = "customer";
        String classType = "DTO";
        
        // When
        TypeName result = JavaPoetClassNameBuilder.buildJavaPoetTypeName(packageName, businessDomainName, classType);
        
        // Then
        assertNotNull(result);
        assertTrue(result instanceof ClassName);
        
        ClassName className = (ClassName) result;
        assertEquals("com.example.dto", className.packageName());
        assertEquals("customerDTO", className.simpleName());
    }
    
    @Test
    void testBuildJavaPoetTypeName_LowerCaseClassType_ConcatenatesDirectly() {
        // Given
        String packageName = "com.example.dto";
        String businessDomainName = "Customer";
        String classType = "dto";
        
        // When
        TypeName result = JavaPoetClassNameBuilder.buildJavaPoetTypeName(packageName, businessDomainName, classType);
        
        // Then
        assertNotNull(result);
        assertTrue(result instanceof ClassName);
        
        ClassName className = (ClassName) result;
        assertEquals("com.example.dto", className.packageName());
        assertEquals("Customerdto", className.simpleName());
    }
    
    @Test
    void testBuildJavaPoetTypeName_DeepPackageStructure_HandlesCorrectly() {
        // Given
        String packageName = "com.example.application.microservices.customer.domain.dto";
        String businessDomainName = "Customer";
        String classType = "DTO";
        
        // When
        TypeName result = JavaPoetClassNameBuilder.buildJavaPoetTypeName(packageName, businessDomainName, classType);
        
        // Then
        assertNotNull(result);
        assertTrue(result instanceof ClassName);
        
        ClassName className = (ClassName) result;
        assertEquals("com.example.application.microservices.customer.domain.dto", className.packageName());
        assertEquals("CustomerDTO", className.simpleName());
        assertEquals("com.example.application.microservices.customer.domain.dto.CustomerDTO", className.toString());
    }
}