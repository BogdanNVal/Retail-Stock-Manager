# --- Etapa 1: compilare cu Maven ---
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
COPY src/main/webapp ./src/main/webapp
RUN mvn clean package -DskipTests

# --- Etapa 2: imagine finala, doar cu JRE + fisierul .war ---
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/target/retail-stock-manager.war app.war
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.war"]
