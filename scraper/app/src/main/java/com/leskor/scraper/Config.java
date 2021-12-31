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
                "localhost:9092" : getenv("SCR_KAFKA_ADDRESS");
    }

    public static int getPollingInterval() {
        return getenv("SCR_POLLING_INTERVAL") == null ?
                20 : Integer.parseInt(getenv("SCR_POLLING_INTERVAL"));
    }
}
