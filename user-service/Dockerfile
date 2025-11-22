# --- Stage 1: Build Stage ---
FROM maven:3.9.8-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
# Skip tests to speed up build in dev
RUN mvn package -DskipTests

# --- Stage 2: Run Stage ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Ensure the jar name matches what maven produces
# (Assuming artifactId=user-service and version=0.0.1-SNAPSHOT)
COPY --from=build /app/target/user-service-0.0.1-SNAPSHOT.jar app.jar

# Expose the port
EXPOSE 8081

# Run
ENTRYPOINT ["java", "-jar", "app.jar"]