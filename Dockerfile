# ---- build stage: has Maven + full JDK, used only to produce the jar ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# copy dependency descriptor first so Docker can cache the downloaded deps
# as a separate layer — re-downloaded only when pom.xml changes
COPY pom.xml .
RUN mvn -B dependency:go-offline

# copy source and build the fat jar, skipping tests (tests run in CI, not here)
COPY src ./src
RUN mvn -B clean package -DskipTests

# ---- runtime stage: lean JRE only, no Maven or full JDK ----
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# copy only the fat jar produced by the build stage
COPY --from=build /app/target/*.jar app.jar

# Spring Boot default port
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]