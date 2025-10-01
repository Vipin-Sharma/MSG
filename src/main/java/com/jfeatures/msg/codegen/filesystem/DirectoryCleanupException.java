package com.jfeatures.msg.codegen.filesystem;

import java.io.IOException;

/**
 * Exception thrown when directory cleanup operations fail.
 */
public class DirectoryCleanupException extends IOException {

    public DirectoryCleanupException(String message) {
        super(message);
    }

    public DirectoryCleanupException(String message, Throwable cause) {
        super(message, cause);
    }
}
