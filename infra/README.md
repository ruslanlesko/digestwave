# Infrastructure

Docker compose for deploying the whole solution. `sink.json` is payload required for configuring Kafka Connect to sync data from Kafka into PostgreSQL, should be executed as a POST request to Kafka Connect after all services will be up and running.

By default, docker compose will be run without scraper service (for the ability to debug it locally). Profile `withscraper` should be applied to run a whole stack. Web UI will be available at `localhost:8090`.

`docker compose --profile withscraper up -d`

Please remember to provide profile parameter when shutting down.

`docker compose --profile withscraper down`

To run the stack wihout scraper service, execute plain docker compose up command without any profiles.

`docker-compose up -d`