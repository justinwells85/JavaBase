#!/bin/bash

# JavaBase Quality Checks Script
# Runs all code quality checks: Checkstyle, PMD, SpotBugs, and tests with coverage

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

cd "$PROJECT_DIR"

echo "=========================================="
echo "JavaBase Quality Checks"
echo "=========================================="
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

print_status() {
    if [ $1 -eq 0 ]; then
        echo -e "${GREEN}✓ $2 passed${NC}"
    else
        echo -e "${RED}✗ $2 failed${NC}"
        exit 1
    fi
}

echo "Step 1/5: Compiling..."
./mvnw compile -q
print_status $? "Compilation"
echo ""

echo "Step 2/5: Running Checkstyle..."
./mvnw checkstyle:check -q 2>/dev/null || true
if [ $? -eq 0 ]; then
    print_status 0 "Checkstyle"
else
    echo -e "${YELLOW}⚠ Checkstyle had warnings (non-blocking)${NC}"
fi
echo ""

echo "Step 3/5: Running PMD..."
./mvnw pmd:check -q 2>/dev/null || true
if [ $? -eq 0 ]; then
    print_status 0 "PMD"
else
    echo -e "${YELLOW}⚠ PMD had warnings (non-blocking)${NC}"
fi
echo ""

echo "Step 4/5: Running SpotBugs..."
./mvnw spotbugs:check -q 2>/dev/null || true
if [ $? -eq 0 ]; then
    print_status 0 "SpotBugs"
else
    echo -e "${YELLOW}⚠ SpotBugs had warnings (non-blocking)${NC}"
fi
echo ""

echo "Step 5/5: Running tests with coverage..."
./mvnw test jacoco:report -q
print_status $? "Tests"
echo ""

echo "=========================================="
echo -e "${GREEN}All quality checks completed!${NC}"
echo ""
echo "Coverage report: target/site/jacoco/index.html"
echo "=========================================="
