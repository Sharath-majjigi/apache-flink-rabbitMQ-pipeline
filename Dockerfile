# Stage 1: Build the application with Gradle
FROM gradle:jdk21 as build
WORKDIR /app

# Copy only the necessary files to avoid cache invalidation
COPY build.gradle settings.gradle /app/
COPY src /app/src

# Build the application
RUN gradle clean build -x test --no-daemon

# Stage 2: Package the application into a lightweight image
FROM openjdk:21
WORKDIR /app

# Copy the built jar file from the previous stage
COPY --from=build /app/build/libs/*.jar /app/application.jar

# Expose the application port (if needed, usually 8080 for Spring Boot)
EXPOSE 8080

# Define the command to run the application
ENTRYPOINT ["java", "-jar", "/app/application.jar"]
