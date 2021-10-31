package com.leskor.scraper.sites;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leskor.scraper.dto.ReadabilityResponse;
import com.leskor.scraper.entities.Post;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Ain {
    private static final Logger logger = LoggerFactory.getLogger("Application");

    private static final URI HOME_PAGE_URI = URI.create("https://ain.ua");
    private static final String MOZILLA_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:93.0) Gecko/20100101 Firefox/93.0";
    private static final String SITE_CODE = "AIN";

    private final HttpClient httpClient;

    public Ain(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public CompletableFuture<List<Post>> fetchPosts() {
        return httpClient.sendAsync(buildRequest(), BodyHandlers.ofString(StandardCharsets.UTF_8))
                .thenApply(response -> {
                    if (response.statusCode() != 200) {
                        logger.warn("Cannot fetchPosts, status {}", response.statusCode());
                        return List.of();
                    }

                    List<CompletableFuture<Post>> postFutures = extractPostURIsFromPage(response.body())
                            .stream()
                            .filter(Objects::nonNull)
                            .map(this::extractPost).toList();

                    List<Post> result = new ArrayList<>();

                    for (var f : postFutures) {
                        try {
                            Post post = f.get(20, TimeUnit.SECONDS);
                            if (post != null) {
                                result.add(post);
                            }
                        } catch (InterruptedException | ExecutionException | TimeoutException e) {
                            logger.error("Failed to fetch posts", e);
                        }
                    }

                    return result;
                });
    }

    private HttpRequest buildRequest() {
        return HttpRequest.newBuilder(HOME_PAGE_URI)
                .setHeader("User-Agent", MOZILLA_AGENT)
                .timeout(Duration.ofSeconds(10))
                .build();
    }

    private List<URI> extractPostURIsFromPage(String page) {
        Document document = Jsoup.parse(page);
        var elements = document.getElementsByClass("post-item ordinary-post");
        int limit = 10;
        List<URI> result = new ArrayList<>();
        for (var postElement : elements) {
            Element link = postElement.getElementsByClass("post-link").first();
            if (link == null) continue;

            limit--;

            if (link.getElementsByTag("svg").size() > 0) continue;

            try {
                result.add(URI.create(link.attr("href")));
            } catch (IllegalArgumentException e) {
                logger.warn("Cannot parse URL", e);
            }
            if (limit <= 0) break;
        }
        return result;
    }

    private CompletableFuture<Post> extractPost(URI uri) {
        logger.debug("Reading {}", uri);

        // Requests to readability and publication time should be parallel
        CompletableFuture<ReadabilityResponse> readabilityFuture = retrieveReadabilityResponse(uri);
        CompletableFuture<ZonedDateTime> publicationTimeFuture = retrievePublicationTime(uri);

        return readabilityFuture.thenCombine(publicationTimeFuture, (readabilityResponse, publicationTime) -> {
            if (readabilityResponse == null) {
                return null;
            }
            if (publicationTime == null) {
                logger.warn("Cannot extract publication time");
                return null;
            }
            return Post.from(SITE_CODE, publicationTime, readabilityResponse);
        });
    }

    private CompletableFuture<ReadabilityResponse> retrieveReadabilityResponse(URI uri) {
        return httpClient.sendAsync(buildReadabilityRequest(uri), BodyHandlers.ofString(StandardCharsets.UTF_8))
                .thenApply(response -> {
                    if (response.statusCode() != 200) {
                        logger.error("Cannot invoke readability, status {}", response.statusCode());
                        return null;
                    }
                    try {
                        return new ObjectMapper().readValue(response.body(), ReadabilityResponse.class);
                    } catch (JsonProcessingException e) {
                        logger.error("Failed to process readability response");
                        return null;
                    }
                });
    }

    private HttpRequest buildReadabilityRequest(URI uri) {
        return HttpRequest.newBuilder(URI.create("http://localhost:3009")) // Expects this service https://github.com/phpdocker-io/readability-js-server
                .POST(HttpRequest.BodyPublishers.ofString(String.format("{\"url\": \"%S\"}", uri.toString())))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(10))
                .build();
    }

    private CompletableFuture<ZonedDateTime> retrievePublicationTime(URI uri) {
        return httpClient.sendAsync(buildPostRequest(uri), BodyHandlers.ofString(StandardCharsets.UTF_8))
                .thenApply(response -> {
                    if (response.statusCode() != 200) {
                        logger.error("Cannot invoke post, status {}", response.statusCode());
                        return null;
                    }
                    Document document = Jsoup.parse(response.body());
                    Elements propertyElements = document.getElementsByAttributeValue("property", "article:published_time");
                    if (propertyElements.size() > 0 && !propertyElements.get(0).attr("content").equals("")) {
                        try {
                            return ZonedDateTime.parse(propertyElements.get(0).attr("content"));
                        } catch (DateTimeParseException e) {
                            logger.warn("Cannot parse publication time", e);
                        }
                    }
                    return null;
                });
    }

    private HttpRequest buildPostRequest(URI uri) {
        return HttpRequest.newBuilder(uri)
                .header("User-Agent", MOZILLA_AGENT)
                .timeout(Duration.ofSeconds(10))
                .build();
    }
}
