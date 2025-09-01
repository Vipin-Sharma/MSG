package com.jfeatures.msg.codegen.domain;

import java.nio.file.Path;

/**
 * Represents the standard Maven project directory structure for generated microservices.
 * This value object encapsulates all the key directory paths needed to write
 * a complete Spring Boot project to the filesystem.
 */
public record ProjectDirectoryStructure(
    Path srcMainJava,
    Path srcTestJava,
    Path srcMainResources,
    Path targetDirectory
) {
    
    public ProjectDirectoryStructure {
        if (srcMainJava == null) {
            throw new IllegalArgumentException("Source main java directory path cannot be null");
        }
        if (srcTestJava == null) {
            throw new IllegalArgumentException("Source test java directory path cannot be null");
        }
        if (srcMainResources == null) {
            throw new IllegalArgumentException("Source main resources directory path cannot be null");
        }
        if (targetDirectory == null) {
            throw new IllegalArgumentException("Target directory path cannot be null");
        }
    }
}