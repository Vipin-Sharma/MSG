package com.jfeatures.msg.codegen.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class JavaPackageNameBuilderTest {

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
            Arguments.of("ORDER_DETAILS", "dao", "com.jfeatures.msg.order_details.dao"),
            Arguments.of("customer_order-details", "dto", "com.jfeatures.msg.customer_order-details.dto"),
            Arguments.of(
                "very_long_customer_order_details_report_summary",
                "dto",
                "com.jfeatures.msg.very_long_customer_order_details_report_summary.dto"
            ),
            Arguments.of("a", "b", "com.jfeatures.msg.a.b")
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"Customer", "ORDER", "product_info", "user-account", "123Order"})
    void shouldHandleDifferentDomainNameFormats(String domain) {
        // When
        String result = JavaPackageNameBuilder.buildJavaPackageName(domain, "dto");

        // Then
        assertThat(result)
            .startsWith("com.jfeatures.msg.")
            .contains(domain.toLowerCase()) // Domain names are converted to lowercase
            .endsWith(".dto");
    }

    @ParameterizedTest
    @MethodSource("invalidDomainNames")
    void shouldValidateDomainName(String invalidDomain) {
        assertThatThrownBy(() -> JavaPackageNameBuilder.buildJavaPackageName(invalidDomain, "dto"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("SQL business domain name cannot be null or empty");
    }

    private static Stream<String> invalidDomainNames() {
        return Stream.of(null, "", "   ");
    }

    @ParameterizedTest
    @MethodSource("invalidPackageTypes")
    void shouldValidatePackageType(String invalidPackageType) {
        assertThatThrownBy(() -> JavaPackageNameBuilder.buildJavaPackageName("customer", invalidPackageType))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Package type cannot be null or empty");
    }

    private static Stream<String> invalidPackageTypes() {
        return Stream.of(null, "", "   ");
    }
}