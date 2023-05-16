# Use an official OpenJDK runtime as the base image
FROM openjdk:11-jre-slim

# Set the working directory in the container
WORKDIR /app

# Copy the executable JAR file from the build output directory to the container
COPY target/topan-project-0.0.1-SNAPSHOT.jar app.jar

# Expose the port that the application listens on (change if necessary)
EXPOSE 8080

# Run the application when the container starts
CMD ["java", "-jar", "app.jar"]
