FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
# Tests already run in GitHub Actions. Skip here so the free-tier image build fits in RAM.
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
# 512 MB Render box. preferIPv4Stack because Render's scanner misses IPv6-only binds.
ENV JAVA_TOOL_OPTIONS="-XX:+UseSerialGC -XX:MaxRAMPercentage=65 -XX:MaxMetaspaceSize=128m -Xss512k -Djava.net.preferIPv4Stack=true"
EXPOSE 8080 10000
# Bind $PORT before the JVM. Otherwise Render restarts mid-boot:
# "New primary port detected".
ENTRYPOINT ["/app/entrypoint.sh"]
