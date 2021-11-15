package com.leskor.scraper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leskor.scraper.sites.Site;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class App {
    private static final Logger logger = LoggerFactory.getLogger("Application");
    private static final String VERSION = "0.1.0";
    private static final long INTERVAL_SECONDS = 20;

    public static void main(String[] args) {
        logger.info("Starting Scraper {}", VERSION);

        ExecutorService pool = Executors.newFixedThreadPool(16);
        var rssFactory = new RSSFactory(new ObjectMapper(), "rss.json", createHttpClient(pool));
        try {
            var rssSites = rssFactory.createSites();
            while (true) {
                handleSites(rssSites);
                Thread.sleep(1000 * INTERVAL_SECONDS);
            }
        } catch (IOException e) {
            logger.error("Failed to create RSS sites", e);
        } catch (InterruptedException e) {
            logger.error("Interrupted");
        } finally {
            logger.info("Shutting down...");
            pool.shutdown();
        }
    }

    private static void handleSites(List<? extends Site> sites) {
        KafkaPostsProducer kafkaPostsProducer = new KafkaPostsProducer();
        for (var site : sites) {
            var posts = site.fetchPosts().join();
            logger.debug("Sending posts:");
            posts.forEach(p -> logger.debug("{} -> {}", p.publicationTime(), p.title()));
            kafkaPostsProducer.sendPosts(posts);
        }
        kafkaPostsProducer.close();
    }

    private static HttpClient createHttpClient(Executor executor) {
        return HttpClient.newBuilder()
                .followRedirects(Redirect.NEVER)
                .connectTimeout(Duration.ofSeconds(10))
                .executor(executor)
                .build();
    }
}
