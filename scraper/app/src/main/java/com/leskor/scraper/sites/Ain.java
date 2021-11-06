package com.leskor.scraper.sites;

import com.leskor.scraper.dto.ReadabilityResponse;
import com.leskor.scraper.entities.Post;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class Ain extends Site {
    public Ain(HttpClient httpClient) {
        super(URI.create("https://ain.ua"), "AIN", httpClient);
    }

    protected List<CompletableFuture<Post>> extractPostsBasedOnPage(String page) {
        return extractPostURIsFromPage(page)
                .stream()
                .filter(Objects::nonNull)
                .map(this::extractPost).toList();
    }

    private List<URI> extractPostURIsFromPage(String page) {
        Document document = Jsoup.parse(page);
        var elements = document.getElementsByClass("post-item ordinary-post");
        int limit = 10;
        List<URI> result = new ArrayList<>();
        for (var postElement : elements) {
            if (limit <= 0) break;
            Element link = postElement.getElementsByClass("post-link").first();
            if (link == null) continue;

            limit--;

            if (link.getElementsByTag("svg").size() > 0) continue;

            try {
                result.add(URI.create(link.attr("href")));
            } catch (IllegalArgumentException e) {
                logger.warn("Cannot parse URL", e);
            }
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
            return Post.from(siteCode, publicationTime, readabilityResponse);
        });
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
}
