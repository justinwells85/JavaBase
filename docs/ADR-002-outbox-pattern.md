# ADR-002: Outbox Pattern

## Status

Accepted

## Context

When a service updates its database and publishes an event, these are two separate operations. If one succeeds and the other fails, we have inconsistency:

- Database updated but event not published → Consumers never learn about the change
- Event published but database update fails → Consumers see changes that don't exist

We need guaranteed consistency between database state and published events.

## Decision

Implement the Transactional Outbox Pattern:

1. **Single Transaction** - When business logic updates the database, it also writes the event to an `outbox_events` table in the same transaction.

2. **Background Processor** - A scheduled job (`OutboxProcessor`) polls the outbox table for unpublished events.

3. **Publish and Mark** - Events are published to RabbitMQ, then marked as published in the database.

4. **At-Least-Once Delivery** - If publication fails, the event remains unpublished and will be retried.

### Flow

```
1. TaskService.createTask()
   ├── taskRepository.save(task)           ─┐
   └── outboxEventRepository.save(event)    ├── Same Transaction
                                           ─┘
2. OutboxProcessor (scheduled)
   ├── Find PENDING events
   ├── Publish to RabbitMQ
   └── Mark as PUBLISHED
```

## Consequences

### Positive

- **Guaranteed Delivery** - Events are never lost even if RabbitMQ is down
- **Atomicity** - Database state and events are always consistent
- **Replayability** - Events can be replayed from the outbox if needed
- **Ordering** - Events are processed in order (by creation time)

### Negative

- **Latency** - Small delay between database update and event publication
- **Table Growth** - Outbox table needs periodic cleanup
- **Complexity** - Additional component (OutboxProcessor) to maintain

## Alternatives Considered

1. **Dual Write** - Write to database and message broker separately. Rejected due to consistency issues.

2. **Change Data Capture (CDC)** - Use database log to capture changes. Rejected due to complexity and infrastructure requirements.

3. **Saga Pattern** - Compensating transactions. Overkill for this use case; events don't require compensation.
