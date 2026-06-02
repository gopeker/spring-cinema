# Multi-stage Dockerfile for Spring Boot application

# Java build stage
FROM eclipse-temurin:25-jdk-jammy AS build

# Install Node.js
RUN apt-get update && apt-get install -y curl && \
    curl -fsSL https://deb.nodesource.com/setup_20.x | bash - && \
    apt-get install -y nodejs && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy Maven wrapper and pom.xml
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
RUN chmod +x mvnw

# Download Maven dependencies
RUN ./mvnw dependency:go-offline

# Copy source code
COPY src ./src

# Build Angular frontend
COPY frontend/package.json frontend/package-lock.json* ./
RUN npm install --no-audit --no-fund

COPY frontend ./frontend

# Build Angular and copy to Spring static resources
RUN cd frontend && npm run build -- --configuration production && mkdir -p ../src/main/resources/static && cp -r ./dist/spring-cinema-browser/* ../src/main/resources/static/ 2>/dev/null || cp -r ./dist/* ../src/main/resources/static/

# Build the application
RUN ./mvnw clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:25-jre-alpine

RUN apk add --no-cache curl

RUN adduser -D -u 1001 springboot

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

RUN chown -R springboot:springboot /app

USER springboot

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
