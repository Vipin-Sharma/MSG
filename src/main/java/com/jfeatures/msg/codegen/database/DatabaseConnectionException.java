package com.jfeatures.msg.codegen.database;

/**
 * Exception thrown when database connection creation fails.
 */
public class DatabaseConnectionException extends RuntimeException {

    public DatabaseConnectionException(String message) {
        super(message);
    }

    public DatabaseConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
