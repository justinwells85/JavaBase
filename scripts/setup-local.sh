#!/bin/bash

# JavaBase Local Development Setup Script
# Sets up the local development environment with Docker containers

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

cd "$PROJECT_DIR"

echo "=========================================="
echo "JavaBase Local Development Setup"
echo "=========================================="
echo ""

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

# Check for Docker
if ! command -v docker &> /dev/null; then
    echo "Docker is required but not installed. Please install Docker first."
    exit 1
fi

# Check for Docker Compose
if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
    echo "Docker Compose is required but not installed. Please install Docker Compose first."
    exit 1
fi

echo "Step 1/4: Starting Docker containers..."
if docker compose version &> /dev/null; then
    docker compose -f docker/docker-compose.yml up -d postgres rabbitmq
else
    docker-compose -f docker/docker-compose.yml up -d postgres rabbitmq
fi
echo -e "${GREEN}✓ Containers started${NC}"
echo ""

echo "Step 2/4: Waiting for PostgreSQL to be ready..."
sleep 5
until docker exec javabase-postgres pg_isready -U postgres > /dev/null 2>&1; do
    echo "Waiting for PostgreSQL..."
    sleep 2
done
echo -e "${GREEN}✓ PostgreSQL is ready${NC}"
echo ""

echo "Step 3/4: Waiting for RabbitMQ to be ready..."
until docker exec javabase-rabbitmq rabbitmq-diagnostics check_running > /dev/null 2>&1; do
    echo "Waiting for RabbitMQ..."
    sleep 2
done
echo -e "${GREEN}✓ RabbitMQ is ready${NC}"
echo ""

echo "Step 4/4: Building the application..."
./mvnw clean compile -DskipTests -q
echo -e "${GREEN}✓ Application built${NC}"
echo ""

echo "=========================================="
echo -e "${GREEN}Setup complete!${NC}"
echo ""
echo "Services running:"
echo "  - PostgreSQL: localhost:5432"
echo "  - RabbitMQ:   localhost:5672 (AMQP)"
echo "  - RabbitMQ:   localhost:15672 (Management UI)"
echo ""
echo "To start the application:"
echo "  ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev"
echo ""
echo "To access:"
echo "  - API:        http://localhost:8080/api/v1/tasks"
echo "  - Swagger UI: http://localhost:8080/swagger-ui.html"
echo "  - RabbitMQ:   http://localhost:15672 (guest/guest)"
echo "=========================================="
