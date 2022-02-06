package com.leskor.scraper.entities;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.leskor.scraper.dto.ReadabilityResponse;

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
        String hash
) {
    public static Post from(String siteCode, ZonedDateTime publicationTime, ReadabilityResponse readabilityResponse) {
        Objects.requireNonNull(siteCode, "Post requires site code");
        Objects.requireNonNull(publicationTime, "Post requires publication time");
        Objects.requireNonNull(readabilityResponse, "Post requires readability response");
        Objects.requireNonNull(readabilityResponse.title(), "Post requires a title");
        Objects.requireNonNull(readabilityResponse.textContent(), "Post requires a text content");

        if (readabilityResponse.length() < 500) {
            throw new IllegalArgumentException("Post is too short");
        }

        final String hash = String.valueOf(readabilityResponse.textContent().hashCode());

        return new Post(siteCode, publicationTime, readabilityResponse.title(), readabilityResponse.textContent(), hash);
    }

    @JsonGetter("publication_time")
    public long getPublicationTimeEpoch() {
        return publicationTime.toEpochSecond();
    }
}
