package com.leskor.digestwave.service;

import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import com.leskor.digestwave.model.Article;
import org.springframework.stereotype.Service;

@Service
public class FeedLoader {

    /**
     * Loads articles from the given InputStream (RSS feed) and filters them based on the provided Instant.
     *
     * @throws IOException if an error occurs while reading the InputStream
     */
    public List<Article> loadArticles(InputStream is, Instant from) throws IOException {
        try {
            SyndFeedInput input = new SyndFeedInput();
            SyndFeed feed = input.build(new XmlReader(is));

            return feed.getEntries().stream()
                    .filter(entry ->
                            entry.getPublishedDate() != null
                                    && entry.getPublishedDate().toInstant().isAfter(from))
                    .map(entry -> new Article(
                                    URI.create(entry.getLink()),
                                    entry.getTitle(),
                                    entry.getPublishedDate().toInstant().atZone(ZoneOffset.UTC)
                            )
                    )
                    .toList();
        } catch (FeedException e) {
            throw new IOException("Failed to parse feed", e);
        }
    }
}
