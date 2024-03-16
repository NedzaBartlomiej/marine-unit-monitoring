 # Use a base image with Java 21
FROM openjdk:21

# Set maintainer label
LABEL author="Bartlomiej Nedza"

# Create a directory for the application
RUN mkdir /usr/src/marine-unit-monitoring
WORKDIR /usr/src/marine-unit-monitoring

# Copy the JAR file of the Spring Boot application into the container
COPY ./target/marine-unit-monitoring.jar /usr/src/marine-unit-monitoring

# Expose the port the application runs on
EXPOSE 9090

# Command to run the Spring Boot application
ENTRYPOINT ["java", "-jar", "/usr/src/marine-unit-monitoring/marine-unit-monitoring.jar"]