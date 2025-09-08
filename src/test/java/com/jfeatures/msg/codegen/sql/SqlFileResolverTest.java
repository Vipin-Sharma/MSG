package com.jfeatures.msg.codegen.sql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.jfeatures.msg.codegen.constants.ProjectConstants;
import com.jfeatures.msg.sql.ReadFileFromResources;
import java.io.IOException;
import java.io.UncheckedIOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SqlFileResolverTest {

    private SqlFileResolver resolver;
    
    @BeforeEach
    void setUp() {
        resolver = new SqlFileResolver();
    }
    
    @Test
    void testLocateAndReadSqlFile_SpecifiedFile_ReadsSpecifiedFile() {
        // Given
        String specifiedFile = "custom_sql.sql";
        String expectedSql = "SELECT * FROM custom_table WHERE id = ?";
        
        try (MockedStatic<ReadFileFromResources> mockReadFile = mockStatic(ReadFileFromResources.class)) {
            mockReadFile.when(() -> ReadFileFromResources.readFileFromResources(specifiedFile))
                       .thenReturn(expectedSql);
            
            // When
            String result = resolver.locateAndReadSqlFile(specifiedFile);
            
            // Then
            assertEquals(expectedSql, result);
            mockReadFile.verify(() -> ReadFileFromResources.readFileFromResources(specifiedFile));
        }
    }
    
    @Test
    void testLocateAndReadSqlFile_NullSpecifiedFile_TriesDefaultFiles() {
        // Given
        String updateSql = "UPDATE customers SET name = ? WHERE id = ?";
        
        try (MockedStatic<ReadFileFromResources> mockReadFile = mockStatic(ReadFileFromResources.class)) {
            mockReadFile.when(() -> ReadFileFromResources.readFileFromResources(ProjectConstants.DEFAULT_UPDATE_SQL_FILE))
                       .thenReturn(updateSql);
            
            // When
            String result = resolver.locateAndReadSqlFile(null);
            
            // Then
            assertEquals(updateSql, result);
            mockReadFile.verify(() -> ReadFileFromResources.readFileFromResources(ProjectConstants.DEFAULT_UPDATE_SQL_FILE));
        }
    }
    
    @Test
    void testLocateAndReadSqlFile_EmptySpecifiedFile_TriesDefaultFiles() {
        // Given
        String insertSql = "INSERT INTO customers (name, email) VALUES (?, ?)";
        
        try (MockedStatic<ReadFileFromResources> mockReadFile = mockStatic(ReadFileFromResources.class)) {
            // UPDATE file doesn't exist, but INSERT does
            mockReadFile.when(() -> ReadFileFromResources.readFileFromResources(ProjectConstants.DEFAULT_UPDATE_SQL_FILE))
                       .thenThrow(new UncheckedIOException("File not found", new IOException()));
            mockReadFile.when(() -> ReadFileFromResources.readFileFromResources(ProjectConstants.DEFAULT_INSERT_SQL_FILE))
                       .thenReturn(insertSql);
            
            // When
            String result = resolver.locateAndReadSqlFile("   ");
            
            // Then
            assertEquals(insertSql, result);
            mockReadFile.verify(() -> ReadFileFromResources.readFileFromResources(ProjectConstants.DEFAULT_UPDATE_SQL_FILE));
            mockReadFile.verify(() -> ReadFileFromResources.readFileFromResources(ProjectConstants.DEFAULT_INSERT_SQL_FILE));
        }
    }
    
    @Test
    void testLocateAndReadSqlFile_UpdateFileNotFound_TriesInsertFile() {
        // Given
        String insertSql = "INSERT INTO customers (name, email) VALUES (?, ?)";
        
        try (MockedStatic<ReadFileFromResources> mockReadFile = mockStatic(ReadFileFromResources.class)) {
            mockReadFile.when(() -> ReadFileFromResources.readFileFromResources(ProjectConstants.DEFAULT_UPDATE_SQL_FILE))
                       .thenThrow(new UncheckedIOException("UPDATE file not found", new IOException()));
            mockReadFile.when(() -> ReadFileFromResources.readFileFromResources(ProjectConstants.DEFAULT_INSERT_SQL_FILE))
                       .thenReturn(insertSql);
            
            // When
            String result = resolver.locateAndReadSqlFile(null);
            
            // Then
            assertEquals(insertSql, result);
            mockReadFile.verify(() -> ReadFileFromResources.readFileFromResources(ProjectConstants.DEFAULT_UPDATE_SQL_FILE));
            mockReadFile.verify(() -> ReadFileFromResources.readFileFromResources(ProjectConstants.DEFAULT_INSERT_SQL_FILE));
        }
    }
    
    @Test
    void testLocateAndReadSqlFile_UpdateAndInsertNotFound_TriesDeleteFile() {
        // Given
        String deleteSql = "DELETE FROM customers WHERE id = ?";
        
        try (MockedStatic<ReadFileFromResources> mockReadFile = mockStatic(ReadFileFromResources.class)) {
            mockReadFile.when(() -> ReadFileFromResources.readFileFromResources(ProjectConstants.DEFAULT_UPDATE_SQL_FILE))
                       .thenThrow(new UncheckedIOException("UPDATE file not found", new IOException()));
            mockReadFile.when(() -> ReadFileFromResources.readFileFromResources(ProjectConstants.DEFAULT_INSERT_SQL_FILE))
                       .thenThrow(new UncheckedIOException("INSERT file not found", new IOException()));
            mockReadFile.when(() -> ReadFileFromResources.readFileFromResources(ProjectConstants.DEFAULT_DELETE_SQL_FILE))
                       .thenReturn(deleteSql);
            
            // When
            String result = resolver.locateAndReadSqlFile(null);
            
            // Then
            assertEquals(deleteSql, result);
            mockReadFile.verify(() -> ReadFileFromResources.readFileFromResources(ProjectConstants.DEFAULT_UPDATE_SQL_FILE));
            mockReadFile.verify(() -> ReadFileFromResources.readFileFromResources(ProjectConstants.DEFAULT_INSERT_SQL_FILE));
            mockReadFile.verify(() -> ReadFileFromResources.readFileFromResources(ProjectConstants.DEFAULT_DELETE_SQL_FILE));
        }
    }
    
    @Test
    void testLocateAndReadSqlFile_OnlySelectFileExists_ReturnsSelectSql() {
        // Given
        String selectSql = "SELECT * FROM customers WHERE id = ?";
        
        try (MockedStatic<ReadFileFromResources> mockReadFile = mockStatic(ReadFileFromResources.class)) {
            // All other files not found, only SELECT exists
            mockReadFile.when(() -> ReadFileFromResources.readFileFromResources(ProjectConstants.DEFAULT_UPDATE_SQL_FILE))
                       .thenThrow(new UncheckedIOException("UPDATE file not found", new IOException()));
            mockReadFile.when(() -> ReadFileFromResources.readFileFromResources(ProjectConstants.DEFAULT_INSERT_SQL_FILE))
                       .thenThrow(new UncheckedIOException("INSERT file not found", new IOException()));
            mockReadFile.when(() -> ReadFileFromResources.readFileFromResources(ProjectConstants.DEFAULT_DELETE_SQL_FILE))
                       .thenThrow(new UncheckedIOException("DELETE file not found", new IOException()));
            mockReadFile.when(() -> ReadFileFromResources.readFileFromResources(ProjectConstants.DEFAULT_SELECT_SQL_FILE))
                       .thenReturn(selectSql);
            
            // When
            String result = resolver.locateAndReadSqlFile(null);
            
            // Then
            assertEquals(selectSql, result);
            mockReadFile.verify(() -> ReadFileFromResources.readFileFromResources(ProjectConstants.DEFAULT_UPDATE_SQL_FILE));
            mockReadFile.verify(() -> ReadFileFromResources.readFileFromResources(ProjectConstants.DEFAULT_INSERT_SQL_FILE));
            mockReadFile.verify(() -> ReadFileFromResources.readFileFromResources(ProjectConstants.DEFAULT_DELETE_SQL_FILE));
            mockReadFile.verify(() -> ReadFileFromResources.readFileFromResources(ProjectConstants.DEFAULT_SELECT_SQL_FILE));
        }
    }
    
    @Test
    void testLocateAndReadSqlFile_NoDefaultFilesExist_ThrowsException() {
        // Given
        try (MockedStatic<ReadFileFromResources> mockReadFile = mockStatic(ReadFileFromResources.class)) {
            // All files not found
            mockReadFile.when(() -> ReadFileFromResources.readFileFromResources(anyString()))
                       .thenThrow(new UncheckedIOException("File not found", new IOException()));
            
            // When & Then
            RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> resolver.locateAndReadSqlFile(null)
            );
            
            assertTrue(exception.getMessage().contains("No default SQL files found"));
            assertTrue(exception.getMessage().contains(ProjectConstants.DEFAULT_UPDATE_SQL_FILE));
            assertTrue(exception.getMessage().contains(ProjectConstants.DEFAULT_INSERT_SQL_FILE));
            assertTrue(exception.getMessage().contains(ProjectConstants.DEFAULT_DELETE_SQL_FILE));
            assertTrue(exception.getMessage().contains(ProjectConstants.DEFAULT_SELECT_SQL_FILE));
            
            // Verify all files were attempted
            mockReadFile.verify(() -> ReadFileFromResources.readFileFromResources(ProjectConstants.DEFAULT_UPDATE_SQL_FILE));
            mockReadFile.verify(() -> ReadFileFromResources.readFileFromResources(ProjectConstants.DEFAULT_INSERT_SQL_FILE));
            mockReadFile.verify(() -> ReadFileFromResources.readFileFromResources(ProjectConstants.DEFAULT_DELETE_SQL_FILE));
            mockReadFile.verify(() -> ReadFileFromResources.readFileFromResources(ProjectConstants.DEFAULT_SELECT_SQL_FILE));
        }
    }
    
    @Test
    void testLocateAndReadSqlFile_SpecifiedFileThrowsException_PropagatesException() {
        // Given
        String specifiedFile = "nonexistent.sql";
        UncheckedIOException fileException = new UncheckedIOException("Specified file not found", new IOException());
        
        try (MockedStatic<ReadFileFromResources> mockReadFile = mockStatic(ReadFileFromResources.class)) {
            mockReadFile.when(() -> ReadFileFromResources.readFileFromResources(specifiedFile))
                       .thenThrow(fileException);
            
            // When & Then
            UncheckedIOException exception = assertThrows(
                UncheckedIOException.class,
                () -> resolver.locateAndReadSqlFile(specifiedFile)
            );
            
            assertEquals("Specified file not found", exception.getMessage());
            mockReadFile.verify(() -> ReadFileFromResources.readFileFromResources(specifiedFile));
        }
    }
    
    @Test
    void testLocateAndReadSqlFile_WhitespaceOnlyFileName_TriesDefaultFiles() {
        // Given
        String deleteSql = "DELETE FROM customers WHERE id = ?";
        
        try (MockedStatic<ReadFileFromResources> mockReadFile = mockStatic(ReadFileFromResources.class)) {
            // UPDATE and INSERT files don't exist, DELETE does
            mockReadFile.when(() -> ReadFileFromResources.readFileFromResources(ProjectConstants.DEFAULT_UPDATE_SQL_FILE))
                       .thenThrow(new UncheckedIOException("UPDATE file not found", new IOException()));
            mockReadFile.when(() -> ReadFileFromResources.readFileFromResources(ProjectConstants.DEFAULT_INSERT_SQL_FILE))
                       .thenThrow(new UncheckedIOException("INSERT file not found", new IOException()));
            mockReadFile.when(() -> ReadFileFromResources.readFileFromResources(ProjectConstants.DEFAULT_DELETE_SQL_FILE))
                       .thenReturn(deleteSql);
            
            // When
            String result = resolver.locateAndReadSqlFile("\t  \n  ");
            
            // Then
            assertEquals(deleteSql, result);
        }
    }
    
    @Test
    void testLocateAndReadSqlFile_CorrectPriorityOrder_TriesInOrder() {
        // Given
        String selectSql = "SELECT * FROM customers";
        
        try (MockedStatic<ReadFileFromResources> mockReadFile = mockStatic(ReadFileFromResources.class)) {
            // Only SELECT file exists (last in priority)
            mockReadFile.when(() -> ReadFileFromResources.readFileFromResources(ProjectConstants.DEFAULT_UPDATE_SQL_FILE))
                       .thenThrow(new UncheckedIOException("Not found", new IOException()));
            mockReadFile.when(() -> ReadFileFromResources.readFileFromResources(ProjectConstants.DEFAULT_INSERT_SQL_FILE))
                       .thenThrow(new UncheckedIOException("Not found", new IOException()));
            mockReadFile.when(() -> ReadFileFromResources.readFileFromResources(ProjectConstants.DEFAULT_DELETE_SQL_FILE))
                       .thenThrow(new UncheckedIOException("Not found", new IOException()));
            mockReadFile.when(() -> ReadFileFromResources.readFileFromResources(ProjectConstants.DEFAULT_SELECT_SQL_FILE))
                       .thenReturn(selectSql);
            
            // When
            String result = resolver.locateAndReadSqlFile(null);
            
            // Then
            assertEquals(selectSql, result);
            
            // Verify the order of attempts: UPDATE -> INSERT -> DELETE -> SELECT
            mockReadFile.verify(() -> ReadFileFromResources.readFileFromResources(ProjectConstants.DEFAULT_UPDATE_SQL_FILE));
            mockReadFile.verify(() -> ReadFileFromResources.readFileFromResources(ProjectConstants.DEFAULT_INSERT_SQL_FILE));
            mockReadFile.verify(() -> ReadFileFromResources.readFileFromResources(ProjectConstants.DEFAULT_DELETE_SQL_FILE));
            mockReadFile.verify(() -> ReadFileFromResources.readFileFromResources(ProjectConstants.DEFAULT_SELECT_SQL_FILE));
        }
    }
    
    @Test
    void testLocateAndReadSqlFile_FirstFileFound_StopsLooking() {
        // Given
        String updateSql = "UPDATE customers SET name = ? WHERE id = ?";
        
        try (MockedStatic<ReadFileFromResources> mockReadFile = mockStatic(ReadFileFromResources.class)) {
            // UPDATE file exists (first in priority)
            mockReadFile.when(() -> ReadFileFromResources.readFileFromResources(ProjectConstants.DEFAULT_UPDATE_SQL_FILE))
                       .thenReturn(updateSql);
            
            // When
            String result = resolver.locateAndReadSqlFile(null);
            
            // Then
            assertEquals(updateSql, result);
            
            // Verify only UPDATE file was attempted
            mockReadFile.verify(() -> ReadFileFromResources.readFileFromResources(ProjectConstants.DEFAULT_UPDATE_SQL_FILE));
            mockReadFile.verifyNoMoreInteractions();
        }
    }
}