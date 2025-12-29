package com.justinwells.javabase.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Database and JPA configuration.
 *
 * <p>Enables JPA repositories, auditing, and transaction management.
 */
@Configuration
@EnableJpaRepositories(basePackages = {
    "com.justinwells.javabase.domain.repository",
    "com.justinwells.javabase.infrastructure.idempotency"
})
@EnableJpaAuditing
@EnableTransactionManagement
public class DatabaseConfig {
    // Configuration is handled via application.yml
    // This class enables JPA features and can be extended for custom config
}
