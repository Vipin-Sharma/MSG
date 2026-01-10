package com.jfeatures.msg.codegen.audit;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.*;

/**
 * Configuration loader for audit field mappings.
 * Loads audit-config.yaml and provides methods to identify audit fields.
 */
@Slf4j
@Data
public class AuditConfiguration {

    private static final String CONFIG_FILE = "audit-config.yaml";
    private static AuditConfiguration instance;

    private Map<AuditFieldType, List<String>> auditFieldMappings;

    private AuditConfiguration() {
        this.auditFieldMappings = new EnumMap<>(AuditFieldType.class);
        loadConfiguration();
    }

    /**
     * Get singleton instance of AuditConfiguration.
     * @return AuditConfiguration instance
     */
    public static synchronized AuditConfiguration getInstance() {
        if (instance == null) {
            instance = new AuditConfiguration();
        }
        return instance;
    }

    /**
     * Load audit configuration from YAML file.
     */
    @SuppressWarnings("unchecked")
    private void loadConfiguration() {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (inputStream == null) {
                log.warn("Audit configuration file '{}' not found. Audit field detection will be disabled.", CONFIG_FILE);
                return;
            }

            Yaml yaml = new Yaml();
            Map<String, Object> config = yaml.load(inputStream);

            if (config == null || !config.containsKey("auditFields")) {
                log.warn("Invalid audit configuration format. 'auditFields' section not found.");
                return;
            }

            Map<String, List<String>> auditFields = (Map<String, List<String>>) config.get("auditFields");

            // Parse each audit field type
            auditFieldMappings.put(AuditFieldType.CREATED_AT,
                normalizeColumnNames(auditFields.getOrDefault("createdAt", Collections.emptyList())));
            auditFieldMappings.put(AuditFieldType.CREATED_BY,
                normalizeColumnNames(auditFields.getOrDefault("createdBy", Collections.emptyList())));
            auditFieldMappings.put(AuditFieldType.UPDATED_AT,
                normalizeColumnNames(auditFields.getOrDefault("updatedAt", Collections.emptyList())));
            auditFieldMappings.put(AuditFieldType.UPDATED_BY,
                normalizeColumnNames(auditFields.getOrDefault("updatedBy", Collections.emptyList())));

            log.info("Loaded audit configuration successfully. Mappings: {}", auditFieldMappings);

        } catch (Exception e) {
            log.error("Failed to load audit configuration from '{}'", CONFIG_FILE, e);
        }
    }

    /**
     * Normalize column names to lowercase for case-insensitive matching.
     */
    private List<String> normalizeColumnNames(List<String> columnNames) {
        return columnNames.stream()
                .map(String::toLowerCase)
                .toList();
    }

    /**
     * Check if a column name matches any audit field pattern.
     * @param columnName Column name to check
     * @return Optional containing AuditFieldType if match found
     */
    public Optional<AuditFieldType> getAuditFieldType(String columnName) {
        if (columnName == null || columnName.trim().isEmpty()) {
            return Optional.empty();
        }

        String normalizedColumnName = columnName.toLowerCase().trim();

        for (Map.Entry<AuditFieldType, List<String>> entry : auditFieldMappings.entrySet()) {
            if (entry.getValue().contains(normalizedColumnName)) {
                return Optional.of(entry.getKey());
            }
        }

        return Optional.empty();
    }

    /**
     * Check if a column is an audit field.
     * @param columnName Column name to check
     * @return true if column is an audit field
     */
    public boolean isAuditField(String columnName) {
        return getAuditFieldType(columnName).isPresent();
    }

    /**
     * Get all configured column names for a specific audit field type.
     * @param auditFieldType The audit field type
     * @return List of column names
     */
    public List<String> getColumnNamesForType(AuditFieldType auditFieldType) {
        return auditFieldMappings.getOrDefault(auditFieldType, Collections.emptyList());
    }

    /**
     * Get all audit field type mappings.
     * @return Map of audit field types to column names
     */
    public Map<AuditFieldType, List<String>> getAllMappings() {
        return Collections.unmodifiableMap(auditFieldMappings);
    }
}
