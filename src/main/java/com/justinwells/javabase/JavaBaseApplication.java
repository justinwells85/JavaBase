package com.justinwells.javabase;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * JavaBase - Production-ready Spring Boot microservice template.
 *
 * <p>This application serves as a foundation for event-driven microservices
 * with built-in support for:
 * <ul>
 *   <li>Event-driven architecture with RabbitMQ</li>
 *   <li>Outbox pattern for transactional event publishing</li>
 *   <li>Idempotency handling for REST endpoints and event consumers</li>
 *   <li>Structured JSON logging with correlation ID tracking</li>
 *   <li>Circuit breakers and resilience patterns</li>
 * </ul>
 */
@SpringBootApplication
@EnableCaching
@EnableScheduling
public class JavaBaseApplication {

    public static void main(String[] args) {
        SpringApplication.run(JavaBaseApplication.class, args);
    }
}
