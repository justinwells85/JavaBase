# ADR-003: Database Choice

## Status

Accepted

## Context

We need a database that supports:
- ACID transactions (critical for outbox pattern)
- JSON storage (for event payloads)
- Strong ecosystem and tooling
- Easy local development and AWS migration
- Good performance for typical CRUD operations

## Decision

Use PostgreSQL as the primary database:

1. **PostgreSQL 16** - Latest stable version with proven reliability
2. **Liquibase** - For database migrations (version-controlled schema changes)
3. **Spring Data JPA** - For ORM and repository pattern
4. **HikariCP** - Connection pooling (Spring Boot default)

### Schema Design Principles

- **UUID Primary Keys** - For distributed systems compatibility
- **Soft Deletes** - `deleted_at` timestamp instead of physical deletes
- **Audit Fields** - `created_at`, `updated_at` on all entities
- **Optimistic Locking** - Version column for concurrent updates
- **Indexes** - On frequently queried columns

## Consequences

### Positive

- **Reliability** - PostgreSQL is battle-tested and stable
- **Features** - JSONB, full-text search, CTEs, window functions
- **AWS Migration** - Direct path to RDS PostgreSQL
- **Tooling** - Excellent IDE support, monitoring tools, backup solutions
- **Open Source** - No licensing costs

### Negative

- **Horizontal Scaling** - Requires read replicas or sharding for extreme scale
- **Learning Curve** - More complex than simpler databases
- **Memory Usage** - Can be memory-intensive for large datasets

## Alternatives Considered

1. **MySQL** - Viable alternative, but PostgreSQL has better JSON support and feature set.

2. **MongoDB** - Rejected due to lack of ACID transactions (critical for outbox pattern) and eventual consistency model.

3. **H2** - Used for development/testing only, not suitable for production.

4. **Amazon Aurora** - Future consideration for AWS migration; compatible with PostgreSQL.
