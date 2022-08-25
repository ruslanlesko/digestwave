package com.leskor.scraper;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leskor.scraper.entities.Post;
import com.leskor.scraper.sites.Site;
import com.leskor.scraper.sites.custom.EconomicnaPravda;
import com.leskor.scraper.sites.custom.Keddr;
import com.leskor.scraper.sites.custom.Liga;
import com.leskor.scraper.sites.custom.NV;
import com.leskor.scraper.sites.custom.Sportarena;
import com.leskor.scraper.sites.custom.Techcrunch;
import com.leskor.scraper.sites.custom.Tribuna;
import com.leskor.scraper.sites.custom.UaFootball;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {
    private static final Logger logger = LoggerFactory.getLogger("Application");

    private static final String VERSION = "0.1.0";

    private static final DuplicatesCache DUPLICATES_CACHE = new DuplicatesCache();

    public static void main(String[] args) {
        logger.info("Starting Scraper {}", VERSION);

        ExecutorService pool = Executors.newFixedThreadPool(16);
        var httpClient = createHttpClient(pool);
        var rssFactory = new RSSFactory(
                new ObjectMapper(),
                "rss.json",
                httpClient,
                Config.getReadabilityURI()
        );

        final int pollingInterval = Config.getPollingInterval();

        try {
            var rssSites = rssFactory.createSites();
            var customSites = createCustomSites(httpClient);
            while (true) {
                handleSites(rssSites);
                handleSites(customSites);
                Thread.sleep(1000L * pollingInterval);
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

    private static List<Site> createCustomSites(HttpClient httpClient) {
        var economicnaPravda = new EconomicnaPravda(httpClient);
        var nv = new NV(Config.getReadabilityURI(), httpClient);
        var liga = new Liga(Config.getReadabilityURI(), httpClient);
        var keddr = new Keddr(Config.getReadabilityURI(), httpClient);
        var uaFootball = new UaFootball(Config.getReadabilityURI(), httpClient);
        var sportarena = new Sportarena(Config.getReadabilityURI(), httpClient);
        var tribuna = new Tribuna(Config.getReadabilityURI(), httpClient);
        var techcrunch = new Techcrunch(Config.getReadabilityURI(), httpClient);
        return List.of(economicnaPravda, nv, liga, keddr, uaFootball, sportarena, tribuna, techcrunch);
    }

    private static void handleSites(List<? extends Site> sites) {
        Optional<String> siteLimitation = Config.getSpecificSiteCodeLimitation();
        KafkaPostsProducer kafkaPostsProducer = new KafkaPostsProducer();
        for (var site : sites) {
            if (siteLimitation.isPresent() && !siteLimitation.get().equals(site.getSiteCode())) {
                continue;
            }
            var posts = site.fetchPosts().join();
            logger.debug("Processing duplicates");
            var filteredPosts = posts.stream().filter(App::processDuplicateCache).toList();
            logger.debug("Sending posts:");
            filteredPosts.forEach(p -> logger.debug("{} -> {}", p.publicationTime(), p.title()));
            kafkaPostsProducer.sendPosts(filteredPosts);
        }
        kafkaPostsProducer.close();
    }

    private static boolean processDuplicateCache(Post post) {
        if (DUPLICATES_CACHE.contains(post.hash())) {
            logger.debug("Duplicate: {}", post.title());
            return false;
        }
        DUPLICATES_CACHE.add(post.hash());
        return true;
    }

    private static HttpClient createHttpClient(Executor executor) {
        return HttpClient.newBuilder()
                .followRedirects(Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(10))
                .executor(executor)
                .build();
    }
}
