package com.leskor.scraper.entities;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.leskor.scraper.dto.ReadabilityResponse;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;

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
        String hash,
        @JsonProperty("image_url")
        String imageURL,
        @JsonProperty
        Topic topic
) {
    public static Post from(String siteCode, Topic topic, ZonedDateTime publicationTime, ReadabilityResponse readabilityResponse) {
        Objects.requireNonNull(siteCode, "Post requires site code");
        Objects.requireNonNull(publicationTime, "Post requires publication time");
        Objects.requireNonNull(readabilityResponse, "Post requires readability response");
        Objects.requireNonNull(readabilityResponse.title(), "Post requires a title");
        Objects.requireNonNull(readabilityResponse.textContent(), "Post requires a text content");

        if (readabilityResponse.length() < 500) {
            throw new IllegalArgumentException("Post is too short");
        }

        final String hash = String.valueOf(readabilityResponse.textContent().hashCode());
        final String imageURL = extractImageURL(readabilityResponse.content());

        return new Post(siteCode, publicationTime, readabilityResponse.title(), readabilityResponse.textContent(), hash, imageURL, topic);
    }

    private static String extractImageURL(String content) {
        Document document = Jsoup.parse(content, Parser.xmlParser());
        var images = document.getElementsByTag("img");
        if (images.isEmpty()) {
            return null;
        }

        for (var image : images) {
            if (image.attributes().hasKey("src")) {
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
