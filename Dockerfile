# Multi-stage build for optimized image size

# Stage 1: Build
FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app

# Copy pom.xml and download dependencies (cached layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Create non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring

# Create directories for logs and data BEFORE switching to non-root user
RUN mkdir -p /app/logs /app/data && chown -R spring:spring /app

# Copy jar from build stage
COPY --from=build /app/target/*.jar app.jar

# Change ownership of the jar file
RUN chown spring:spring app.jar

# Switch to non-root user
USER spring:spring

# Expose application port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/api/deals/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]