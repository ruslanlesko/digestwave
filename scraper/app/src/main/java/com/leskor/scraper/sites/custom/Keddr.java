package com.leskor.scraper.sites.custom;

import com.leskor.scraper.entities.Post;
import com.leskor.scraper.entities.Region;
import com.leskor.scraper.entities.Topic;
import com.leskor.scraper.sites.RSSSite;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

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
                Set.of("Паляниця")
        );
    }

    @Override
    protected List<CompletableFuture<Post>> extractPostsBasedOnPage(String page) {
        return extractPostURIsWithPublicationTimeFromPage(page)
                .stream()
                .filter(Objects::nonNull)
                .map(this::extractPost)
                .map(p -> p.thenCompose(this::enrichWithImage))
                .toList();
    }

    private CompletableFuture<Post> enrichWithImage(Post post) {
        if (post.imageURL() != null && !post.imageURL().isBlank()) {
            return CompletableFuture.completedFuture(post);
        }
        return extractImageUrlFromPostURI(URI.create(post.url()))
                .thenApply(opt -> opt.isEmpty() ? post : post.withImageURL(opt.get()));
    }

    private CompletableFuture<Optional<String>> extractImageUrlFromPostURI(URI postURI) {
        return httpClient.sendAsync(buildPageRequest(postURI), HttpResponse.BodyHandlers.ofString(charset()))
                .thenApply(response -> {
                    if (response.statusCode() != 200) {
                        logger.error("Failed to fetch keddr article, status {}", response.statusCode());
                        return Optional.empty();
                    }
                    String body = response.body();
                    if (body == null || body.isBlank()) {
                        logger.warn("Cannot parse keddr response, body is blank");
                        return Optional.empty();
                    }

                    return extractImageFromDocument(Jsoup.parse(response.body(), Parser.htmlParser()));
                });
    }

    private Optional<String> extractImageFromDocument(Document document) {
        Element img = document.getElementsByClass("attachment-post-thumbnail").first();
        if (img == null || !img.hasAttr("src")) {
            return Optional.empty();
        }

        return Optional.of(img.attr("src"));
    }

    private HttpRequest buildPageRequest(URI postURI) {
        return HttpRequest.newBuilder(postURI)
                .GET()
                .header("Content-Type", "text/html")
                .timeout(DEFAULT_TIMEOUT)
                .build();
    }
}
