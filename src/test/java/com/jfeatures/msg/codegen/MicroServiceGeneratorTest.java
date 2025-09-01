package com.jfeatures.msg.codegen;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class MicroServiceGeneratorTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldDisplayHelpWhenRequested() {
        // Given
        MicroServiceGenerator generator = new MicroServiceGenerator();
        
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        CommandLine cmd = new CommandLine(generator);
        cmd.setOut(new PrintWriter(outContent));
        cmd.setErr(new PrintWriter(errContent));

        // When
        int exitCode = cmd.execute("--help");

        // Then
        assertThat(exitCode).isEqualTo(0);
        String output = outContent.toString() + errContent.toString();
        assertThat(output).contains("Creates a microservice application");
        assertThat(output).contains("-d, --destination");
        assertThat(output).contains("-n, --name");
        assertThat(output).contains("-f, --sql-file");
    }

    @Test
    void shouldDisplayVersionWhenRequested() {
        // Given
        MicroServiceGenerator generator = new MicroServiceGenerator();
        
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        CommandLine cmd = new CommandLine(generator);
        cmd.setOut(new PrintWriter(outContent));
        cmd.setErr(new PrintWriter(errContent));

        // When
        int exitCode = cmd.execute("--version");

        // Then
        assertThat(exitCode).isEqualTo(0);
        String output = outContent.toString() + errContent.toString();
        assertThat(output).contains("MSG 1.0");
    }

    @Test
    void shouldAcceptDestinationDirectoryOption() {
        // Given
        MicroServiceGenerator generator = new MicroServiceGenerator();
        CommandLine cmd = new CommandLine(generator);

        // When
        cmd.parseArgs("-d", tempDir.toString());

        // Then
        // This test mainly verifies that the option parsing works without errors
        assertThat(tempDir).exists();
    }

    @Test
    void shouldAcceptBusinessNameOption() {
        // Given
        MicroServiceGenerator generator = new MicroServiceGenerator();
        CommandLine cmd = new CommandLine(generator);

        // When
        cmd.parseArgs("-n", "TestService");

        // Then
        // This test mainly verifies that the option parsing works without errors
        assertThat(cmd.getParseResult().hasMatchedOption('n')).isTrue();
    }

    @Test
    void shouldAcceptSqlFileOption() {
        // Given
        MicroServiceGenerator generator = new MicroServiceGenerator();
        CommandLine cmd = new CommandLine(generator);

        // When
        cmd.parseArgs("-f", "test.sql");

        // Then
        // This test mainly verifies that the option parsing works without errors
        assertThat(cmd.getParseResult().hasMatchedOption('f')).isTrue();
    }

    @Test
    void shouldAcceptLongFormOptions() {
        // Given
        MicroServiceGenerator generator = new MicroServiceGenerator();
        CommandLine cmd = new CommandLine(generator);

        // When
        cmd.parseArgs("--destination", tempDir.toString(), "--name", "TestService", "--sql-file", "test.sql");

        // Then
        assertThat(cmd.getParseResult().hasMatchedOption("destination")).isTrue();
        assertThat(cmd.getParseResult().hasMatchedOption("name")).isTrue();
        assertThat(cmd.getParseResult().hasMatchedOption("sql-file")).isTrue();
    }

    @Test
    void shouldHaveDefaultValues() {
        // Given
        MicroServiceGenerator generator = new MicroServiceGenerator();

        // When
        CommandLine cmd = new CommandLine(generator);
        cmd.parseArgs(); // Parse with no arguments

        // Then
        // Default values should be set according to ProjectConstants
        // We can't directly access private fields, but the help output should show defaults
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        CommandLine helpCmd = new CommandLine(new MicroServiceGenerator());
        helpCmd.setOut(new PrintWriter(outContent));
        helpCmd.setErr(new PrintWriter(errContent));

        helpCmd.execute("--help");
        String output = outContent.toString() + errContent.toString();
        assertThat(output).contains("Default value is");
    }

    @Test
    void shouldParseCombinedOptions() {
        // Given
        MicroServiceGenerator generator = new MicroServiceGenerator();
        CommandLine cmd = new CommandLine(generator);

        // When
        cmd.parseArgs("-d", tempDir.toString(), "-n", "Customer", "-f", "customer.sql");

        // Then
        assertThat(cmd.getParseResult().hasMatchedOption('d')).isTrue();
        assertThat(cmd.getParseResult().hasMatchedOption('n')).isTrue();
        assertThat(cmd.getParseResult().hasMatchedOption('f')).isTrue();
    }

    @Test
    void shouldHaveCorrectCommandName() {
        // Given
        MicroServiceGenerator generator = new MicroServiceGenerator();
        CommandLine cmd = new CommandLine(generator);

        // When & Then
        assertThat(cmd.getCommandName()).isEqualTo("MSG");
    }

    @Test
    void shouldValidateMainMethodExists() throws Exception {
        // Given
        Class<?> clazz = MicroServiceGenerator.class;

        // When
        var mainMethod = clazz.getMethod("main", String[].class);

        // Then
        assertThat(mainMethod).isNotNull();
        assertThat(mainMethod.getReturnType()).isEqualTo(void.class);
    }

    @Test
    void shouldValidateGetSqlMethodExists() throws Exception {
        // Given
        Class<?> clazz = MicroServiceGenerator.class;

        // When
        var getSqlMethod = clazz.getMethod("getSql", String.class);

        // Then
        assertThat(getSqlMethod).isNotNull();
        assertThat(getSqlMethod.getReturnType()).isEqualTo(String.class);
    }
}