# Infrastructure

Docker compose for deploying the whole solution. `sink.json` is payload required for configuring Kafka Connect to sync data from Kafka into PostgreSQL, should be executed as a POST request to Kafka Connect after all services will be up and running.