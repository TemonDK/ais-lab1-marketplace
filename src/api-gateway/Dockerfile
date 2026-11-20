FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /build
COPY src/api-gateway/pom.xml src/api-gateway/pom.xml
COPY src/api-gateway/src src/api-gateway/src
RUN mvn -B -f src/api-gateway/pom.xml package -DskipTests
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /build/src/api-gateway/target/api-gateway-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
