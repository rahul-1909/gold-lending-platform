# Stage 1: Build application with Maven and Java 17
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /app

# Copy pom.xml and source code
COPY pom.xml .
COPY src ./src

# Build production jar skipping tests
RUN mvn clean package -DskipTests

# Stage 2: Minimal runtime image
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy the built jar
COPY --from=build /app/target/starfinance-0.0.1-SNAPSHOT.jar app.jar

# Render assigns dynamic port via PORT environment variable
EXPOSE 8080

# Keep JVM heap within Render free tier (512MB memory limit)
ENV JAVA_OPTS="-Xms128m -Xmx384m"

ENTRYPOINT ["sh", "-c", "java  -jar app.jar"]
