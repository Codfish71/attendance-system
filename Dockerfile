# Stage 1: Build the application with Gradle
FROM gradle:8.4-jdk17-alpine AS build
WORKDIR /app

# Copy only the necessary files for dependency resolution first
COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle

# FIX: Add execute permission to the Gradle wrapper
RUN chmod +x ./gradlew

# Copy the source code
COPY src ./src

# Build the project. --no-daemon is recommended for CI/CD environments.
RUN ./gradlew build --no-daemon -x test

# Stage 2: Create the final, lightweight image
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy the built JAR file from the 'build' stage
COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]