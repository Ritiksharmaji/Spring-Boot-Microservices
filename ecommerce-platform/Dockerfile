# syntax=docker/dockerfile:1.7
# ---------------------------------------------------------------------------
# One Dockerfile for every service:
#   docker build --build-arg MODULE=order-service -t ecommerce/order-service .
# Stage 1 builds the module with Maven, stage 2 runs it on a small JRE image
# using Spring Boot's layered jar (dependencies change rarely -> better caching).
# ---------------------------------------------------------------------------

FROM maven:3.9.9-eclipse-temurin-21-alpine AS build
ARG MODULE
WORKDIR /workspace

# 1) POMs first so the dependency download is cached until a pom.xml changes
COPY pom.xml ./
COPY config-server/pom.xml config-server/
COPY discovery-server/pom.xml discovery-server/
COPY api-gateway/pom.xml api-gateway/
COPY user-service/pom.xml user-service/
COPY product-service/pom.xml product-service/
COPY order-service/pom.xml order-service/
COPY payment-service/pom.xml payment-service/
COPY notification-service/pom.xml notification-service/
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -q -pl ${MODULE} -am dependency:go-offline -DexcludeReactor=true || true

# 2) Sources, then build only the requested module
COPY . .
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -q -pl ${MODULE} -am package -DskipTests \
 && cp ${MODULE}/target/${MODULE}-*.jar /workspace/app.jar \
 && java -Djarmode=tools -jar /workspace/app.jar extract --layers --launcher --destination /workspace/extracted

# ---------------------------------------------------------------------------
FROM eclipse-temurin:25-jre-alpine
ARG MODULE
LABEL org.opencontainers.image.title="${MODULE}" \
      org.opencontainers.image.source="https://github.com/Ritiksharmaji/Spring-Boot-Microservices"

RUN addgroup -S app && adduser -S app -G app \
 && mkdir -p /app/logs && chown -R app:app /app
WORKDIR /app

COPY --from=build --chown=app:app /workspace/extracted/dependencies/ ./
COPY --from=build --chown=app:app /workspace/extracted/spring-boot-loader/ ./
COPY --from=build --chown=app:app /workspace/extracted/snapshot-dependencies/ ./
COPY --from=build --chown=app:app /workspace/extracted/application/ ./

USER app
# Heap follows the container memory limit; crash fast on OOM so Docker restarts the container
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=60 -XX:+ExitOnOutOfMemoryError -XX:+UseSerialGC -Xss512k" \
    TZ=Asia/Kolkata
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
