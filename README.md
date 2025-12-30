# JavaBase - Spring Boot Microservice Template

[![CI](https://github.com/justinwells85/JavaBase/actions/workflows/ci.yml/badge.svg)](https://github.com/justinwells85/JavaBase/actions/workflows/ci.yml)

A production-ready Spring Boot 3.x microservice template designed for event-driven architecture with emphasis on code quality, observability, and easy AWS migration.

## Features

- **Event-Driven Architecture** - Spring Cloud Stream with RabbitMQ
- **Transactional Outbox Pattern** - Guaranteed event delivery
- **Idempotency Handling** - For REST endpoints and event consumers
- **Structured JSON Logging** - With correlation ID tracking
- **Circuit Breakers** - Resilience4j for fault tolerance
- **API Versioning** - URL-based versioning with deprecation support
- **Code Quality Gates** - Checkstyle, PMD, SpotBugs, JaCoCo (80% coverage)
- **Full Test Coverage** - Unit, integration, and controller tests
- **Docker Ready** - Multi-stage Dockerfile and Docker Compose
- **CI/CD Pipeline** - GitHub Actions workflow included

## Technology Stack

| Category | Technology |
|----------|------------|
| Language | Java 21 |
| Framework | Spring Boot 3.4.x |
| Build Tool | Maven |
| Database | PostgreSQL + Liquibase |
| Messaging | RabbitMQ via Spring Cloud Stream |
| Caching | Caffeine |
| Documentation | Springdoc OpenAPI 3 |

## Quick Start

### Prerequisites

- Java 21+
- Docker & Docker Compose
- Maven 3.9+ (or use included wrapper)

### Option 1: Docker Compose (Recommended)

```bash
# Clone the repository
git clone https://github.com/justinwells85/JavaBase.git
cd JavaBase

# Start everything (app + PostgreSQL + RabbitMQ)
docker-compose up --build
```

### Option 2: Local Development

```bash
# Start infrastructure only
docker-compose up postgres rabbitmq -d

# Run the application
./mvnw spring-boot:run
```

### Access Points

| Service | URL |
|---------|-----|
| API | http://localhost:8080/api/v1/tasks |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| API Docs | http://localhost:8080/api-docs |
| RabbitMQ Management | http://localhost:15672 (guest/guest) |
| Health Check | http://localhost:8080/actuator/health |

## API Usage

### Create a Task

```bash
curl -X POST http://localhost:8080/api/v1/tasks \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: $(uuidgen)" \
  -d '{
    "title": "My First Task",
    "description": "Task description",
    "assignee": "john"
  }'
```

### Get a Task

```bash
curl http://localhost:8080/api/v1/tasks/{task-id}
```

### List Tasks

```bash
curl "http://localhost:8080/api/v1/tasks?status=PENDING&page=0&size=20"
```

## Project Structure

```
src/main/java/com/justinwells/javabase/
├── JavaBaseApplication.java       # Application entry point
├── config/                        # Configuration classes
├── controller/                    # REST controllers
│   ├── VersionedController.java   # Base controller with versioning
│   └── v1/                        # Version 1 controllers
├── domain/
│   ├── model/                     # JPA entities
│   ├── repository/                # Spring Data repositories
│   └── event/                     # Domain events
├── dto/v1/                        # Request/Response DTOs
├── exception/                     # Exception handling
├── infrastructure/
│   ├── correlation/               # Correlation ID filter
│   ├── idempotency/               # Idempotency handling
│   └── outbox/                    # Outbox pattern implementation
├── messaging/
│   ├── producer/                  # Event producers
│   └── consumer/                  # Event consumers
└── service/                       # Business logic
```

## Configuration

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `DB_HOST` | localhost | PostgreSQL host |
| `DB_PORT` | 5432 | PostgreSQL port |
| `DB_NAME` | javabase | Database name |
| `DB_USERNAME` | postgres | Database user |
| `DB_PASSWORD` | postgres | Database password |
| `RABBITMQ_HOST` | localhost | RabbitMQ host |
| `RABBITMQ_PORT` | 5672 | RabbitMQ AMQP port |

### Profiles

- `default` - Local development
- `docker` - Docker container (JSON logging)
- `test` - Testing (H2, Testcontainers for integration)
- `prod` - Production (JSON logging, security enabled)

## Development

### Running Tests

```bash
# Unit tests only
./mvnw test

# Integration tests (requires Docker)
./mvnw verify -DskipITs=false

# With coverage report
./mvnw verify jacoco:report
open target/site/jacoco/index.html
```

### Code Quality

```bash
# Run all quality checks
./mvnw verify -DskipTests

# Individual checks
./mvnw checkstyle:check
./mvnw pmd:check
./mvnw spotbugs:check
```

### Docker

```bash
# Build and run full stack
docker-compose up --build

# Build image only
docker build -t javabase:latest .

# Stop and cleanup
docker-compose down -v
```

## Building a New Service from This Template

1. **Clone/Fork** this repository
2. **Rename** the project in `pom.xml`:
   - Update `artifactId`, `name`, `description`
   - Update `groupId` if needed
3. **Rename** the base package from `com.justinwells.javabase` to your package
4. **Update** `application.yml` with your service name
5. **Replace** the Task domain with your domain entities
6. **Create** your own domain events
7. **Update** the README and documentation

## AWS Migration Path

This template is designed for easy AWS migration:

| Local Component | AWS Service |
|----------------|-------------|
| PostgreSQL | RDS PostgreSQL |
| RabbitMQ | Amazon MQ or SQS/SNS |
| Docker Container | ECS Fargate or EKS |
| Logs | CloudWatch Logs |
| Metrics | CloudWatch Metrics |

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Author

Justin Wells - [GitHub](https://github.com/justinwells85)
