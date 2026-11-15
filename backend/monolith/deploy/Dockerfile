# Runtime-only image for the shared apps-server: the jar is built on your machine by
# deploy/deploy.sh, so the small server never runs Maven (same pattern as the job portal).
FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S app && adduser -S app -G app
WORKDIR /app
COPY --chown=app:app shop.jar shop.jar
RUN mkdir -p /app/uploads && chown app:app /app/uploads
USER app
EXPOSE 8086
# Lean JVM for the shared 2 GB server (demo traffic): heap 256 MB, capped metaspace/code cache/direct
# memory, C1 compiler only (less memory and faster start-up); measured ~330-380 MB resident.
ENV JAVA_TOOL_OPTIONS="-Xms96m -Xmx256m -XX:MaxMetaspaceSize=160m -XX:ReservedCodeCacheSize=48m -XX:MaxDirectMemorySize=32m -XX:TieredStopAtLevel=1 -XX:+UseSerialGC -Xss512k -XX:+ExitOnOutOfMemoryError" \
    TZ=Asia/Kolkata
ENTRYPOINT ["java", "-jar", "shop.jar"]
