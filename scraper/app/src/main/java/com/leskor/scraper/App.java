package com.leskor.scraper;

import com.leskor.scraper.sites.Ain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.time.Duration;

public class App {
    private static final Logger logger = LoggerFactory.getLogger("Application");
    public static final String VERSION = "0.0.1";

    public static void main(String[] args) {
        logger.info("Starting Scraper {}", VERSION);

        var ain = new Ain(createHttpClient());
        ain.fetchPosts().forEach(p -> logger.debug("{} -> {}", p.publicationTime(), p.title()));
    }

    private static HttpClient createHttpClient() {
        return HttpClient.newBuilder()
                .followRedirects(Redirect.NEVER)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }
}
