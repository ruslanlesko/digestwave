# Provider

Service for providing REST API for entire system.

## Requirements

* Java 21
* PostgreSQL 14

## Compiling and running Provider

```bash
./gradlew bootJar
java -jar app/build/libs/app.jar
```

## API Reference

Documentation for API is available in Swagger form at `localhost:8080/swagger-ui.html`

## Environment variables

Provider is configured via environment variables.

| Name                 | Meaning                        | 
|----------------------|--------------------------------|
| DB_HOST              | Database host (e.g. localhost) |
| DB_USER              | Database user                  |
| DB_PWD               | Database password              |