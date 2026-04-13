FROM maven:3.9.9-eclipse-temurin-23 AS builder
WORKDIR /workspace

COPY pom.xml ./
COPY mvnw ./
COPY .mvn .mvn
RUN chmod +x mvnw

# Download dependencies first to maximize Docker layer cache usage.
RUN ./mvnw -q -DskipTests dependency:go-offline

COPY src src
RUN ./mvnw -q -DskipTests clean package

FROM eclipse-temurin:23-jre
WORKDIR /app

COPY --from=builder /workspace/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
