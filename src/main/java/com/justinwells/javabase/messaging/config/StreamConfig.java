package com.justinwells.javabase.messaging.config;

import com.justinwells.javabase.messaging.consumer.TaskEventConsumer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import java.util.function.Consumer;

/**
 * Spring Cloud Stream configuration for messaging.
 *
 * <p>Defines the function beans that handle incoming and outgoing messages.
 */
@Configuration
public class StreamConfig {

    private final TaskEventConsumer taskEventConsumer;

    public StreamConfig(TaskEventConsumer taskEventConsumer) {
        this.taskEventConsumer = taskEventConsumer;
    }

    /**
     * Consumer function bean for processing incoming task events.
     *
     * @return the consumer function
     */
    @Bean
    public Consumer<Message<String>> taskEventsIn() {
        return taskEventConsumer::handleEvent;
    }
}
