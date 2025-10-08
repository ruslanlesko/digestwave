FROM gradle:8.10.2-jdk21-alpine AS builder
WORKDIR /app
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
RUN ./gradlew dependencies --no-daemon || true
COPY src src
RUN ./gradlew bootJar --no-daemon -x test

FROM eclipse-temurin:21.0.5_11-jre-alpine
RUN addgroup -S spring && adduser -S spring -G spring
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
RUN chown -R spring:spring /app
USER spring
EXPOSE 8080
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
