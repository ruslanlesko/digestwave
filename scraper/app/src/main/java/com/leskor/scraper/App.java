package com.leskor.scraper;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.time.Duration;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class App {
    private static final Logger logger = LoggerFactory.getLogger("Application");
    public static final String VERSION = "0.1.0";

    public static void main(String[] args) {
        logger.info("Starting Scraper {}", VERSION);

        ExecutorService pool = Executors.newFixedThreadPool(16);
        KafkaPostsProducer kafkaPostsProducer = new KafkaPostsProducer();

        var rssFactory = new RSSFactory(new ObjectMapper(), "rss.json", createHttpClient(pool));
        try {
            var rssSites = rssFactory.createSites();
            for (var site : rssSites) {
                var posts = site.fetchPosts().join();
                logger.debug("Sending posts:");
                posts.forEach(p -> logger.debug("{} -> {}", p.publicationTime(), p.title()));
                kafkaPostsProducer.sendPosts(posts);
            }
        } catch (IOException e) {
            logger.error("Failed to create RSS sites", e);
        }

        kafkaPostsProducer.close();
        pool.shutdown();
    }

    private static HttpClient createHttpClient(Executor executor) {
        return HttpClient.newBuilder()
                .followRedirects(Redirect.NEVER)
                .connectTimeout(Duration.ofSeconds(10))
                .executor(executor)
                .build();
    }
}
