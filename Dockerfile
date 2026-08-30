# syntax=docker/dockerfile:1

# --- Stage 1 : build Maven multi-module ---
FROM maven:3.9.11-eclipse-temurin-25 AS build
WORKDIR /workspace

COPY pom.xml .
COPY obv-domain/pom.xml obv-domain/pom.xml
COPY obv-application/pom.xml obv-application/pom.xml
COPY obv-infrastructure/pom.xml obv-infrastructure/pom.xml
COPY obv-api/pom.xml obv-api/pom.xml
COPY obv-bootstrap/pom.xml obv-bootstrap/pom.xml
RUN --mount=type=cache,target=/root/.m2 \
    mvn -q -B dependency:go-offline || true

COPY obv-domain obv-domain
COPY obv-application obv-application
COPY obv-infrastructure obv-infrastructure
COPY obv-api obv-api
COPY obv-bootstrap obv-bootstrap
RUN --mount=type=cache,target=/root/.m2 \
    mvn -q -B -DskipTests package

# --- Stage 2 : runtime JRE 25 minimal, utilisateur non-root ---
FROM eclipse-temurin:25-jre-alpine AS runtime
RUN apk add --no-cache curl \
    && addgroup -S obv && adduser -S obv -G obv
WORKDIR /app
COPY --from=build /workspace/obv-bootstrap/target/obv-gestion.jar app.jar
USER obv

EXPOSE 8080
HEALTHCHECK --interval=10s --timeout=5s --start-period=40s --retries=5 \
    CMD curl -f http://localhost:8080/actuator/health/readiness || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
