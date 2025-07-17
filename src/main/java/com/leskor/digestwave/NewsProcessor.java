package com.leskor.digestwave;

import com.leskor.digestwave.cache.Bookkeeper;
import com.leskor.digestwave.config.FeedProperties;
import com.leskor.digestwave.service.ArticleProcessor;
import com.leskor.digestwave.service.FeedLoader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class NewsProcessor {
    private static final Logger logger = LoggerFactory.getLogger(NewsProcessor.class);

    private final FeedProperties feedProperties;
    private final FeedLoader feedLoader;
    private final Bookkeeper bookkeeper;
    private final ArticleProcessor articleProcessor;

    @Autowired
    public NewsProcessor(
            FeedProperties feedProperties,
            FeedLoader feedLoader,
            Bookkeeper bookkeeper,
            ArticleProcessor articleProcessor) {
        this.feedProperties = feedProperties;
        this.feedLoader = feedLoader;
        this.bookkeeper = bookkeeper;
        this.articleProcessor = articleProcessor;
    }

    @Scheduled(fixedDelay = 2, timeUnit = TimeUnit.HOURS)
    public void process() {
        for (String url : feedProperties.urls()) {
            try {
                processUrl(url);
            } catch (Exception e) {
                logger.error("Error processing news for {}", url, e);
            }
        }
    }

    private void processUrl(String url) throws URISyntaxException, IOException {
        URI uri = new URI(url);

        Instant lastFetchTime = bookkeeper.lastFetchTime(uri).orElse(Instant.EPOCH);

        try (InputStream is = uri.toURL().openStream()) {
            feedLoader.loadArticles(is, lastFetchTime)
                    .stream()
                    .map(articleProcessor::processArticle)
                    .max(Instant::compareTo)
                    .ifPresent(latestPublished -> bookkeeper.saveFetchTime(uri, latestPublished));
        }
    }
}
