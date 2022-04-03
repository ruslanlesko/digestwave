#!/usr/bin/python3

import sys
import requests
import os

base = "http://localhost:8083"
env_base = os.getenv("BASE_URL")
if env_base:
    base = env_base
print("Connecting to Kafka Connect on", base)

r = requests.get(base + "/connectors")
if r.status_code != 200:
    print("Failed to obtain list of connectors")
    sys.exit(1)
l = r.json()
if l == []:
    print("No connectors is configured, creating...")
    body = {
        "name": "jdbc-sink",
        "config": {
            "connector.class": "io.confluent.connect.jdbc.JdbcSinkConnector",
            "tasks.max": "1",
            "topics": "posts",
            "connection.url": "jdbc:postgresql://db:5432/digestwave?user=connect&password=connectpass",
            "value.converter": "io.confluent.connect.json.JsonSchemaConverter",
            "value.converter.schema.registry.url": "http://schema:8081",
            "auto.create": "false",
            "insert.mode": "upsert",
            "delete.enabled": "false",
            "pk.fields": "hash",
            "pk.mode": "record_value",
            "batch.size": "10",
            "dialect.name": "PostgreSqlDatabaseDialect"
        }
    }
    r = requests.post(base + "/connectors", json = body)
    if r.status_code != 201:
        print("Failed to create a new connector")
        sys.exit(2)
    else:
        print("Connector was successfully added")
else:
    print("Connector is aleady present")