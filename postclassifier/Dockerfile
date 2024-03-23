FROM openjdk:21-slim AS build
WORKDIR /usr/build
COPY . .
RUN ["./gradlew", "bootJar"]

FROM openjdk:21-slim
COPY --from=build /usr/build/build/libs/app.jar /app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]