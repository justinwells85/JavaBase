package com.justinwells.javabase.config;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Observability configuration for metrics and tracing.
 *
 * <p>Configures Micrometer for metrics collection.
 * Prepared for future OpenTelemetry integration.
 */
@Configuration
public class ObservabilityConfig {

    /**
     * Enables @Timed annotation support for method-level timing.
     *
     * @param registry the meter registry
     * @return the timed aspect
     */
    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }
}
