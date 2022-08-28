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
import org.jsoup.nodes.Element;

import static java.time.Duration.ofSeconds;

public class Keddr extends RSSSite {
    private static final URI INDEX_URI = URI.create("https://keddr.com/feed/");

    public Keddr(URI readabilityUri, HttpClient httpClient) {
        super(
                INDEX_URI,
                readabilityUri,
                "KDR",
                Topic.TECH,
                Region.UA,
                httpClient,
                ofSeconds(60),
                "|",
                Set.of("Підкаст", "Подкаст"),
                Set.of("Паляниця"),
                false
        );
    }

    @Override
    protected CompletableFuture<Optional<String>> extractImageURI(Post post) {
        return post == null ? CompletableFuture.completedFuture(Optional.empty())
                : extractArticlePage(URI.create(post.url()))
                .thenApply(document -> document == null ? Optional.empty() : extractImageFromDocument(document));
    }

    private Optional<String> extractImageFromDocument(Document document) {
        Element img = document.getElementsByClass("attachment-post-thumbnail").first();
        if (img == null || !img.hasAttr("src")) {
            return Optional.empty();
        }

        return Optional.of(img.attr("src"));
    }
}
