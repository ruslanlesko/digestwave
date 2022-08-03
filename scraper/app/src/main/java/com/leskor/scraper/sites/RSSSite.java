package com.leskor.scraper.sites;

import com.leskor.scraper.dto.ReadabilityResponse;
import com.leskor.scraper.entities.Post;
import com.leskor.scraper.entities.Region;
import com.leskor.scraper.entities.Topic;
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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;

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
            Region region,
            HttpClient httpClient,
            Duration indexPageTimeoutDuration,
            String titleSuffixToTrim,
            Set<String> excludedCategories,
            Set<String> excludeIfTitleContains
    ) {
        super(indexPageUri, readabilityUri, siteCode, httpClient, indexPageTimeoutDuration, topic, region, excludeIfTitleContains);
        this.titleSuffixToTrim = titleSuffixToTrim == null ? "" : titleSuffixToTrim;
        this.excludedCategories = excludedCategories == null ? Set.of() : excludedCategories;
    }

    protected List<CompletableFuture<Post>> extractPostsBasedOnPage(String page) {
        return extractPostURIsWithPublicationTimeFromPage(page)
                .stream()
                .filter(Objects::nonNull)
                .map(this::extractPost).toList();
    }

    private List<PostMetadata> extractPostURIsWithPublicationTimeFromPage(String page) {
        Document document = Jsoup.parse(page, Parser.xmlParser());
        var postElements = document.getElementsByTag("item");
        int limit = POSTS_LIMIT;
        List<PostMetadata> result = new ArrayList<>();
        for (var elem : postElements) {
            if (limit <= 0) break;

            Element link = elem.getElementsByTag("link").first();
            if (link == null) continue;

            limit--;

            boolean exclude = elem.getElementsByTag("category").stream()
                    .anyMatch(c -> excludedCategories.stream().anyMatch(e -> c.text().contains(e)));

            if (exclude) continue;

            var titleElement = elem.getElementsByTag("title").first();
            var titleString = "";
            if (titleElement != null) {
                titleString = titleElement.text();
            }

            var dateElement = elem.getElementsByTag("pubDate").first();
            if (dateElement == null) continue;
            var dateString = dateElement.text();

            URI imageURI = null;
            var enclosure = elem.getElementsByTag("enclosure").first();
            if (enclosure != null
                    && enclosure.hasAttr("type")
                    && enclosure.attr("type").startsWith("image")
                    && enclosure.hasAttr("url")) {
                imageURI = URI.create(enclosure.attr("url"));
            }

            try {
                ZonedDateTime publicationTime = ZonedDateTime.parse(dateString, RFC_1123_DATE_TIME);
                result.add(new PostMetadata(URI.create(link.text()), titleString, publicationTime, imageURI));
            } catch (IllegalArgumentException e) {
                logger.warn("Cannot parse URL", e);
            } catch (DateTimeParseException e) {
                logger.warn("Cannot parse publication time", e);
            }
        }
        return result;
    }

    private CompletableFuture<Post> extractPost(PostMetadata metadata) {
        URI uri = metadata.uri();
        ZonedDateTime publicationTime = metadata.publicationTime();
        logger.debug("Reading {}", uri);

        CompletableFuture<ReadabilityResponse> readabilityFuture = retrieveReadabilityResponse(uri)
                .thenApply(r -> {
                    if (r != null && titleSuffixToTrim != null && !titleSuffixToTrim.isBlank()) {
                        String cleanedUpTitle = cleanUpTitle(r.title());
                        return ReadabilityResponse.fromTitleAndExistingResponse(cleanedUpTitle, r);
                    }
                    return r;
                });

        return readabilityFuture.thenApply(readabilityResponse -> {
            if (readabilityResponse == null) {
                return null;
            }
            String title = metadata.title() == null || metadata.title().isBlank() ? readabilityResponse.title()
                    : cleanUpTitle(metadata.title());
            return buildPost(siteCode, topic, region, publicationTime, readabilityResponse, title, uri, metadata.imageURI());
        });
    }

    private String cleanUpTitle(String title) {
        return title.contains(titleSuffixToTrim) ?
                title.substring(0, title.indexOf(titleSuffixToTrim)).strip() : title.strip();
    }

    protected Post buildPost(
            String siteCode,
            Topic topic,
            Region region,
            ZonedDateTime publicationTime,
            ReadabilityResponse readabilityResponse,
            String title,
            URI uri,
            URI imageURI
    ) {
        return Post.from(siteCode, topic, region, publicationTime, readabilityResponse, title, uri, imageURI);
    }

    private record PostMetadata(URI uri, String title, ZonedDateTime publicationTime, URI imageURI) {
    }
}
