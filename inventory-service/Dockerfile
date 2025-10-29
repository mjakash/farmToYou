# --- Stage 1: Build Stage ---
FROM maven:3.9.8-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn package -DskipTests

# --- Stage 2: Run Stage ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# --- IMPORTANT: Change this .jar file name ---
COPY --from=build /app/target/inventory-service-0.0.1-SNAPSHOT.jar inventory-service.jar

# Expose the port for this service
EXPOSE 8083

ENTRYPOINT ["java", "-jar", "inventory-service.jar"]