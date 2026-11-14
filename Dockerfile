# 1. Build the React owner screens.
FROM node:20-alpine AS web
WORKDIR /web
COPY frontend/package*.json ./
RUN npm install --no-audit --no-fund
COPY frontend/ ./
RUN npm run build

# 2. Build the Spring Boot jar, with the React build inside it as static files.
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
COPY --from=web /web/dist ./src/main/resources/static
RUN mvn -q -B package -DskipTests

# 3. Run it on a small image with only the JRE.
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/bazaarready.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
