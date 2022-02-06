# Scraper

Service for parsing websites and extracting posts into Kafka topic.

## Requirements

* Java 17
* Kafka 2.7.1
* Confluent Schema Registry 7.0.1
* [Mozilla's Readability](https://github.com/phpdocker-io/readability-js-server) service

## Compiling and running Scraper

```bash
cd app
./gradlew jar
java -jar build/libs/app.jar
```

## Running the whole solution via docker compose

`docker-compose up -d`

This will build a Docker image for scraper and run it with Kafka

## Running Kafka and Readability dockerized

`docker-compose -f .\docker-compose-deps.yml up -d`

This will run Kafka (with Zookeeper) on port 9092 and Readability on port 3009. Useful for local debugging.

## Environment variables

Scraper has default values for its configuration. However, you may want to substitute some parameters for your specific
needs. Scraper is configured via environment variables.

| Name                        | Default               | Meaning                                                                             | 
|-----------------------------|-----------------------|-------------------------------------------------------------------------------------|
| SCR_READABILITY_URI         | http://localhost:3009 | URI of [Readability service](https://github.com/phpdocker-io/readability-js-server) |
| SCR_KAFKA_ADDRESS           | localhost:19092       | Address of Kafka broker                                                             |
| SCR_SCHEMA_REGISTRY_ADDRESS | http://localhost:8081 | Address of Kafka Schema Registry                                                    |
| SCR_LOGGING_LEVEL           | info                  | Logging level                                                                       |
| SCR_POLLING_INTERVAL        | 20                    | Polling interval seconds                                                            |