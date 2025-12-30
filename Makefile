# JavaBase Makefile
# Common commands for development and deployment

.PHONY: help build test run clean docker-build docker-up docker-down dev-up dev-down monitoring-up monitoring-down

# Default target
help:
	@echo "JavaBase - Available Commands"
	@echo ""
	@echo "Development:"
	@echo "  make build          - Build the application"
	@echo "  make test           - Run unit tests"
	@echo "  make test-all       - Run all tests (unit + integration)"
	@echo "  make run            - Run the application locally"
	@echo "  make clean          - Clean build artifacts"
	@echo ""
	@echo "Docker - Full Stack:"
	@echo "  make docker-build   - Build Docker image"
	@echo "  make docker-up      - Start full stack (app + deps)"
	@echo "  make docker-down    - Stop full stack"
	@echo "  make docker-logs    - View application logs"
	@echo ""
	@echo "Docker - Development (dependencies only):"
	@echo "  make dev-up         - Start dependencies (Postgres, RabbitMQ)"
	@echo "  make dev-down       - Stop dependencies"
	@echo "  make dev-tools      - Start with pgAdmin"
	@echo ""
	@echo "Docker - Monitoring:"
	@echo "  make monitoring-up  - Start monitoring stack (Prometheus, Grafana)"
	@echo "  make monitoring-down- Stop monitoring stack"
	@echo ""
	@echo "Code Quality:"
	@echo "  make lint           - Run all linters"
	@echo "  make format         - Format code (if formatter configured)"
	@echo "  make coverage       - Generate coverage report"
	@echo ""
	@echo "Utilities:"
	@echo "  make db-shell       - Connect to PostgreSQL"
	@echo "  make rabbit-ui      - Open RabbitMQ management UI"
	@echo "  make grafana        - Open Grafana dashboard"
	@echo "  make swagger        - Open Swagger UI"

# ============================================
# Development
# ============================================

build:
	./mvnw clean package -DskipTests

test:
	./mvnw test

test-all:
	./mvnw verify -DskipITs=false

run:
	./mvnw spring-boot:run

clean:
	./mvnw clean
	rm -rf target/

# ============================================
# Docker - Full Stack
# ============================================

docker-build:
	docker build -t javabase:latest .

docker-up: docker-build
	docker compose up -d

docker-down:
	docker compose down

docker-logs:
	docker compose logs -f app

docker-restart:
	docker compose restart app

# ============================================
# Docker - Development Dependencies
# ============================================

dev-up:
	docker compose -f docker-compose.dev.yml up -d postgres rabbitmq

dev-down:
	docker compose -f docker-compose.dev.yml down

dev-tools:
	docker compose -f docker-compose.dev.yml --profile tools up -d

dev-cache:
	docker compose -f docker-compose.dev.yml --profile cache up -d

dev-logs:
	docker compose -f docker-compose.dev.yml logs -f

# ============================================
# Docker - Monitoring
# ============================================

monitoring-up: dev-up
	docker compose -f docker-compose.dev.yml -f docker-compose.monitoring.yml up -d

monitoring-down:
	docker compose -f docker-compose.dev.yml -f docker-compose.monitoring.yml down

monitoring-alerts:
	docker compose -f docker-compose.dev.yml -f docker-compose.monitoring.yml --profile alerts up -d

# ============================================
# Code Quality
# ============================================

lint:
	./mvnw checkstyle:check spotbugs:check pmd:check

coverage:
	./mvnw verify jacoco:report
	@echo "Coverage report: target/site/jacoco/index.html"

# ============================================
# Utilities
# ============================================

db-shell:
	docker exec -it javabase-postgres psql -U postgres -d javabase

rabbit-ui:
	@echo "Opening RabbitMQ Management UI..."
	@open http://localhost:15672 2>/dev/null || xdg-open http://localhost:15672 2>/dev/null || echo "Visit: http://localhost:15672 (guest/guest)"

grafana:
	@echo "Opening Grafana..."
	@open http://localhost:3000 2>/dev/null || xdg-open http://localhost:3000 2>/dev/null || echo "Visit: http://localhost:3000 (admin/admin)"

prometheus:
	@echo "Opening Prometheus..."
	@open http://localhost:9090 2>/dev/null || xdg-open http://localhost:9090 2>/dev/null || echo "Visit: http://localhost:9090"

swagger:
	@echo "Opening Swagger UI..."
	@open http://localhost:8080/swagger-ui.html 2>/dev/null || xdg-open http://localhost:8080/swagger-ui.html 2>/dev/null || echo "Visit: http://localhost:8080/swagger-ui.html"

pgadmin:
	@echo "Opening pgAdmin..."
	@open http://localhost:5050 2>/dev/null || xdg-open http://localhost:5050 2>/dev/null || echo "Visit: http://localhost:5050 (admin@local.dev/admin)"

# ============================================
# Release
# ============================================

release-patch:
	@echo "Creating patch release..."
	./mvnw build-helper:parse-version versions:set -DnewVersion=\$${parsedVersion.majorVersion}.\$${parsedVersion.minorVersion}.\$${parsedVersion.nextIncrementalVersion}
	./mvnw versions:commit

release-minor:
	@echo "Creating minor release..."
	./mvnw build-helper:parse-version versions:set -DnewVersion=\$${parsedVersion.majorVersion}.\$${parsedVersion.nextMinorVersion}.0
	./mvnw versions:commit

release-major:
	@echo "Creating major release..."
	./mvnw build-helper:parse-version versions:set -DnewVersion=\$${parsedVersion.nextMajorVersion}.0.0
	./mvnw versions:commit
