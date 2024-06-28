package com.ajua.Dromed.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.CommandLineRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Configuration class for setting up Swagger/OpenAPI documentation for the Drone Management API.
 * It defines custom OpenAPI settings, groups APIs, and provides a command runner to open the Swagger UI automatically.
 */
@Configuration
public class SwaggerConfig {

    private static final Logger logger = LoggerFactory.getLogger(SwaggerConfig.class);

    /**
     * Customizes the OpenAPI documentation with project-specific information such as title, description, version, contact, and license details.
     *
     * @return An OpenAPI object customized with project metadata.
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Drone Management API")
                        .description("API documentation for Drone Dispatch System for Medication Transport")
                        .version("1.0")
                        .contact(new Contact()
                                .name("Support Team")
                                .email("support@ajua.com")
                                .url("https://www.ajua.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://springdoc.org")));
    }

    /**
     * Groups APIs under a common name and path prefix, facilitating organization in the generated Swagger UI.
     *
     * @return A GroupedOpenApi object that groups all paths under the specified group and path matcher.
     */
    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("com.ajua")
                .pathsToMatch("/api/v1/drones/**")
                .build();
    }

    /**
     * Opens the Swagger UI in the default web browser upon application startup.
     * This bean uses a CommandLineRunner to execute the browser opening command after the application context is loaded.
     *
     * @return A CommandLineRunner that opens the Swagger UI.
     */
    @Bean
    public CommandLineRunner openSwaggerUi() {
        return args -> {
            try {
                String url = "http://localhost:8080/swagger-ui.html";
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    Desktop.getDesktop().browse(new URI(url));
                } else {
                    String os = System.getProperty("os.name").toLowerCase();
                    ProcessBuilder builder = new ProcessBuilder();
                    if (os.contains("win")) {
                        builder.command("cmd.exe", "/c", "start", url);
                    } else if (os.contains("mac")) {
                        builder.command("open", url);
                    } else {
                        builder.command("xdg-open", url);
                    }
                    builder.start();
                }
            } catch (IOException | URISyntaxException e) {
                logger.error("Failed to open Swagger UI", e);
            }
        };
    }
}
