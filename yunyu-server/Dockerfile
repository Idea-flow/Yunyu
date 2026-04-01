FROM maven:3.9.11-eclipse-temurin-25 AS builder

WORKDIR /build

COPY pom.xml .
COPY src ./src

RUN mvn -B -DskipTests clean package

FROM eclipse-temurin:25-jre

WORKDIR /app

COPY --from=builder /build/target/yunyu-server-0.0.1-SNAPSHOT.jar /app/yunyu-server.jar

EXPOSE 20000

ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "/app/yunyu-server.jar"]
