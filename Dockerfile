# Build argument for service name
ARG SERVICE_NAME

# Build stage  
FROM gradle:8.14-jdk17 AS builder

WORKDIR /app

# Copy gradle files first for better caching
COPY build.gradle settings.gradle gradle.properties ./
COPY gradle/ gradle/

# Copy libs for dependencies
COPY libs/ libs/

# Copy specific service source
ARG SERVICE_NAME
COPY ${SERVICE_NAME}/ ${SERVICE_NAME}/

# Build the specific service
RUN gradle :${SERVICE_NAME}:build -x test --no-daemon

# Runtime stage
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy the built jar
ARG SERVICE_NAME
COPY --from=builder /app/${SERVICE_NAME}/build/libs/${SERVICE_NAME}-*.jar app.jar

# Create non-root user
RUN addgroup -g 1000 -S hermes && adduser -u 1000 -S hermes -G hermes
RUN chown -R hermes:hermes /app
USER hermes

ARG SERVER_PORT=8080
ENV SERVER_PORT=$SERVER_PORT
EXPOSE $SERVER_PORT

# Run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar", "--spring.profiles.active=prod"]