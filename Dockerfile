FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

RUN chmod +x ./mvnw && ./mvnw dependency:go-offline -B

COPY src src

RUN chmod +x ./mvnw && ./mvnw clean package -DskipTests -B

FROM eclipse-temurin:21-jre-alpine

RUN addgroup -S orderservice && adduser -S orderservice -G orderservice

RUN apk add --no-cache wget

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

RUN chown -R orderservice:orderservice /app

USER orderservice

EXPOSE 8083

ENV SPRING_PROFILES_ACTIVE=docker
ENV SERVER_PORT=8083
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:InitialRAMPercentage=50.0"

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget -q --spider http://localhost:8083/actuator/health || exit 1

ENTRYPOINT exec java $JAVA_OPTS -jar app.jar
