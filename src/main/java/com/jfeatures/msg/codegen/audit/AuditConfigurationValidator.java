package com.jfeatures.msg.codegen.audit;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * Validates audit configuration at startup.
 * Follows Vipin's Principle: Single responsibility - audit configuration validation only.
 */
@Slf4j
public class AuditConfigurationValidator {

    private AuditConfigurationValidator() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Validates that audit configuration is loaded correctly.
     * Throws exception if configuration is invalid or missing.
     */
    public static void validateConfiguration() {
        log.info("Validating audit configuration...");

        AuditConfiguration config = AuditConfiguration.getInstance();
        Map<AuditFieldType, List<String>> mappings = config.getAllMappings();

        if (mappings.isEmpty()) {
            String errorMsg = "Audit configuration is empty or failed to load from audit-config.yaml. " +
                             "Audit field detection will not work. Please check the configuration file exists in src/main/resources/";
            log.error(errorMsg);
            throw new IllegalStateException(errorMsg);
        }

        // Validate each audit field type has at least one column pattern
        int totalPatterns = 0;
        for (AuditFieldType fieldType : AuditFieldType.values()) {
            List<String> columns = mappings.get(fieldType);
            if (columns == null || columns.isEmpty()) {
                log.warn("No column patterns configured for audit field type: {}", fieldType);
            } else {
                totalPatterns += columns.size();
                log.debug("Audit field type {} has {} column patterns", fieldType, columns.size());
            }
        }

        if (totalPatterns == 0) {
            String errorMsg = "Audit configuration has no column patterns defined. " +
                             "Please check audit-config.yaml has proper field mappings under 'auditFields' section.";
            log.error(errorMsg);
            throw new IllegalStateException(errorMsg);
        }

        log.info("Audit configuration validated successfully. Total {} column patterns across {} field types.",
                 totalPatterns, mappings.keySet().size());
    }
}
