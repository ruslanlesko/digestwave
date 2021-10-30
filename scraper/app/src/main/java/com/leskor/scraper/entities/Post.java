package com.leskor.scraper.entities;

import com.leskor.scraper.dto.ReadabilityResponse;

import java.time.ZonedDateTime;
import java.util.Objects;

public record Post(String siteCode, ZonedDateTime publicationTime, String title, String content) {
    public static Post from(String siteCode, ZonedDateTime publicationTime, ReadabilityResponse readabilityResponse) {
        Objects.requireNonNull(siteCode, "Post requires site code");
        Objects.requireNonNull(publicationTime, "Post requires publication time");
        Objects.requireNonNull(readabilityResponse, "Post requires readability response");
        Objects.requireNonNull(readabilityResponse.title(), "Post requires a title");
        Objects.requireNonNull(readabilityResponse.textContent(), "Post requires a text content");

        if (readabilityResponse.length() < 500) {
            throw new IllegalArgumentException("Post is too short");
        }

        return new Post(siteCode, publicationTime, readabilityResponse.title(), readabilityResponse.textContent());
    }
}
