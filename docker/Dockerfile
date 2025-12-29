# Stage 1: Build
FROM eclipse-temurin:21-jdk as builder

WORKDIR /app

# Copy Maven wrapper and pom.xml first (for layer caching)
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Download dependencies (cached if pom.xml unchanged)
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copy source code
COPY src ./src
COPY checkstyle.xml .
COPY pmd-ruleset.xml .
COPY spotbugs-exclude.xml .

# Build the application (skip tests for faster builds)
RUN ./mvnw clean package -DskipTests -Pskip-quality

# Stage 2: Runtime
FROM eclipse-temurin:21-jre

WORKDIR /app

# Create non-root user for security
RUN groupadd -r javabase && useradd -r -g javabase javabase

# Copy the built artifact from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Set ownership
RUN chown -R javabase:javabase /app

# Switch to non-root user
USER javabase

# Expose the application port
EXPOSE 8080

# JVM options for container environment
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:InitialRAMPercentage=50.0"

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
