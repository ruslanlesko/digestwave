FROM openjdk:21-slim AS build
WORKDIR /
COPY . .
RUN ["./gradlew", "jar"]

FROM openjdk:21-slim
COPY --from=build /app/build/libs/app.jar /app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]