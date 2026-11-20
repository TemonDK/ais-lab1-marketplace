FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /build
COPY src/catalog-service/pom.xml src/catalog-service/pom.xml
COPY src/catalog-service/src src/catalog-service/src
COPY docs/api/openapi.yaml src/catalog-service/src/main/resources/static/openapi.yaml
RUN mvn -B -f src/catalog-service/pom.xml package -DskipTests
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /build/src/catalog-service/target/catalog-service-1.0.0.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java","-jar","/app/app.jar"]
