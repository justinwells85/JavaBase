package com.justinwells.javabase.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger documentation configuration.
 */
@Configuration
public class OpenApiConfig {

    @Value("${api.version.current:v1}")
    private String apiVersion;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("JavaBase API")
                .version(apiVersion)
                .description("""
                    JavaBase is a production-ready Spring Boot microservice template
                    for event-driven architecture.

                    ## Features
                    - Event-driven architecture with RabbitMQ
                    - Transactional outbox pattern
                    - Idempotency handling
                    - Structured JSON logging
                    - Circuit breakers and resilience patterns

                    ## Authentication
                    Currently, authentication is disabled. Future versions will support
                    JWT-based authentication.

                    ## Rate Limiting
                    API endpoints are rate limited to 100 requests per second per client.
                    """)
                .contact(new Contact()
                    .name("JavaBase Support")
                    .url("https://github.com/justinwells85/JavaBase"))
                .license(new License()
                    .name("MIT License")
                    .url("https://opensource.org/licenses/MIT")))
            .servers(List.of(
                new Server()
                    .url("http://localhost:8080")
                    .description("Local Development Server")))
            .components(new Components()
                .addSecuritySchemes("bearer-jwt", new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .description("JWT authentication (not yet implemented)")));
    }
}
