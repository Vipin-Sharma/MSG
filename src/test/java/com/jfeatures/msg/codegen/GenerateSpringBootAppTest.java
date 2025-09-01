package com.jfeatures.msg.codegen;

import com.squareup.javapoet.JavaFile;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GenerateSpringBootAppTest {

    @Test
    void shouldGenerateValidSpringBootApplication() throws Exception {
        // When
        JavaFile result = GenerateSpringBootApp.createSpringBootApp("Customer");

        // Then
        assertThat(result).isNotNull();
        String generatedCode = result.toString();
        assertThat(generatedCode).contains("@SpringBootApplication");
        assertThat(generatedCode).contains("CustomerSpringBootApplication");
        assertThat(generatedCode).contains("public static void main(String[] args)");
    }

    @Test
    void shouldHandleDifferentBusinessNames() throws Exception {
        String[] names = {"Product", "Order", "Invoice"};
        
        for (String name : names) {
            JavaFile result = GenerateSpringBootApp.createSpringBootApp(name);
            String code = result.toString();
            assertThat(code).contains(name + "SpringBootApplication");
        }
    }
}