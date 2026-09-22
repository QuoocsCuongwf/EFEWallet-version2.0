# Build stage
FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests


# Run stage
FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

# Port is configured via SERVER_PORT env variable
EXPOSE ${SERVER_PORT:-8080}

ENTRYPOINT ["java", "-jar", "app.jar"]