# ADR-001: Event-Driven Architecture

## Status

Accepted

## Context

We need an architecture that supports:
- Loose coupling between microservices
- Asynchronous communication
- Scalability and resilience
- Future event sourcing patterns
- Easy integration with external systems

## Decision

Use an event-driven architecture with the following characteristics:

1. **All state changes publish domain events** - When a Task is created, updated, or deleted, a corresponding event is published.

2. **Asynchronous messaging via RabbitMQ** - Services communicate primarily through events rather than synchronous HTTP calls.

3. **Spring Cloud Stream as abstraction layer** - Provides binder abstraction allowing easy switch between messaging systems (RabbitMQ, Kafka, SQS).

4. **Domain events as first-class citizens** - Events are versioned, typed, and include correlation IDs for tracing.

## Consequences

### Positive

- **Loose Coupling** - Services don't need to know about each other
- **Scalability** - Easy to add new consumers without affecting producers
- **Resilience** - Events can be replayed, services can recover from failures
- **Audit Trail** - Complete history of all state changes
- **Flexibility** - Easy to add new features by subscribing to existing events

### Negative

- **Complexity** - More moving parts compared to simple REST
- **Eventual Consistency** - Data may not be immediately consistent across services
- **Debugging** - Harder to trace request flows across services
- **Infrastructure** - Requires message broker setup and maintenance

## Alternatives Considered

1. **Synchronous REST only** - Rejected due to tight coupling and resilience concerns
2. **Apache Kafka** - Rejected for initial implementation due to higher operational overhead; can be added later via Spring Cloud Stream binder
3. **Direct database sharing** - Rejected due to coupling and scalability limitations
