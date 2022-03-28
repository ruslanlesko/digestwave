# Infrastructure

Docker compose for deploying the whole solution. `sink.json` is payload required for configuring Kafka Connect to sync data from Kafka into PostgreSQL, should be executed as a POST request to Kafka Connect after all services will be up and running.

For convenience, I have added a specific compose file for deploying the infrastructure except for the scraper app. This can be used for running and debugging scraper service locally in a non-containarized way.

`docker-compose -f .\docker-compose-deps-for-scraper.yml up -d`