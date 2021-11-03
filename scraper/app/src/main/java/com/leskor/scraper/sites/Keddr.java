package com.leskor.scraper.sites;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leskor.scraper.dto.ReadabilityResponse;
import com.leskor.scraper.entities.Post;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Keddr {
    private static final Logger logger = LoggerFactory.getLogger("Application");

    private static final URI HOME_PAGE_URI = URI.create("https://keddr.com/feed/");
    private static final String MOZILLA_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:93.0) Gecko/20100101 Firefox/93.0";
    private static final String SITE_CODE = "KDR";

    private final HttpClient httpClient;

    public Keddr(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public CompletableFuture<List<Post>> fetchPosts() {
        return httpClient.sendAsync(buildRequest(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
                .thenApply(response -> {
                    if (response.statusCode() != 200) {
                        logger.warn("Cannot fetchPosts, status {}", response.statusCode());
                        return List.of();
                    }

                    var postFutures = extractPostURIsWithPublicationTimeFromPage(response.body())
                            .stream()
                            .filter(Objects::nonNull)
                            .map(uriWithTime -> extractPost(uriWithTime.uri, uriWithTime.publicationTime)).toList();

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
                .timeout(Duration.ofMinutes(1))
                .build();
    }

    private List<URIWithPublicationTime> extractPostURIsWithPublicationTimeFromPage(String page) {
        Document document = Jsoup.parse(page, Parser.xmlParser());
        var items = document.getElementsByTag("item");
        int limit = 12;
        List<URIWithPublicationTime> result = new ArrayList<>();
        for (var item : items) {
            if (limit <= 0) break;

            Element link = item.getElementsByTag("link").first();
            if (link == null) continue;

            limit--;

            boolean isPodcast = item.getElementsByTag("category").stream().anyMatch(e -> e.text().contains("Подкаст"));

            if (isPodcast) continue;

            var dateElement = item.getElementsByTag("pubDate").first();
            if (dateElement == null) continue;
            var dateString = dateElement.text();

            try {
                ZonedDateTime publicationTime = ZonedDateTime.parse(dateString, DateTimeFormatter.RFC_1123_DATE_TIME);
                result.add(new URIWithPublicationTime(URI.create(link.text()), publicationTime));
            } catch (IllegalArgumentException e) {
                logger.warn("Cannot parse URL", e);
            } catch (DateTimeParseException e) {
                logger.warn("Cannot parse publication time", e);
            }
        }
        return result;
    }

    private CompletableFuture<Post> extractPost(URI uri, ZonedDateTime publicationTime) {
        logger.debug("Reading {}", uri);

        CompletableFuture<ReadabilityResponse> readabilityFuture = retrieveReadabilityResponse(uri)
                .thenApply(r -> {
                    String cleanedUpTitle = r.title().contains("|") ?
                            r.title().substring(0, r.title().indexOf("|")).strip() : r.title().strip();
                    return ReadabilityResponse.fromTitleAndExistingResponse(cleanedUpTitle, r);
                });

        return readabilityFuture.thenApply(readabilityResponse -> {
            if (readabilityResponse == null) {
                return null;
            }
            return Post.from(SITE_CODE, publicationTime, readabilityResponse);
        });
    }


    private CompletableFuture<ReadabilityResponse> retrieveReadabilityResponse(URI uri) {
        return httpClient.sendAsync(buildReadabilityRequest(uri), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
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

    private record URIWithPublicationTime(URI uri, ZonedDateTime publicationTime) {
    }
}
