# ─── BUILD STAGE ──────────────────────────────────────────
FROM maven:3.8.8-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests -q

# ─── RUN STAGE ────────────────────────────────────────────
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/target/bakery-saas-backend-2.0.0.jar app.jar

# Exposed port (Render binds to $PORT dynamically)
EXPOSE 8080

# Run the Spring Boot app
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]
