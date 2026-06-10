# Stage 1: Build the application using Maven
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copy pom-web.xml as pom.xml so Maven builds the Web version of the application
COPY pom-web.xml ./pom.xml
COPY src ./src

# Build the package (JAR file)
RUN mvn clean package -DskipTests

# Stage 2: Create runtime container
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copy the built jar from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose port (Render will override this using the PORT env variable)
EXPOSE 8081

# Run the spring-boot application
ENTRYPOINT ["java", "-jar", "app.jar"]
