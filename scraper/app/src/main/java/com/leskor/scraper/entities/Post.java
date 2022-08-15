package com.leskor.scraper.entities;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.leskor.scraper.dto.ReadabilityResponse;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;

import java.net.URI;
import java.time.ZonedDateTime;
import java.util.Objects;

public record Post(
        @JsonProperty("site_code")
        String siteCode,
        @JsonIgnore
        ZonedDateTime publicationTime,
        @JsonProperty
        String title,
        @JsonProperty
        String content,
        @JsonProperty
        String html,
        @JsonProperty
        String hash,
        @JsonProperty
        String url,
        @JsonProperty("image_url")
        String imageURL,
        @JsonProperty
        Topic topic,
        @JsonProperty
        Region region
) {
    public static Post from(
            String siteCode,
            Topic topic,
            Region region,
            ZonedDateTime publicationTime,
            ReadabilityResponse readabilityResponse,
            URI uri
    ) {
        return from(siteCode, topic, region, publicationTime, readabilityResponse, readabilityResponse.title(), uri, null);
    }

    public static Post from(
            String siteCode,
            Topic topic,
            Region region,
            ZonedDateTime publicationTime,
            ReadabilityResponse readabilityResponse,
            String title,
            URI uri,
            URI imageURI
    ) {
        Objects.requireNonNull(siteCode, "Post requires site code");
        Objects.requireNonNull(publicationTime, "Post requires publication time");
        Objects.requireNonNull(uri, "Post requires URI");
        Objects.requireNonNull(readabilityResponse, "Post requires readability response");
        Objects.requireNonNull(title, "Post requires a title");
        Objects.requireNonNull(readabilityResponse.textContent(), "Post requires a text content");

        if (readabilityResponse.length() < 500) {
            throw new IllegalArgumentException("Post is too short");
        }

        if (title.length() < 10) {
            throw new IllegalArgumentException("Title is too short");
        }

        final String hash = String.valueOf(Objects.hash(siteCode, title));
        final String imageURL = generateImageURL(readabilityResponse.content(), imageURI);

        return new Post(
                siteCode,
                publicationTime,
                title,
                readabilityResponse.textContent(),
                readabilityResponse.content(),
                hash,
                uri.toString(),
                imageURL,
                topic,
                region
        );
    }

    public static Post from(
            String siteCode,
            Topic topic,
            Region region,
            ZonedDateTime publicationTime,
            String title,
            String content,
            String html,
            URI uri,
            String imageURL
    ) {
        Objects.requireNonNull(siteCode, "Post requires site code");
        Objects.requireNonNull(publicationTime, "Post requires publication time");
        Objects.requireNonNull(uri, "Post requires URI");
        Objects.requireNonNull(title, "Post requires a title");
        Objects.requireNonNull(content, "Post requires a text content");

        if (content.length() < 500) {
            throw new IllegalArgumentException("Post is too short");
        }

        if (title.length() < 10) {
            throw new IllegalArgumentException("Title is too short");
        }

        final String hash = String.valueOf(Objects.hash(siteCode, title));

        return new Post(siteCode, publicationTime, title, content, html, hash, uri.toString(), imageURL, topic, region);
    }

    public Post withImageURL(String newImageURL) {
        return new Post(siteCode, publicationTime, title, content, html, hash, url, newImageURL, topic, region);
    }

    private static String generateImageURL(String postBody, URI providedImageURI) {
        final String extractedImageFromBody = extractImageURL(postBody);
        if (extractedImageFromBody == null || extractedImageFromBody.isBlank()) {
            return providedImageURI == null ? null : providedImageURI.toString();
        }
        return extractedImageFromBody;
    }

    private static String extractImageURL(String content) {
        Element document = Jsoup.parse(content, Parser.xmlParser());

        Element picture = document.getElementsByTag("picture").first();
        if (picture != null) {
            document = picture;
        }

        var images = document.getElementsByTag("img");
        if (images.isEmpty()) {
            return null;
        }

        for (var image : images) {
            if (image.attributes().hasKey("src")) {
                try {
                    if (image.hasAttr("height")
                            && Integer.parseInt(image.attr("height")) < 128
                            || image.hasAttr("width")
                            && Integer.parseInt(image.attr("width")) < 128
                    ) {
                        continue;
                    }
                } catch (NumberFormatException e) {
                    continue;
                }
                return image.attributes().getIgnoreCase("src");
            }
        }

        return null;
    }

    @JsonGetter("publication_time")
    public long getPublicationTimeEpoch() {
        return publicationTime.toEpochSecond();
    }
}
