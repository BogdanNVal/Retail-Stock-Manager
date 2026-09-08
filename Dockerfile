# --- Etapa 1: compilare cu Maven ---
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
# Tests run in GitHub Actions; skip here to keep hosted image builds under free-tier RAM.
RUN mvn -B -DskipTests package

# --- Etapa 2: imagine finala, doar cu JRE + fisierul .war ---
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/target/retail-stock-manager.war app.war
# Fit a 512 MB Render/Cloud Run instance. preferIPv4Stack so the process
# listens on 0.0.0.0:$PORT (Render's scanner does not see IPv6-only binds).
ENV JAVA_TOOL_OPTIONS="-XX:+UseSerialGC -XX:MaxRAMPercentage=70 -Djava.net.preferIPv4Stack=true"
EXPOSE 8080 10000
# Do not pass --server.port=$PORT: on Render that port is bound immediately
# by EarlyBindProxy while Tomcat starts on loopback.
ENTRYPOINT ["sh", "-c", "exec java -jar app.war"]
