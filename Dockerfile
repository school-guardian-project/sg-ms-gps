FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre AS final
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
EXPOSE 8842

ENTRYPOINT ["java", "-jar", "app.jar"]
