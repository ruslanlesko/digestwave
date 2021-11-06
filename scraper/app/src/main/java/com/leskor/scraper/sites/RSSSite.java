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
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;

public class RSSSite extends Site {
    private final String titleSuffixToTrim;
    private final Set<String> excludedCategories;

    public RSSSite(
            URI homePageUri,
            String siteCode,
            HttpClient httpClient,
            Duration homePageTimeoutDuration,
            String titleSuffixToTrim
    ) {
        this(homePageUri, siteCode, httpClient, homePageTimeoutDuration, titleSuffixToTrim, Set.of());
    }

    public RSSSite(
            URI homePageUri,
            String siteCode,
            HttpClient httpClient,
            Duration homePageTimeoutDuration,
            String titleSuffixToTrim,
            Set<String> excludedCategories
    ) {
        super(homePageUri, siteCode, httpClient, homePageTimeoutDuration);
        this.titleSuffixToTrim = titleSuffixToTrim;
        this.excludedCategories = excludedCategories;
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
                    if (titleSuffixToTrim != null && !titleSuffixToTrim.isBlank()) {
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
            return Post.from(siteCode, publicationTime, readabilityResponse);
        });
    }

    private record URIWithPublicationTime(URI uri, ZonedDateTime publicationTime) {
    }
}
