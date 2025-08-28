package com.jfeatures.msg.codegen.constants;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProjectConstantsTest {

    @Test
    void shouldHaveDefaultDestinationDirectory() {
        // Then
        assertThat(ProjectConstants.DEFAULT_DESTINATION_DIRECTORY).isNotNull();
        assertThat(ProjectConstants.DEFAULT_DESTINATION_DIRECTORY).isNotEmpty();
    }

    @Test
    void shouldHaveDefaultBusinessDomain() {
        // Then
        assertThat(ProjectConstants.DEFAULT_BUSINESS_DOMAIN).isNotNull();
        assertThat(ProjectConstants.DEFAULT_BUSINESS_DOMAIN).isNotEmpty();
        assertThat(ProjectConstants.DEFAULT_BUSINESS_DOMAIN).isEqualTo("Customer");
    }
}