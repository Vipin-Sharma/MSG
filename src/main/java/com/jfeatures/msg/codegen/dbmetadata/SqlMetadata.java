package com.jfeatures.msg.codegen.dbmetadata;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Component
public class SqlMetadata {

    private JdbcTemplate jdbcTemplate;

    public SqlMetadata(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<ColumnMetadata> getColumnMetadata(String query) throws SQLException {

        List<ColumnMetadata> columnMetadataList = new ArrayList<>();
        jdbcTemplate.query(query, new RowMapper<ColumnMetadata>() {
            @Override
            public ColumnMetadata mapRow(ResultSet rs, int rowNum) throws SQLException {
                ResultSetMetaData metadata = rs.getMetaData();

                int columnCount = metadata.getColumnCount();

                for (int i = 1; i <= columnCount; i++) {
                    ColumnMetadata columnMetadata = new ColumnMetadata();
                    columnMetadata.setColumnName(metadata.getColumnName(i));
                    columnMetadata.setColumnAlias(metadata.getColumnLabel(i));
                    columnMetadata.setTableName(metadata.getTableName(i));
                    columnMetadata.setColumnType(metadata.getColumnType(i));
                    columnMetadata.setColumnTypeName(metadata.getColumnTypeName(i));
                    columnMetadata.setColumnClassName(metadata.getColumnClassName(i));
                    columnMetadata.setColumnDisplaySize(metadata.getColumnDisplaySize(i));
                    columnMetadata.setPrecision(metadata.getPrecision(i));
                    columnMetadata.setScale(metadata.getScale(i));
                    columnMetadata.setIsNullable(metadata.isNullable(i));
                    columnMetadata.setAutoIncrement(metadata.isAutoIncrement(i));
                    columnMetadata.setCaseSensitive(metadata.isCaseSensitive(i));
                    columnMetadata.setReadOnly(metadata.isReadOnly(i));
                    columnMetadata.setWritable(metadata.isWritable(i));
                    columnMetadata.setDefinitelyWritable(metadata.isDefinitelyWritable(i));
                    columnMetadata.setCurrency(metadata.isCurrency(i));
                    columnMetadata.setSigned(metadata.isSigned(i));

                    columnMetadataList.add(columnMetadata);
                }

                return null;
            }
        });
        return columnMetadataList;
    }
}
