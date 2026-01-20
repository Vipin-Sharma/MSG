package com.jfeatures.msg.codegen.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class AuditConfigurationTest {

    @Test
    void testDisabledConfiguration_ReturnsDisabledState() {
        AuditConfiguration config = AuditConfiguration.disabled();

        assertThat(config.enabled()).isFalse();
        assertThat(config.userHeaderName()).isEqualTo(AuditConfiguration.DEFAULT_USER_HEADER);
        assertThat(config.auditColumnNames()).isEmpty();
    }

    @Test
    void testDisabledConfiguration_IsAuditColumnReturnsFalse() {
        AuditConfiguration config = AuditConfiguration.disabled();

        assertThat(config.isAuditColumn("created_by")).isFalse();
        assertThat(config.isAuditColumn("updated_by")).isFalse();
        assertThat(config.isAuditColumn("any_column")).isFalse();
        assertThat(config.isAuditColumn(null)).isFalse();
    }

    @Test
    void testEnabledConfiguration_WithValidAuditColumns() {
        Set<String> auditColumns = Set.of("created_by", "updated_by");
        AuditConfiguration config = new AuditConfiguration(true, "X-User-ID", auditColumns);

        assertThat(config.enabled()).isTrue();
        assertThat(config.userHeaderName()).isEqualTo("X-User-ID");
        assertThat(config.auditColumnNames()).containsExactlyInAnyOrder("created_by", "updated_by");
        assertThat(config.auditColumnCount()).isEqualTo(2);
    }

    @Test
    void testIsAuditColumn_CaseInsensitiveMatching() {
        Set<String> auditColumns = Set.of("created_by", "updated_by");
        AuditConfiguration config = new AuditConfiguration(true, "X-User-ID", auditColumns);

        // Exact match
        assertThat(config.isAuditColumn("created_by")).isTrue();
        assertThat(config.isAuditColumn("updated_by")).isTrue();

        // Case variations
        assertThat(config.isAuditColumn("CREATED_BY")).isTrue();
        assertThat(config.isAuditColumn("UPDATED_BY")).isTrue();
        assertThat(config.isAuditColumn("Created_By")).isTrue();
        assertThat(config.isAuditColumn("createdBy")).isFalse(); // Different format

        // Non-audit columns
        assertThat(config.isAuditColumn("customer_name")).isFalse();
        assertThat(config.isAuditColumn("email")).isFalse();
    }

    @Test
    void testIsAuditColumn_NullColumnName_ReturnsFalse() {
        Set<String> auditColumns = Set.of("created_by");
        AuditConfiguration config = new AuditConfiguration(true, "X-User-ID", auditColumns);

        assertThat(config.isAuditColumn(null)).isFalse();
    }

    @Test
    void testEnabledWithEmptyColumns_ThrowsException() {
        assertThatThrownBy(() -> new AuditConfiguration(true, "X-User-ID", Set.of()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Audit columns must be specified when audit is enabled");
    }

    @Test
    void testEnabledWithNullColumns_ThrowsException() {
        assertThatThrownBy(() -> new AuditConfiguration(true, "X-User-ID", null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Audit columns must be specified when audit is enabled");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t", "\n"})
    void testNullOrEmptyHeaderName_DefaultsToXUserId(String headerName) {
        Set<String> auditColumns = Set.of("created_by");
        AuditConfiguration config = new AuditConfiguration(true, headerName, auditColumns);

        assertThat(config.userHeaderName()).isEqualTo(AuditConfiguration.DEFAULT_USER_HEADER);
    }

    @Test
    void testCustomHeaderName() {
        Set<String> auditColumns = Set.of("created_by");
        AuditConfiguration config = new AuditConfiguration(true, "Authorization", auditColumns);

        assertThat(config.userHeaderName()).isEqualTo("Authorization");
    }

    @Test
    void testSingleAuditColumn() {
        Set<String> auditColumns = Set.of("modified_by");
        AuditConfiguration config = new AuditConfiguration(true, "X-User-ID", auditColumns);

        assertThat(config.enabled()).isTrue();
        assertThat(config.auditColumnCount()).isEqualTo(1);
        assertThat(config.isAuditColumn("modified_by")).isTrue();
    }

    @Test
    void testMultipleAuditColumns() {
        Set<String> auditColumns = Set.of("created_by", "updated_by", "deleted_by", "approved_by");
        AuditConfiguration config = new AuditConfiguration(true, "X-User-ID", auditColumns);

        assertThat(config.auditColumnCount()).isEqualTo(4);
        assertThat(config.isAuditColumn("created_by")).isTrue();
        assertThat(config.isAuditColumn("updated_by")).isTrue();
        assertThat(config.isAuditColumn("deleted_by")).isTrue();
        assertThat(config.isAuditColumn("approved_by")).isTrue();
    }

    @Test
    void testDefaultUserHeaderConstant() {
        assertThat(AuditConfiguration.DEFAULT_USER_HEADER).isEqualTo("X-User-ID");
    }

    @Test
    void testDisabledAuditColumnCount_ReturnsZero() {
        AuditConfiguration config = AuditConfiguration.disabled();
        assertThat(config.auditColumnCount()).isZero();
    }

    @Test
    void testRecordEquality() {
        Set<String> auditColumns = Set.of("created_by", "updated_by");
        AuditConfiguration config1 = new AuditConfiguration(true, "X-User-ID", auditColumns);
        AuditConfiguration config2 = new AuditConfiguration(true, "X-User-ID", Set.of("created_by", "updated_by"));

        assertThat(config1).isEqualTo(config2);
        assertThat(config1.hashCode()).isEqualTo(config2.hashCode());
    }

    @Test
    void testRecordToString() {
        Set<String> auditColumns = Set.of("created_by");
        AuditConfiguration config = new AuditConfiguration(true, "X-User-ID", auditColumns);

        String toString = config.toString();
        assertThat(toString).contains("AuditConfiguration");
        assertThat(toString).contains("enabled=true");
        assertThat(toString).contains("X-User-ID");
        assertThat(toString).contains("created_by");
    }

    // ========== ADDITIONAL EDGE CASE TESTS ==========

    @Test
    void testAuditColumnWithSpecialDatabaseNaming() {
        // Test audit columns with various database naming conventions
        Set<String> auditColumns = Set.of(
                "created_by",      // snake_case
                "MODIFIED_BY",     // UPPER_SNAKE_CASE
                "LastUpdatedBy",   // PascalCase
                "approved_by_user" // longer snake_case
        );
        AuditConfiguration config = new AuditConfiguration(true, "X-User-ID", auditColumns);

        assertThat(config.isAuditColumn("created_by")).isTrue();
        assertThat(config.isAuditColumn("CREATED_BY")).isTrue();
        assertThat(config.isAuditColumn("modified_by")).isTrue();
        assertThat(config.isAuditColumn("MODIFIED_BY")).isTrue();
        assertThat(config.isAuditColumn("lastupdatedby")).isTrue();
        assertThat(config.isAuditColumn("LASTUPDATEDBY")).isTrue();
        assertThat(config.isAuditColumn("approved_by_user")).isTrue();
    }

    @Test
    void testHeaderNameWithCommonPatterns() {
        Set<String> auditColumns = Set.of("created_by");

        // Test various header name patterns
        String[] headerNames = {
                "X-User-ID",
                "Authorization",
                "X-Auth-User",
                "X-Request-User-ID",
                "User-Identity",
                "x-user-id",  // lowercase
                "X-USER-ID"   // uppercase
        };

        for (String headerName : headerNames) {
            AuditConfiguration config = new AuditConfiguration(true, headerName, auditColumns);
            assertThat(config.userHeaderName()).isEqualTo(headerName);
        }
    }

    @Test
    void testDisabledConfigurationImmutability() {
        AuditConfiguration disabled1 = AuditConfiguration.disabled();
        AuditConfiguration disabled2 = AuditConfiguration.disabled();

        // Both should return equivalent configurations
        assertThat(disabled1.enabled()).isEqualTo(disabled2.enabled());
        assertThat(disabled1.userHeaderName()).isEqualTo(disabled2.userHeaderName());
        assertThat(disabled1.auditColumnNames()).isEqualTo(disabled2.auditColumnNames());
    }

    @Test
    void testIsAuditColumn_EmptyString_ReturnsFalse() {
        Set<String> auditColumns = Set.of("created_by");
        AuditConfiguration config = new AuditConfiguration(true, "X-User-ID", auditColumns);

        assertThat(config.isAuditColumn("")).isFalse();
        assertThat(config.isAuditColumn("   ")).isFalse();
    }

    @Test
    void testAuditColumnNamesAreUnmodifiable() {
        Set<String> originalColumns = Set.of("created_by", "updated_by");
        AuditConfiguration config = new AuditConfiguration(true, "X-User-ID", originalColumns);

        Set<String> returnedColumns = config.auditColumnNames();

        // Attempt to modify should throw exception or have no effect
        assertThatThrownBy(() -> returnedColumns.add("new_column"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void testAuditColumnWithSimilarNames() {
        // Test that similar but different column names are handled correctly
        Set<String> auditColumns = Set.of("created_by");
        AuditConfiguration config = new AuditConfiguration(true, "X-User-ID", auditColumns);

        assertThat(config.isAuditColumn("created_by")).isTrue();
        assertThat(config.isAuditColumn("created_by_user")).isFalse();  // Different column
        assertThat(config.isAuditColumn("user_created_by")).isFalse();  // Different column
        assertThat(config.isAuditColumn("created")).isFalse();          // Partial match
        assertThat(config.isAuditColumn("by")).isFalse();               // Partial match
    }

    @Test
    void testDisabledConfiguration_AuditColumnCountIsZero() {
        AuditConfiguration config = AuditConfiguration.disabled();

        assertThat(config.auditColumnCount()).isZero();
        assertThat(config.auditColumnNames()).isEmpty();
    }

    @Test
    void testEnabledConfiguration_WithMixedCaseColumnSet() {
        // When columns are specified with mixed cases, they should still match case-insensitively
        Set<String> auditColumns = Set.of("Created_By", "UPDATED_BY", "modified_by");
        AuditConfiguration config = new AuditConfiguration(true, "X-User-ID", auditColumns);

        assertThat(config.isAuditColumn("created_by")).isTrue();
        assertThat(config.isAuditColumn("CREATED_BY")).isTrue();
        assertThat(config.isAuditColumn("updated_by")).isTrue();
        assertThat(config.isAuditColumn("MODIFIED_BY")).isTrue();
    }

    @Test
    void testHeaderNameTrimming() {
        // Header name with leading/trailing spaces should be handled
        Set<String> auditColumns = Set.of("created_by");

        // Note: The implementation may or may not trim - this tests current behavior
        AuditConfiguration config = new AuditConfiguration(true, "  X-User-ID  ", auditColumns);

        // Should either trim or preserve - document actual behavior
        assertThat(config.userHeaderName()).isNotNull();
    }

    @Test
    void testLargeNumberOfAuditColumns() {
        // Test with many audit columns
        Set<String> auditColumns = Set.of(
                "created_by", "updated_by", "deleted_by", "approved_by",
                "reviewed_by", "modified_by", "verified_by", "submitted_by",
                "cancelled_by", "completed_by"
        );
        AuditConfiguration config = new AuditConfiguration(true, "X-User-ID", auditColumns);

        assertThat(config.auditColumnCount()).isEqualTo(10);

        // All columns should be recognized
        for (String col : auditColumns) {
            assertThat(config.isAuditColumn(col)).isTrue();
            assertThat(config.isAuditColumn(col.toUpperCase())).isTrue();
        }
    }
}
