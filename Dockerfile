# ============================================================
# STAGE 1: Build with Maven & JDK 21
# ============================================================
FROM maven:3.9-eclipse-temurin-21-alpine AS builder

WORKDIR /build

# Copy pom.xml first for dependency caching
COPY pom.xml ./

# Download dependencies (cached layer unless pom.xml changes)
RUN mvn dependency:go-offline -B -q

# Copy source and build
COPY src src
RUN mvn package -DskipTests -B -q

# ============================================================
# STAGE 2: Runtime with minimal JDK 21 JRE
# ============================================================
FROM eclipse-temurin:21-jre-alpine AS runtime

# Create non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Copy the fat JAR from builder stage
COPY --from=builder /build/target/*.jar app.jar

# Use non-root user
USER appuser

# Expose application port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Entry point with Docker profile
ENTRYPOINT ["java", \
    "-Dspring.profiles.active=docker", \
    "-jar", "app.jar"]
