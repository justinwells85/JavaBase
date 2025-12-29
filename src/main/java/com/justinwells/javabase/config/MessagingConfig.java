package com.justinwells.javabase.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Messaging and serialization configuration.
 *
 * <p>Configures Jackson ObjectMapper for JSON serialization of events.
 */
@Configuration
public class MessagingConfig {

    /**
     * Creates a configured ObjectMapper for JSON serialization.
     *
     * <p>Includes support for Java 8 date/time types.
     *
     * @return the configured ObjectMapper
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }
}
