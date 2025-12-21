package com.leskor.digestwave;

import com.leskor.digestwave.cache.ArticleCache;
import com.leskor.digestwave.config.FeedProperties;
import com.leskor.digestwave.model.Article;
import com.leskor.digestwave.service.ArticleProcessor;
import com.leskor.digestwave.service.FeedLoader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class NewsProcessor {
    private static final Logger logger = LoggerFactory.getLogger(NewsProcessor.class);

    private final FeedProperties feedProperties;
    private final FeedLoader feedLoader;
    private final ArticleProcessor articleProcessor;
    private final ArticleCache articleCache;

    public NewsProcessor(
            FeedProperties feedProperties,
            FeedLoader feedLoader,
            ArticleProcessor articleProcessor,
            ArticleCache articleCache) {
        this.feedProperties = feedProperties;
        this.feedLoader = feedLoader;
        this.articleProcessor = articleProcessor;
        this.articleCache = articleCache;
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

        try (InputStream is = uri.toURL().openStream()) {
            logger.info("Processing feed: {}", uri);
            List<Article> articles = feedLoader.loadArticles(is).stream()
                    .filter(article -> {
                        boolean exists = articleCache.exists(article);
                        if (exists) {
                            logger.info("Skipping article as it is already processed: {}", article.uri());
                        }
                        return !exists;
                    }).toList();

            articles.forEach(articleProcessor::processArticle);

            logger.info("Processed {} new articles from {}", articles.size(), uri);
        }
    }
}
