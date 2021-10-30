package com.leskor.scraper.sites;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leskor.scraper.dto.ReadabilityResponse;
import com.leskor.scraper.entities.Post;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Ain {
    private static final Logger logger = LoggerFactory.getLogger("Application");

    private static final URI HOME_PAGE_URI = URI.create("https://ain.ua");
    private static final String MOZILLA_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:93.0) Gecko/20100101 Firefox/93.0";
    private static final String SITE_CODE = "AIN";

    private final HttpClient httpClient;

    public Ain(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public List<Post> fetchPosts() {
        HttpRequest request = buildRequest();
        try {
            HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() != 200) {
                logger.warn("Cannot fetchPosts, status {}", response.statusCode());
                return List.of();
            }

            return extractPostURIsFromPage(response.body()).stream()
                    .filter(Objects::nonNull)
                    .map(this::extractPost)
                    .filter(Objects::nonNull)
                    .toList();
        } catch (IOException e) {
            logger.error("IO failed", e);
        } catch (InterruptedException e) {
            logger.error("Interrupted Exception", e);
        }
        return List.of();
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

    private Post extractPost(URI uri) {
        logger.debug("Reading {}", uri);
        HttpRequest request = buildReadabilityRequest(uri);
        try {
            var response = httpClient.send(request, BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() != 200) {
                logger.error("Cannot invoke readability, status {}", response.statusCode());
                return null;
            }
            String body = response.body();
            var readabilityResponse = new ObjectMapper().readValue(body, ReadabilityResponse.class);
            request = buildPostRequest(uri);
            response = httpClient.send(request, BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() != 200) {
                logger.error("Cannot invoke post, status {}", response.statusCode());
                return null;
            }
            ZonedDateTime publicationTime = extractPublicationTime(response.body());
            if (publicationTime == null) {
                logger.warn("Cannot extract publication time");
                return null;
            }
            return Post.from(SITE_CODE, publicationTime, readabilityResponse);
        } catch (IOException e) {
            logger.error("Failed to invoke readability", e);
        } catch (InterruptedException e) {
            logger.error("Interrupted Exception", e);
        }
        return null;
    }

    private HttpRequest buildReadabilityRequest(URI uri) {
        return HttpRequest.newBuilder(URI.create("http://localhost:3009")) // Expects this service https://github.com/phpdocker-io/readability-js-server
                .POST(HttpRequest.BodyPublishers.ofString(String.format("{\"url\": \"%S\"}", uri.toString())))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(10))
                .build();
    }

    private HttpRequest buildPostRequest(URI uri) {
        return HttpRequest.newBuilder(uri)
                .header("User-Agent", MOZILLA_AGENT)
                .timeout(Duration.ofSeconds(10))
                .build();
    }

    private ZonedDateTime extractPublicationTime(String body) {
        Document document = Jsoup.parse(body);
        Elements propertyElements = document.getElementsByAttributeValue("property", "article:published_time");
        if (propertyElements.size() > 0 && !propertyElements.get(0).attr("content").equals("")) {
            try {
                return ZonedDateTime.parse(propertyElements.get(0).attr("content"));
            } catch (DateTimeParseException e) {
                logger.warn("Cannot parse publication time", e);
            }
        }
        return null;
    }
}
