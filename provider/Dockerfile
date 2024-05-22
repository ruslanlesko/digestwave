FROM openjdk:21-slim AS build
WORKDIR /
COPY . .
RUN ["./gradlew", "bootJar"]

FROM openjdk:21-slim
COPY --from=build /app/build/libs/app-0.1.0.jar /app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]