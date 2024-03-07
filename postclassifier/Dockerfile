FROM openjdk:21-slim AS build
WORKDIR /
COPY . .
RUN ["./gradlew", "bootJar"]

FROM openjdk:21-slim
COPY --from=build /build/libs/app.jar /app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]