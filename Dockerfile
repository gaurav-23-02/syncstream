# =========================================================
# Multi-Stage Dockerfile for SyncStream Full-Stack App
# Stage 1: Build React + Vite + Tailwind Frontend
# Stage 2: Build Spring Boot 3.3 Backend with Java 21
# Stage 3: Lightweight Java 21 JRE Runtime Image
# =========================================================

# --- STAGE 1: Build Frontend ---
FROM node:20-alpine AS frontend-builder
WORKDIR /app/client

COPY client/package*.json ./
RUN npm ci

COPY client/ ./
RUN npm run build

# --- STAGE 2: Build Backend ---
FROM maven:3.9.6-eclipse-temurin-21-alpine AS backend-builder
WORKDIR /app

# Copy Maven POM and download dependencies for cache efficiency
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy Spring Boot source code
COPY src ./src

# Embed compiled React static assets into Spring Boot's static resources folder
COPY --from=frontend-builder /app/client/dist ./src/main/resources/static

# Package fat JAR (skip test execution during Docker build)
RUN mvn clean package -DskipTests

# --- STAGE 3: Final Production Runtime ---
FROM eclipse-temurin:21-jre-alpine AS runner
WORKDIR /app

# Create non-root system user for secure execution
RUN addgroup -S syncstream && adduser -S syncstream -G syncstream

# Copy compiled executable JAR from builder stage
COPY --from=backend-builder /app/target/syncstream-backend-1.0.0.jar app.jar

# Set user permissions
RUN chown -R syncstream:syncstream /app
USER syncstream

# Configuration & Environment Defaults
ENV PORT=8080
ENV SPRING_PROFILES_ACTIVE=prod
ENV DEMO_MODE=false

EXPOSE 8080

# Healthcheck
HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/api/auth/status || exit 1

ENTRYPOINT ["java", "-XX:+UseZGC", "-XX:+ZGenerational", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
