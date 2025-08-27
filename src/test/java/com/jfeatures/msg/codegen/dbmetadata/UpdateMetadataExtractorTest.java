package com.jfeatures.msg.codegen.dbmetadata;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ParameterMetaData;
import java.sql.Types;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateMetadataExtractorTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Mock
    private Connection connection;

    @Mock
    private DatabaseMetaData dbMetaData;

    @Mock
    private ResultSet columnsResultSet;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ParameterMetaData parameterMetaData;

    private UpdateMetadataExtractor extractor;

    @BeforeEach
    void setUp() throws Exception {
        extractor = new UpdateMetadataExtractor(dataSource, jdbcTemplate);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getMetaData()).thenReturn(dbMetaData);
    }

    @Test
    void shouldExtractSimpleUpdateMetadata() throws Exception {
        // Given
        String updateSql = "UPDATE customer SET first_name = ?, last_name = ? WHERE customer_id = ?";
        setupColumnMetadata();
        setupParameterMetadata(3);

        // When
        UpdateMetadata result = extractor.extractUpdateMetadata(updateSql);

        // Then
        assertThat(result.tableName()).isEqualTo("customer");
        assertThat(result.originalSql()).isEqualTo(updateSql);
        assertThat(result.setColumns()).hasSize(2);
        assertThat(result.setColumns().get(0).columnName()).isEqualTo("first_name");
        assertThat(result.setColumns().get(1).columnName()).isEqualTo("last_name");
        assertThat(result.whereColumns()).hasSize(1);
    }

    @Test
    void shouldExtractUpdateWithMultipleWhereConditions() throws Exception {
        // Given
        String updateSql = "UPDATE customer SET email = ? WHERE customer_id = ? AND active = ?";
        setupColumnMetadata();
        setupParameterMetadata(3);

        // When
        UpdateMetadata result = extractor.extractUpdateMetadata(updateSql);

        // Then
        assertThat(result.tableName()).isEqualTo("customer");
        assertThat(result.setColumns()).hasSize(1);
        assertThat(result.setColumns().get(0).columnName()).isEqualTo("email");
        assertThat(result.whereColumns()).hasSize(2);
    }

    @Test
    void shouldExtractComplexUpdateWithJoins() throws Exception {
        // Given
        String complexUpdateSql = """
            UPDATE customer 
            SET customer.first_name = ?,
                customer.email = ?
            FROM customer 
            INNER JOIN address ON customer.address_id = address.address_id
            WHERE address.city_id = ?
            """;
        setupColumnMetadata();
        setupParameterMetadata(3);

        // When
        UpdateMetadata result = extractor.extractUpdateMetadata(complexUpdateSql);

        // Then
        assertThat(result.tableName()).isEqualTo("customer");
        assertThat(result.setColumns()).hasSize(2);
        assertThat(result.whereColumns()).hasSize(1);
    }

    @Test
    void shouldHandleUpdateWithoutWhereClause() throws Exception {
        // Given
        String updateSql = "UPDATE customer SET active = ?";
        setupColumnMetadata();
        setupParameterMetadata(1);

        // When
        UpdateMetadata result = extractor.extractUpdateMetadata(updateSql);

        // Then
        assertThat(result.tableName()).isEqualTo("customer");
        assertThat(result.setColumns()).hasSize(1);
        assertThat(result.setColumns().get(0).columnName()).isEqualTo("active");
        assertThat(result.whereColumns()).isEmpty();
    }

    @Test
    void shouldThrowExceptionForNonUpdateStatement() {
        // Given
        String selectSql = "SELECT * FROM customer WHERE customer_id = ?";

        // When & Then
        assertThatThrownBy(() -> extractor.extractUpdateMetadata(selectSql))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("SQL is not an UPDATE statement");
    }

    @Test
    void shouldHandleParameterMetadataExtractionFailure() throws Exception {
        // Given
        String updateSql = "UPDATE customer SET first_name = ? WHERE customer_id = ?";
        setupColumnMetadata();
        when(connection.prepareStatement(updateSql)).thenThrow(new RuntimeException("DB connection failed"));

        // When
        UpdateMetadata result = extractor.extractUpdateMetadata(updateSql);

        // Then - Should still extract basic metadata without parameter details
        assertThat(result.tableName()).isEqualTo("customer");
        assertThat(result.setColumns()).hasSize(1);
        assertThat(result.setColumns().get(0).columnName()).isEqualTo("first_name");
        // WHERE columns might be empty or contain fallback data
    }

    @Test
    void shouldExtractUpdateWithSubquery() throws Exception {
        // Given
        String updateWithSubquery = """
            UPDATE customer 
            SET email = ? 
            WHERE customer_id IN (
                SELECT customer_id 
                FROM rental 
                WHERE return_date IS NULL
                AND rental_date < ?
            )
            """;
        setupColumnMetadata();
        setupParameterMetadata(2);

        // When
        UpdateMetadata result = extractor.extractUpdateMetadata(updateWithSubquery);

        // Then
        assertThat(result.tableName()).isEqualTo("customer");
        assertThat(result.setColumns()).hasSize(1);
        assertThat(result.whereColumns()).hasSize(1); // Parameters after SET clause
    }

    private void setupColumnMetadata() throws Exception {
        when(dbMetaData.getColumns(isNull(), isNull(), eq("customer"), isNull()))
                .thenReturn(columnsResultSet);

        // Mock multiple columns being returned
        when(columnsResultSet.next())
                .thenReturn(true, true, true, true, false); // Return true 4 times, then false

        when(columnsResultSet.getString("COLUMN_NAME"))
                .thenReturn("customer_id", "first_name", "last_name", "email");

        when(columnsResultSet.getString("TYPE_NAME"))
                .thenReturn("INT", "VARCHAR", "VARCHAR", "VARCHAR");

        when(columnsResultSet.getInt("DATA_TYPE"))
                .thenReturn(Types.INTEGER, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR);

        when(columnsResultSet.getInt("NULLABLE"))
                .thenReturn(0, 1, 1, 1); // customer_id NOT NULL, others nullable
    }

    private void setupParameterMetadata(int paramCount) throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.getParameterMetaData()).thenReturn(parameterMetaData);
        when(parameterMetaData.getParameterCount()).thenReturn(paramCount);

        // Setup parameter metadata for each parameter
        for (int i = 1; i <= paramCount; i++) {
            when(parameterMetaData.getParameterTypeName(i)).thenReturn("VARCHAR");
            when(parameterMetaData.getParameterType(i)).thenReturn(Types.VARCHAR);
            when(parameterMetaData.isNullable(i)).thenReturn(1);
        }
    }
}