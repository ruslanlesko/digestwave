package com.leskor.scraper.sites;

import com.leskor.scraper.dto.ReadabilityResponse;
import com.leskor.scraper.entities.Post;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;

import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;

public class Keddr extends Site {
    public Keddr(HttpClient httpClient) {
        super(URI.create("https://keddr.com/feed/"), "KDR", httpClient, Duration.ofMinutes(1));
    }

    protected List<CompletableFuture<Post>> extractPostsBasedOnPage(String page) {
        return extractPostURIsWithPublicationTimeFromPage(page)
                .stream()
                .filter(Objects::nonNull)
                .map(uriWithTime -> extractPost(uriWithTime.uri, uriWithTime.publicationTime)).toList();
    }

    private List<URIWithPublicationTime> extractPostURIsWithPublicationTimeFromPage(String page) {
        Document document = Jsoup.parse(page, Parser.xmlParser());
        var postElements = document.getElementsByTag("item");
        int limit = 12;
        List<URIWithPublicationTime> result = new ArrayList<>();
        for (var elem : postElements) {
            if (limit <= 0) break;

            Element link = elem.getElementsByTag("link").first();
            if (link == null) continue;

            limit--;

            boolean isPodcast = elem.getElementsByTag("category").stream().anyMatch(e -> e.text().contains("Подкаст"));

            if (isPodcast) continue;

            var dateElement = elem.getElementsByTag("pubDate").first();
            if (dateElement == null) continue;
            var dateString = dateElement.text();

            try {
                ZonedDateTime publicationTime = ZonedDateTime.parse(dateString, RFC_1123_DATE_TIME);
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
            return Post.from(siteCode, publicationTime, readabilityResponse);
        });
    }

    private record URIWithPublicationTime(URI uri, ZonedDateTime publicationTime) {
    }
}
