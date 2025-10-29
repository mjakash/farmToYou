# --- Stage 1: Build Stage ---
# Use an official Maven image with Eclipse Temurin (OpenJDK) 21
# This is the corrected image tag
FROM maven:3.9.8-eclipse-temurin-21 AS build

# Set the working directory inside the container
WORKDIR /app

# Copy the Maven project file and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy the rest of the source code and build the application
COPY src ./src
RUN mvn package -DskipTests

# --- Stage 2: Run Stage ---
# Use a slim OpenJDK 21 image for a smaller, more secure final container
# This must also be updated to match the build version
FROM eclipse-temurin:21-jre-alpine

# Set the working directory
WORKDIR /app

# Copy the built .jar file from the 'build' stage
# Make sure your artifact name in pom.xml matches this!
COPY --from=build /app/target/user-service-0.0.1-SNAPSHOT.jar user-service.jar

# Expose the port that our application runs on
EXPOSE 8081

# This is the command that will run when the container starts
ENTRYPOINT ["java", "-jar", "user-service.jar"]