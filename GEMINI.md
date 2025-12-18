# Project Overview

This project is a news analytics application called Digestwave. It processes RSS feeds, extracts keywords from news titles, and determines their sentiments using LLM. The application is built with Java 21 and Spring Boot, using Gradle for dependency management. The frontend is a single HTML file powered by HTMX and vanilla Javascript. It uses Cassandra as a database to store mentions and keyword statistics. Docker Compose is used to manage the Cassandra and Ollama AI services.

# Building and Running

## Prerequisites

- Java 21 or higher
- Gradle
- Docker

## Running the Application

1.  **Start Services:** Launch the Cassandra and Ollama services using Docker Compose:
    ```bash
    docker compose up -d
    ```

2.  **Initialize Database:** Run the script to initialize the Cassandra database schema (if data is not already present):
    ```bash
    sh cassandra-init.sh
    ```

3.  **Run Application:** Start the Spring Boot application using the Gradle wrapper:
    ```bash
    ./gradlew bootRun
    ```

## Running Tests

- **Unit Tests:**
  ```bash
  ./gradlew test
  ```

- **Integration Tests:**
  ```bash
  ./gradlew integrationTest
  ```

# Development Conventions

## Code Style

The project follows standard Java conventions.

## Testing

The project has both unit and integration tests. 
- Unit tests are located in `src/test/java` and can be run with `./gradlew test`.
- Integration tests are also located in `src/test/java` but are tagged with `@Tag("integration")`. They can be run with `./gradlew integrationTest`.

## Branching Strategy

The project seems to use a feature-branching workflow, with pull requests being merged into the `main` branch.

## CI/CD

The project has a CI pipeline configured in `.github/workflows/ci.yml` that runs on every push to the `main` branch. It builds the project and runs the tests.
