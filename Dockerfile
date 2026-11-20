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

# 3. The free DB-IP country database (CC BY 4.0, https://db-ip.com), used only to suggest a visitor's
#    language. This month's file, else last month's; if neither downloads, the app runs without it.
FROM alpine:3.20 AS geo
RUN apk add --no-cache curl && mkdir -p /geo && cd /geo  && for month in "$(date +%Y-%m)" "$(date -d "@$(( $(date +%s) - 2678400 ))" +%Y-%m)"; do       curl -fsSL --max-time 120 "https://download.db-ip.com/free/dbip-country-lite-$month.mmdb.gz" -o db.gz         && gunzip -f db.gz && mv db dbip-country-lite.mmdb && break;     done; ls -la /geo

# 4. Run it on a small image with only the JRE.
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/bazaarready.jar app.jar
COPY --from=geo /geo ./geo
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
