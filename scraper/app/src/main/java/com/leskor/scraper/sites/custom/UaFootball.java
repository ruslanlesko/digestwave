package com.leskor.scraper.sites.custom;

import com.leskor.scraper.entities.Post;
import com.leskor.scraper.entities.Region;
import com.leskor.scraper.entities.Topic;
import com.leskor.scraper.sites.RSSSite;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static java.time.Duration.ofSeconds;

public class UaFootball extends RSSSite {
    private static final URI INDEX_URI = URI.create("https://www.ua-football.com/ua/rss/all.xml");

    public UaFootball(URI readabilityUri, HttpClient httpClient) {
        super(
                INDEX_URI,
                readabilityUri,
                "UFB",
                Topic.FOOTBALL,
                Region.UA,
                httpClient,
                ofSeconds(10),
                null,
                Set.of("Загальні новини"),
                Set.of("Аудіодумка", "Текстова трансляція")
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
        return extractImageURL(URI.create(post.url()))
                .thenApply(opt -> opt.isEmpty() ? post : post.withImageURL(opt.get()));
    }

    private CompletableFuture<Optional<String>> extractImageURL(URI postURI) {
        return httpClient.sendAsync(buildPageRequest(postURI), BodyHandlers.ofString(charset()))
                .thenApply(response -> {
                    if (response.statusCode() != 200) {
                        logger.error("Failed to fetch article from ua-football, status {}", response.statusCode());
                        return Optional.empty();
                    }
                    String body = response.body();
                    if (body == null || body.isBlank()) {
                        logger.warn("Cannot parse article response from ua-football, body is blank");
                        return Optional.empty();
                    }

                    Document document = Jsoup.parse(response.body(), Parser.htmlParser());
                    return extractImageURLFromDocument(document);
                });
    }

    private HttpRequest buildPageRequest(URI uri) {
        return HttpRequest.newBuilder(uri)
                .GET()
                .header("Content-Type", "text/html")
                .timeout(DEFAULT_TIMEOUT)
                .build();
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
