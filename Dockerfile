# Build Stage: Use Maven image with Eclipse Temurin JDK 21 on Alpine Linux
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build

# Set the working directory inside the container
WORKDIR /app

# Copy Maven project file and download dependencies offline
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy the source code into the container
COPY src ./src

# Build the project and create the executable JAR (skip tests)
RUN mvn package -DskipTests

# Runtime Stage: Use the Eclipse Temurin JRE 21 image on Alpine Linux
FROM eclipse-temurin:21-jre-alpine

# Set the working directory inside the container
WORKDIR /app

# Copy the built JAR from the build stage to the current stage
COPY --from=build /app/target/webchat-0.0.1-SNAPSHOT.jar app.jar

# Expose the port where the Spring Boot application runs
EXPOSE 8080

# Command to execute the application
ENTRYPOINT ["java", "-jar", "app.jar"]