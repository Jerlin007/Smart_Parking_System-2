# ============================================================
# STAGE 1: Build Application
# ============================================================
FROM maven:3.9-eclipse-temurin-21-alpine AS builder

WORKDIR /build

# Copy pom.xml first for dependency caching
COPY pom.xml .

# Download dependencies
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build application
RUN mvn clean package -DskipTests -B

# ============================================================
# STAGE 2: Runtime
# ============================================================
FROM eclipse-temurin:21-jre-alpine

# Install wget for healthcheck
RUN apk add --no-cache wget

# Create non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Copy jar from builder
COPY --from=builder /build/target/*.jar app.jar

# Change ownership
RUN chown -R appuser:appgroup /app

# Use non-root user
USER appuser

# Expose Spring Boot port
EXPOSE 8080

# Health Check
HEALTHCHECK --interval=30s \
            --timeout=5s \
            --start-period=60s \
            --retries=3 \
            CMD wget --no-verbose --tries=1 --spider \
            http://127.0.0.1:8080/actuator/health || exit 1

# Start Application
ENTRYPOINT ["java","-Dspring.profiles.active=docker","-jar","app.jar"]