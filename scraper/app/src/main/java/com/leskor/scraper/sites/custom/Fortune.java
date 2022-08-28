package com.leskor.scraper.sites.custom;

import java.net.URI;
import java.net.http.HttpClient;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import com.leskor.scraper.entities.Post;
import com.leskor.scraper.entities.Region;
import com.leskor.scraper.entities.Topic;
import com.leskor.scraper.sites.RSSSite;
import org.jsoup.nodes.Document;

import static java.time.Duration.ofSeconds;

public class Fortune extends RSSSite {
    private static final URI INDEX_URI = URI.create("https://fortune.com/feed/fortune-feeds/?id=3230629");

    public Fortune(URI readabilityUri, HttpClient httpClient) {
        super(
                INDEX_URI,
                readabilityUri,
                "FOR",
                Topic.FINANCE,
                Region.INT,
                httpClient,
                ofSeconds(10),
                null,
                Set.of(),
                Set.of()
        );
    }

    @Override
    protected CompletableFuture<Optional<String>> extractImageURI(Post post) {
        return post == null ? CompletableFuture.completedFuture(Optional.empty())
                : extractArticlePage(URI.create(post.url()))
                .thenApply(document -> document == null ? Optional.empty() : extractImageURLFromDocument(document));
    }

    private Optional<String> extractImageURLFromDocument(Document document) {
        return document.getElementsByTag("meta")
                .stream()
                .filter(e -> e.hasAttr("property") && e.hasAttr("content")
                        && "og:image".equals(e.attr("property"))
                )
                .map(e -> e.attr("content"))
                .findFirst();
    }
}