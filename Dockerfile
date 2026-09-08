FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
# Tests run in GitHub Actions; skip here to keep hosted image builds under free-tier RAM.
RUN mvn -B -DskipTests package

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
RUN apt-get update \
    && apt-get install -y --no-install-recommends python3 \
    && rm -rf /var/lib/apt/lists/*
COPY --from=build /app/target/retail-stock-manager.war app.war
COPY docker/early_proxy.py /app/early_proxy.py
COPY docker/entrypoint.sh /app/entrypoint.sh
RUN chmod +x /app/entrypoint.sh
# Fit a 512 MB Render/Cloud Run instance. preferIPv4Stack so the process
# listens on 0.0.0.0:$PORT (Render's scanner does not see IPv6-only binds).
ENV JAVA_TOOL_OPTIONS="-XX:+UseSerialGC -XX:MaxRAMPercentage=65 -XX:MaxMetaspaceSize=128m -Xss512k -Djava.net.preferIPv4Stack=true"
EXPOSE 8080 10000
# Bind $PORT in the entrypoint *before* the JVM so Render does not restart
# mid-boot with "New primary port detected".
ENTRYPOINT ["/app/entrypoint.sh"]
