FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /build
COPY src/cart-service/pom.xml src/cart-service/pom.xml
COPY src/cart-service/src src/cart-service/src
RUN mvn -B -f src/cart-service/pom.xml package -DskipTests
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /build/src/cart-service/target/cart-service-1.0.0.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java","-jar","/app/app.jar"]
