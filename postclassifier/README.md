# Post Classifier

Service for classifying posts into top interesting posts.

## Requirements

* Java 21
* Ollama 0.1.27
* PostgreSQL 14

## Usage

Post classifier runs on port 8082 with a single endpoint `POST /top` which does all the work.

## Compiling and running Post Classifier

```bash
./gradlew bootJar
java -jar build/libs/app.jar
```

## Environment variables

Post Classifier has default values for some of its configuration. However, you may want to substitute some parameters
for your specific
needs. Scraper is configured via environment variables.

| Name              | Required | Default                    | Meaning                                                   |
| ----------------- | -------- | -------------------------- | --------------------------------------------------------- |
| DB_HOST           | Yes      |                            | PostgreSQL database host name or IP (such as `localhost`) |
| DB_USER           | Yes      |                            | PostgreSQL database username                              |
| DB_PWD            | Yes      |                            | PostgreSQL database password                              |
| PC_MODEL          | No       | phi:latest                 | [Ollama model](https://ollama.com/library) used           |
| PC_OLLAMA_HOST    | No       | <http://localhost:11434>   | Ollama URI                                                |
| PC_OLLAMA_TIMEOUT | No       | 120                        | Ollama timeout in seconds                                 |
| PC_PROMPT         | No       | Defined in the source code | Prompt for Ollama request                                 |
| PC_LOG_LEVEL      | No       | INFO                       | Logging level                                             |
| PC_KAFKA_HOST     | No       | localhost:19092            | Address of Kafka broker                                   |
