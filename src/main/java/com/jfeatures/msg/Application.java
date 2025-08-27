package com.jfeatures.msg;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot application class.
 * Follows Vipin's Principle: One public method per class.
 * Single responsibility: Application bootstrapping only.
 */
@SpringBootApplication
public class Application {

    /**
     * Main entry point for the Spring Boot application.
     * Single responsibility: Start the application.
     */
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
