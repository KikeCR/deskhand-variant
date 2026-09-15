# --- Build stage ---
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
# Copy the POM first so dependency resolution is cached across builds when only source changes.
COPY pom.xml .
RUN mvn -B dependency:go-offline
COPY src ./src
COPY docs ./docs
RUN mvn -B package -DskipTests

# --- Runtime stage ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/deskhand-variant.jar app.jar
COPY --from=build /app/docs ./docs
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
