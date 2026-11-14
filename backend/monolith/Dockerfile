# syntax=docker/dockerfile:1.7
# Self-contained build (CI / local): docker build -t ecommerce-monolith .
FROM maven:3.9.9-eclipse-temurin-21-alpine AS build
WORKDIR /workspace
COPY pom.xml ./
RUN --mount=type=cache,target=/root/.m2 mvn -B -q dependency:go-offline || true
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -B -q package -DskipTests \
 && java -Djarmode=tools -jar target/ecommerce-monolith-*.jar extract --layers --launcher --destination /workspace/extracted

FROM eclipse-temurin:25-jre-alpine
RUN addgroup -S app && adduser -S app -G app
WORKDIR /app
COPY --from=build --chown=app:app /workspace/extracted/dependencies/ ./
COPY --from=build --chown=app:app /workspace/extracted/spring-boot-loader/ ./
COPY --from=build --chown=app:app /workspace/extracted/snapshot-dependencies/ ./
COPY --from=build --chown=app:app /workspace/extracted/application/ ./
USER app
EXPOSE 8086
ENV JAVA_TOOL_OPTIONS="-Xms96m -Xmx256m -XX:MaxMetaspaceSize=160m -XX:ReservedCodeCacheSize=48m -XX:MaxDirectMemorySize=32m -XX:TieredStopAtLevel=1 -XX:+UseSerialGC -Xss512k -XX:+ExitOnOutOfMemoryError" \
    TZ=Asia/Kolkata
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
