package com.jfeatures.msg.codegen.domain;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class ProjectDirectoryStructureTest {

    @Test
    void constructorValidatesNonNullPaths() {
        Path sample = Path.of("/tmp");

        assertThrows(IllegalArgumentException.class, () -> new ProjectDirectoryStructure(null, sample, sample, sample));
        assertThrows(IllegalArgumentException.class, () -> new ProjectDirectoryStructure(sample, null, sample, sample));
        assertThrows(IllegalArgumentException.class, () -> new ProjectDirectoryStructure(sample, sample, null, sample));
        assertThrows(IllegalArgumentException.class, () -> new ProjectDirectoryStructure(sample, sample, sample, null));
    }

    @Test
    void constructorAcceptsValidPaths() {
        Path sample = Path.of("/tmp");
        assertDoesNotThrow(() -> new ProjectDirectoryStructure(sample, sample, sample, sample));
    }
}
