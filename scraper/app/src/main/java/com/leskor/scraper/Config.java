package com.leskor.scraper;

import java.net.URI;

import static java.lang.System.getenv;

public final class Config {
    private Config() {
    }

    public static URI getReadabilityURI() {
        return URI.create(
                getenv("SCR_READABILITY_URI") == null ?
                        "http://localhost:3009" : getenv("SCR_READABILITY_URI")
        );
    }

    public static String getKafkaAddress() {
        return getenv("SCR_KAFKA_ADDRESS") == null ?
                "localhost:19092" : getenv("SCR_KAFKA_ADDRESS");
    }

    public static int getPollingInterval() {
        return getenv("SCR_POLLING_INTERVAL") == null ?
                20 : Integer.parseInt(getenv("SCR_POLLING_INTERVAL"));
    }

    public static String getSchemaRegistryAddress() {
        return getenv("SCR_SCHEMA_REGISTRY_ADDRESS") == null ?
                "http://localhost:8081" : getenv("SCR_SCHEMA_REGISTRY_ADDRESS");
    }
}
