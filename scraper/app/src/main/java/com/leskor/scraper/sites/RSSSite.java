package com.leskor.scraper.sites;

import com.leskor.scraper.dto.ReadabilityResponse;
import com.leskor.scraper.entities.Post;
import com.leskor.scraper.entities.Topic;
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
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;

public class RSSSite extends Site {
    private static final int POSTS_LIMIT = 10;

    private final String titleSuffixToTrim;
    private final Set<String> excludedCategories;

    public RSSSite(
            URI indexPageUri,
            URI readabilityUri,
            String siteCode,
            Topic topic,
            HttpClient httpClient,
            Duration indexPageTimeoutDuration,
            String titleSuffixToTrim,
            Set<String> excludedCategories
    ) {
        super(indexPageUri, readabilityUri, siteCode, httpClient, indexPageTimeoutDuration, topic);
        this.titleSuffixToTrim = titleSuffixToTrim == null ? "" : titleSuffixToTrim;
        this.excludedCategories = excludedCategories == null ? Set.of() : excludedCategories;
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
        int limit = POSTS_LIMIT;
        List<URIWithPublicationTime> result = new ArrayList<>();
        for (var elem : postElements) {
            if (limit <= 0) break;

            Element link = elem.getElementsByTag("link").first();
            if (link == null) continue;

            limit--;

            boolean exclude = elem.getElementsByTag("category").stream()
                    .anyMatch(c -> excludedCategories.stream().anyMatch(e -> c.text().contains(e)));

            if (exclude) continue;

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
                    if (r != null && titleSuffixToTrim != null && !titleSuffixToTrim.isBlank()) {
                        String cleanedUpTitle = r.title().contains(titleSuffixToTrim) ?
                                r.title().substring(0, r.title().indexOf(titleSuffixToTrim)).strip() : r.title().strip();
                        return ReadabilityResponse.fromTitleAndExistingResponse(cleanedUpTitle, r);
                    }
                    return r;
                });

        return readabilityFuture.thenApply(readabilityResponse -> {
            if (readabilityResponse == null) {
                return null;
            }
            return buildPost(siteCode, topic, publicationTime, readabilityResponse);
        });
    }

    protected Post buildPost(String siteCode, Topic topic, ZonedDateTime publicationTime, ReadabilityResponse readabilityResponse) {
        return Post.from(siteCode, topic, publicationTime, readabilityResponse);
    }

    private record URIWithPublicationTime(URI uri, ZonedDateTime publicationTime) {
    }
}
