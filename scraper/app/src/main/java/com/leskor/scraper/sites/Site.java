package com.leskor.scraper.sites;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leskor.scraper.dto.ReadabilityResponse;
import com.leskor.scraper.entities.Post;
import com.leskor.scraper.entities.Region;
import com.leskor.scraper.entities.Topic;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.nio.charset.StandardCharsets.UTF_8;

public abstract class Site {
    protected static final Logger logger = LoggerFactory.getLogger("Application");

    protected static final String MOZILLA_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:93.0) Gecko/20100101 Firefox/93.0";
    protected static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(20);

    protected final URI indexPageUri;
    protected final URI readabilityUri;
    protected final String siteCode;
    protected final HttpClient httpClient;
    protected final Duration indexPageTimeoutDuration;
    protected final Topic topic;
    protected final Region region;
    protected final Set<String> excludeIfTitleContains;

    protected Site(
            URI indexPageUri,
            URI readabilityUri,
            String siteCode,
            HttpClient httpClient,
            Duration indexPageTimeoutDuration,
            Topic topic,
            Region region,
            Set<String> excludeIfTitleContains
    ) {
        this.indexPageUri = indexPageUri;
        this.readabilityUri = readabilityUri;
        this.siteCode = siteCode;
        this.httpClient = httpClient;
        this.indexPageTimeoutDuration = indexPageTimeoutDuration;
        this.topic = topic;
        this.region = region;
        this.excludeIfTitleContains = excludeIfTitleContains == null ? Set.of() : excludeIfTitleContains;
    }

    public final CompletableFuture<List<Post>> fetchPosts() {
        return httpClient.sendAsync(buildIndexPageRequest(indexPageTimeoutDuration), BodyHandlers.ofString(charset()))
                .thenApply(response -> {
                    if (response.statusCode() != 200) {
                        logger.warn("Cannot fetchPosts, status {}", response.statusCode());
                        return List.of();
                    }
                    if (response.body() == null || response.body().isBlank()) {
                        logger.warn("Cannot fetchPosts, body is blank");
                        return List.of();
                    }
                    return waitForPostFutures(extractPostsBasedOnPage(response.body()));
                });
    }

    protected Charset charset() {
        return UTF_8;
    }

    protected abstract List<CompletableFuture<Post>> extractPostsBasedOnPage(String page);

    protected final List<Post> waitForPostFutures(List<CompletableFuture<Post>> postFutures) {
        List<Post> result = new ArrayList<>();

        for (var f : postFutures) {
            try {
                Post post = f.get(DEFAULT_TIMEOUT.getSeconds() * 2, TimeUnit.SECONDS);
                if (post != null && !isPostContainExcludedTitleString(post)) {
                    result.add(post);
                }
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                logger.error("Failed to fetch posts", e);
            }
        }

        return result;
    }

    protected final CompletableFuture<ReadabilityResponse> retrieveReadabilityResponse(URI uri) {
        return retrieveReadabilityResponse(uri, 5);
    }

    private CompletableFuture<ReadabilityResponse> retrieveReadabilityResponse(URI uri, int attemptsLeft) {
        return httpClient.sendAsync(buildReadabilityRequest(uri), BodyHandlers.ofString(charset()))
                .thenCompose(response -> {
                    if (response.statusCode() == 500) {
                        return CompletableFuture.failedFuture(new IOException("Readability server error"));
                    }
                    return CompletableFuture.completedFuture(response);
                })
                .thenApply(response -> {
                    if (response.statusCode() != 200) {
                        logger.error("Cannot invoke readability, status {}", response.statusCode());
                        return null;
                    }
                    if (response.body() == null || response.body().isBlank()) {
                        logger.warn("Cannot parse readability response, body is blank");
                        return null;
                    }
                    try {
                        return new ObjectMapper().readValue(response.body(), ReadabilityResponse.class);
                    } catch (JsonProcessingException e) {
                        logger.error("Failed to process readability response");
                        return null;
                    }
                })
                .exceptionallyCompose(e -> {
                    if (attemptsLeft > 0) {
                        logger.warn("Failed to invoke readability for uri {}, retrying...", uri);
                        return retrieveReadabilityResponse(uri, attemptsLeft - 1);
                    }
                    logger.error("Readability cannot process uri {}", uri);
                    return CompletableFuture.completedFuture(null);
                });
    }

    protected final HttpRequest buildIndexPageRequest(Duration timeout) {
        return HttpRequest.newBuilder(indexPageUri)
                .setHeader("User-Agent", MOZILLA_AGENT)
                .timeout(timeout)
                .build();
    }

    private HttpRequest buildReadabilityRequest(URI uri) {
        return HttpRequest.newBuilder(readabilityUri)
                .POST(HttpRequest.BodyPublishers.ofString(String.format("{\"url\": \"%s\"}", uri.toString())))
                .header("Content-Type", "application/json")
                .timeout(DEFAULT_TIMEOUT)
                .build();
    }

    private boolean isPostContainExcludedTitleString(Post post) {
        return excludeIfTitleContains.stream()
                .map(String::toUpperCase)
                .anyMatch(s -> post.title().toUpperCase().contains(s));
    }
}
