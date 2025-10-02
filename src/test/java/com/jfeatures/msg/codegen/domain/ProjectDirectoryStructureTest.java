package com.jfeatures.msg.codegen.domain;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link ProjectDirectoryStructure}.
 */
class ProjectDirectoryStructureTest {

    @Test
    void shouldCreateValidProjectDirectoryStructure() {
        // given
        Path srcMainJava = Paths.get("/project/src/main/java");
        Path srcTestJava = Paths.get("/project/src/test/java");
        Path srcMainResources = Paths.get("/project/src/main/resources");
        Path targetDirectory = Paths.get("/project");

        // when
        ProjectDirectoryStructure structure = new ProjectDirectoryStructure(
            srcMainJava, srcTestJava, srcMainResources, targetDirectory
        );

        // then
        assertThat(structure.srcMainJava()).isEqualTo(srcMainJava);
        assertThat(structure.srcTestJava()).isEqualTo(srcTestJava);
        assertThat(structure.srcMainResources()).isEqualTo(srcMainResources);
        assertThat(structure.targetDirectory()).isEqualTo(targetDirectory);
    }

    @Test
    void shouldThrowExceptionWhenSrcMainJavaIsNull() {
        assertThatThrownBy(() -> new ProjectDirectoryStructure(
            null,
            Paths.get("/project/src/test/java"),
            Paths.get("/project/src/main/resources"),
            Paths.get("/project")
        ))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Source main java directory path cannot be null");
    }

    @Test
    void shouldThrowExceptionWhenSrcTestJavaIsNull() {
        assertThatThrownBy(() -> new ProjectDirectoryStructure(
            Paths.get("/project/src/main/java"),
            null,
            Paths.get("/project/src/main/resources"),
            Paths.get("/project")
        ))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Source test java directory path cannot be null");
    }

    @Test
    void shouldThrowExceptionWhenSrcMainResourcesIsNull() {
        assertThatThrownBy(() -> new ProjectDirectoryStructure(
            Paths.get("/project/src/main/java"),
            Paths.get("/project/src/test/java"),
            null,
            Paths.get("/project")
        ))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Source main resources directory path cannot be null");
    }

    @Test
    void shouldThrowExceptionWhenTargetDirectoryIsNull() {
        assertThatThrownBy(() -> new ProjectDirectoryStructure(
            Paths.get("/project/src/main/java"),
            Paths.get("/project/src/test/java"),
            Paths.get("/project/src/main/resources"),
            null
        ))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Target directory path cannot be null");
    }
}
