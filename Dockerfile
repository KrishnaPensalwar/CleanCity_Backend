# ---- Build stage ----
FROM eclipse-temurin:17-jdk-jammy AS builder

WORKDIR /workspace

# Copy Gradle wrapper and build metadata first for better layer caching
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle gradle.properties ./

RUN chmod +x gradlew

# Download dependencies (cached unless build files change)
RUN ./gradlew dependencies --no-daemon || true

# Copy source and build the Spring Boot JAR
COPY src src
RUN ./gradlew bootJar --no-daemon -x test \
    && BOOT_JAR="$(ls build/libs/*.jar | grep -v '\-plain\.jar$' | head -n 1)" \
    && cp "$BOOT_JAR" /workspace/application.jar

# ---- Runtime stage ----
FROM eclipse-temurin:17-jre-jammy AS runtime

WORKDIR /app

# Non-root user for safer container runtime
RUN groupadd --system spring && useradd --system --gid spring spring

COPY --from=builder /workspace/application.jar /app/app.jar

RUN chown spring:spring /app/app.jar
USER spring:spring

ENV PORT=8080
EXPOSE 8080

# Prefer IPv4 in cloud/container networks; pass through PORT for Spring
ENTRYPOINT ["java", "-Djava.net.preferIPv4Stack=true", "-jar", "/app/app.jar"]
