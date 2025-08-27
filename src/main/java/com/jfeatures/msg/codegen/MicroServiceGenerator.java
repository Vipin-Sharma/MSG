package com.jfeatures.msg.codegen;

import com.jfeatures.msg.codegen.constants.ProjectConstants;
import com.jfeatures.msg.codegen.database.DatabaseConnectionFactory;
import com.jfeatures.msg.codegen.domain.DatabaseConnection;
import com.jfeatures.msg.codegen.domain.GeneratedMicroservice;
import com.jfeatures.msg.codegen.filesystem.MicroserviceDirectoryCleaner;
import com.jfeatures.msg.codegen.filesystem.MicroserviceProjectWriter;
import com.jfeatures.msg.codegen.generator.SelectMicroserviceGenerator;
import com.jfeatures.msg.codegen.generator.UpdateMicroserviceGenerator;
import com.jfeatures.msg.codegen.sql.SqlFileResolver;
import com.jfeatures.msg.codegen.util.SqlStatementDetector;
import com.jfeatures.msg.codegen.util.SqlStatementType;
import com.jfeatures.msg.sql.ReadFileFromResources;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;
import picocli.CommandLine.Option;

import java.net.URISyntaxException;
import java.util.concurrent.Callable;

import static picocli.CommandLine.Command;

@Slf4j
@Command(name = "MSG", mixinStandardHelpOptions = true, version = "MSG 1.0",
        description = "Creates a microservice application.")
public class MicroServiceGenerator implements Callable<Integer> {

    @Option(names = {"-d", "--destination"}, description = "The destination directory of the generated application. Default value is \"" + ProjectConstants.DEFAULT_DESTINATION_DIRECTORY + "\".")
    private String destinationDirectory = ProjectConstants.DEFAULT_DESTINATION_DIRECTORY;

    @Option(names = {"-n", "--name"}, description = "Business purpose name for the generated microservice (e.g., 'Customer', 'Product', 'Order'). Default is '" + ProjectConstants.DEFAULT_BUSINESS_DOMAIN + "'.")
    private String businessPurposeName = ProjectConstants.DEFAULT_BUSINESS_DOMAIN;

    @Option(names = {"-f", "--sql-file"}, description = "SQL file to use for generation. Default tries UPDATE first, then SELECT.")
    private String sqlFileName;

    public static void main(String... args) {
        int exitCode = new CommandLine(new MicroServiceGenerator()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws Exception {
        log.info("Starting microservice generation for business domain: {}", businessPurposeName);
        log.info("Target directory: {}", destinationDirectory);
        
        // Initialize all the focused helper components
        var directoryCleaner = new MicroserviceDirectoryCleaner();
        var sqlFileResolver = new SqlFileResolver();
        var databaseConnectionFactory = new DatabaseConnectionFactory();
        var projectWriter = new MicroserviceProjectWriter();
        
        // Clean destination directory (preserve IDE configurations)
        directoryCleaner.cleanGeneratedCodeDirectories(destinationDirectory);
        
        // Resolve and read SQL file
        String sql = sqlFileResolver.locateAndReadSqlFile(sqlFileName);
        
        // Setup database connections
        DatabaseConnection databaseConnection = databaseConnectionFactory.createDatabaseConnection();
        
        // Detect SQL statement type
        SqlStatementType statementType = SqlStatementDetector.detectStatementType(sql);
        log.info("Detected SQL statement type: {}", statementType);
        
        // Generate microservice based on SQL type
        GeneratedMicroservice microservice = switch (statementType) {
            case SELECT -> new SelectMicroserviceGenerator()
                .generateSelectMicroservice(sql, businessPurposeName, databaseConnection);
            case UPDATE -> new UpdateMicroserviceGenerator()
                .generateUpdateMicroservice(sql, businessPurposeName, databaseConnection);
            case INSERT, DELETE -> throw new UnsupportedOperationException(
                "SQL statement type '" + statementType + "' is not yet supported. Currently supported: SELECT, UPDATE");
            default -> throw new IllegalArgumentException(
                "Unknown or unsupported SQL statement type: '" + statementType + "'. Please provide a valid SELECT or UPDATE statement.");
        };
        
        // Write complete microservice to filesystem
        projectWriter.writeMicroserviceProject(microservice, destinationDirectory);
        
        log.info("Successfully completed microservice generation!");
        return 0;
    }
    
    public static String getSql(String fileName) throws URISyntaxException {
        return ReadFileFromResources.readFileFromResources(fileName);
    }

}
