# Multi-stage Dockerfile for the Spring Boot application only

# 1) Build stage
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Copy Gradle wrapper and project files
COPY gradlew .
COPY gradle ./gradle
COPY settings.gradle build.gradle ./
COPY src ./src

RUN chmod +x ./gradlew \
  && ./gradlew --no-daemon clean bootJar

# 2) Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Install wget for health checks
RUN apk add --no-cache wget

# Copy the Spring Boot executable jar (exclude the "-plain" jar)
COPY --from=builder /app/build/libs/*-SNAPSHOT.jar /app/app.jar

# Optional JVM flags; do not hard-code Spring properties here
ENV JAVA_OPTS=""

# Expose default port as metadata only; you can override with SERVER_PORT at runtime
EXPOSE 8080

# Conditionally add -Dserver.port only if SERVER_PORT is provided; Spring will pick up
# SPRING_PROFILES_ACTIVE directly from the environment if set
ENTRYPOINT ["sh", "-c", "EXTRA_OPTS=; if [ -n \"$SERVER_PORT\" ]; then EXTRA_OPTS=\"$EXTRA_OPTS -Dserver.port=$SERVER_PORT\"; fi; exec java $JAVA_OPTS $EXTRA_OPTS -jar /app/app.jar"]


