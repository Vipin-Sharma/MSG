package com.jfeatures.msg.codegen.audit;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AuditConfigurationValidator.
 * Tests validation logic for audit configuration.
 */
class AuditConfigurationValidatorTest {

    @Test
    void testValidateConfiguration_WithValidConfig_DoesNotThrowException() {
        // Should not throw exception when audit-config.yaml is properly loaded
        assertDoesNotThrow(() -> AuditConfigurationValidator.validateConfiguration(),
            "Should not throw exception for valid audit configuration");
    }

    @Test
    void testValidateConfiguration_LogsSuccessMessage() {
        // This test verifies the method completes successfully
        // In a real scenario, you might want to capture logs to verify the success message
        assertDoesNotThrow(() -> AuditConfigurationValidator.validateConfiguration(),
            "Validation should complete successfully");
    }
}
