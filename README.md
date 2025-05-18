# Digestwave

Digestwave is a news analytics application designed to process RSS feeds, extract keywords from news titles and their sentiments.

## Features

- **RSS Feed Processing**: Fetches articles from multiple RSS feeds at scheduled intervals.
- **Metadata Extraction**: Uses AI to extract keywords and determine the sentiment of articles.
- **Data Storage**: Stores mentions and keyword statistics in a Cassandra database.

## Technologies Used

- **Java 21**: Core programming language.
- **Spring Boot**: Framework for building the application.
- **Gradle**: Build tool for dependency management and project configuration.
- **Cassandra**: NoSQL database for storing mentions and keyword statistics.
- **AI Integration**: Uses the Ollama AI model for metadata extraction.
- **Docker Compose**: Manages the Cassandra and AI services.

## Prerequisites

- Java 21 or higher
- Gradle

## Getting Started (simplified for local development)

### 1. Run Cassandra and Ollama using Docker Compose

```bash
docker compose up -d
```

### 2. Initialize the Database

```bash
sh cassandra-init.sh
```

### 3. Run the project

```bash
./gradlew bootRun
```

## Useful Commands

### Connect to Cassandra

```bash
docker exec -it cassandra cqlsh
```

## Data Import/Export

Python scripts for loading and exporting data to/from Cassandra are provided in the `scripts` directory. These scripts can be used to import/export data in CSV format.

Currently, Python 3.11 is required to run the scripts. You can install it using venv.
