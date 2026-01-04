# ---- build ----
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -DskipTests package

# ---- run ----
FROM eclipse-temurin:17-jre
WORKDIR /app

# папка под БД (можно монтировать volume)
RUN mkdir -p /data

COPY --from=build /app/target/*-shaded.jar /app/bot.jar

# никаких ENV тут — всё передаём через docker run
ENTRYPOINT ["java", "-jar", "/app/bot.jar"]