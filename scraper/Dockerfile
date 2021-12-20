FROM openjdk:17-slim as build
WORKDIR /
COPY . .
RUN ["./gradlew", "jar"]

FROM openjdk:17-slim
COPY --from=build /app/build/libs/app.jar /app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]