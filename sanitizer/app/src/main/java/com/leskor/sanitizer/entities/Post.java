package com.leskor.sanitizer.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Post(
        @JsonProperty("site_code")
        String siteCode,
        @JsonProperty("publication_time")
        long publicationTime,
        @JsonProperty
        String title,
        @JsonProperty
        String content,
        @JsonProperty
        String hash,
        @JsonProperty
        String url,
        @JsonProperty("image_url")
        String imageURL,
        @JsonProperty
        String topic
) {
}
