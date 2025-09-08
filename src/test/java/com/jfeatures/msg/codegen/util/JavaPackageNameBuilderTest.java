package com.jfeatures.msg.codegen.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class JavaPackageNameBuilderTest {

    @Test
    void shouldBuildBasicPackageName() {
        // When
        String result = JavaPackageNameBuilder.buildJavaPackageName("customer", "dto");

        // Then
        assertThat(result).isEqualTo("com.jfeatures.msg.customer.dto");
    }

    @ParameterizedTest
    @MethodSource("provideDomainAndTypeArguments")
    void shouldBuildPackageNameForDifferentCombinations(String domain, String packageType, String expected) {
        // When
        String result = JavaPackageNameBuilder.buildJavaPackageName(domain, packageType);

        // Then
        assertThat(result).isEqualTo(expected);
    }

    static Stream<Arguments> provideDomainAndTypeArguments() {
        return Stream.of(
            Arguments.of("customer", "dto", "com.jfeatures.msg.customer.dto"),
            Arguments.of("customer", "dao", "com.jfeatures.msg.customer.dao"),
            Arguments.of("customer", "controller", "com.jfeatures.msg.customer.controller"),
            Arguments.of("order", "service", "com.jfeatures.msg.order.service"),
            Arguments.of("product", "repository", "com.jfeatures.msg.product.repository"),
            Arguments.of("UserAccount", "dto", "com.jfeatures.msg.useraccount.dto"),
            Arguments.of("ORDER_DETAILS", "dao", "com.jfeatures.msg.order_details.dao")
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"Customer", "ORDER", "product_info", "user-account", "123Order"})
    void shouldHandleDifferentDomainNameFormats(String domain) {
        // When
        String result = JavaPackageNameBuilder.buildJavaPackageName(domain, "dto");

        // Then
        assertThat(result).startsWith("com.jfeatures.msg.");
        assertThat(result).contains(domain.toLowerCase()); // Domain names are converted to lowercase
        assertThat(result).endsWith(".dto");
    }

    @Test
    void shouldValidateNullDomainName() {
        // When & Then
        assertThatThrownBy(() -> JavaPackageNameBuilder.buildJavaPackageName(null, "dto"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("SQL business domain name cannot be null or empty");
    }

    @Test
    void shouldValidateEmptyDomainName() {
        // When & Then
        assertThatThrownBy(() -> JavaPackageNameBuilder.buildJavaPackageName("", "dto"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("SQL business domain name cannot be null or empty");

        assertThatThrownBy(() -> JavaPackageNameBuilder.buildJavaPackageName("   ", "dto"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("SQL business domain name cannot be null or empty");
    }

    @Test
    void shouldValidateNullPackageType() {
        // When & Then
        assertThatThrownBy(() -> JavaPackageNameBuilder.buildJavaPackageName("customer", null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Package type cannot be null or empty");
    }

    @Test
    void shouldValidateEmptyPackageType() {
        // When & Then
        assertThatThrownBy(() -> JavaPackageNameBuilder.buildJavaPackageName("customer", ""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Package type cannot be null or empty");

        assertThatThrownBy(() -> JavaPackageNameBuilder.buildJavaPackageName("customer", "   "))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Package type cannot be null or empty");
    }

    @Test
    void shouldHandleSpecialCharactersInDomainName() {
        // Given
        String domainWithSpecialChars = "customer_order-details";

        // When
        String result = JavaPackageNameBuilder.buildJavaPackageName(domainWithSpecialChars, "dto");

        // Then
        assertThat(result).isEqualTo("com.jfeatures.msg.customer_order-details.dto");
    }

    @Test
    void shouldHandleLongDomainNames() {
        // Given
        String longDomain = "very_long_customer_order_details_report_summary";

        // When
        String result = JavaPackageNameBuilder.buildJavaPackageName(longDomain, "dto");

        // Then
        assertThat(result).isEqualTo("com.jfeatures.msg.very_long_customer_order_details_report_summary.dto");
    }

    @Test
    void shouldHandleSingleCharacterInputs() {
        // When
        String result = JavaPackageNameBuilder.buildJavaPackageName("a", "b");

        // Then
        assertThat(result).isEqualTo("com.jfeatures.msg.a.b");
    }
}