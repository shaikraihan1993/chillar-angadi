# Multi-stage build for Spring Boot Gradle application
FROM gradle:8.5-jdk17-jammy AS build

# Set working directory
WORKDIR /app

# Copy Gradle configuration files
COPY build.gradle settings.gradle gradle.properties ./
COPY gradle ./gradle

# Copy source code
COPY src ./src

# Build the application
RUN gradle clean build -x test --no-daemon

# Runtime stage
FROM eclipse-temurin:17-jre-jammy

# Set working directory
WORKDIR /app

# Create a non-root user
RUN addgroup --system spring && adduser --system --ingroup spring spring

# Create upload directory
RUN mkdir -p /app/uploads && chown -R spring:spring /app

# Copy the built artifact from the build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Change ownership of the app
RUN chown -R spring:spring /app

# Switch to non-root user
USER spring

# Expose the application port
EXPOSE 8089

# Set JVM options for container environment
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8089/actuator/health || exit 1

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

