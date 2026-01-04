# ---- build stage ----
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn -DskipTests clean package

# ---- runtime stage ----
FROM eclipse-temurin:17-jre
WORKDIR /app

# директория под БД (будет монтироваться volume)
RUN mkdir -p /data

# ВАЖНО: jar фиксированно называется target/bot.jar
COPY --from=build /app/target/bot.jar /app/bot.jar

ENTRYPOINT ["java", "-jar", "/app/bot.jar"]
