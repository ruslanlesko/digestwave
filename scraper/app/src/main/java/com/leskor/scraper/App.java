package com.leskor.scraper;

import com.leskor.scraper.sites.Keddr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.time.Duration;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class App {
    private static final Logger logger = LoggerFactory.getLogger("Application");
    public static final String VERSION = "0.0.2";

    public static void main(String[] args) {
        logger.info("Starting Scraper {}", VERSION);

        ExecutorService pool = Executors.newFixedThreadPool(16);

//        var ain = new Ain(createHttpClient(pool));
//        ain.fetchPosts()
//                .join()
//                .forEach(p -> logger.debug("{} -> {}", p.publicationTime(), p.title()));

        var keddr = new Keddr(createHttpClient(pool));
        keddr.fetchPosts()
                .join()
                .forEach(p -> logger.debug("{} -> {}", p.publicationTime(), p.title()));

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
