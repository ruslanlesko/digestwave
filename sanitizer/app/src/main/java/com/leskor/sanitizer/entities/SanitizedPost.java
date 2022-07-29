package com.leskor.sanitizer.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record SanitizedPost(
        @JsonProperty("site_code")
        String siteCode,
        @JsonProperty("publication_time")
        long publicationTime,
        @JsonProperty
        String title,
        @JsonIgnore
        List<String> paragraphs,
        @JsonProperty
        String hash,
        @JsonProperty
        String url,
        @JsonProperty("image_url")
        String imageURL,
        @JsonProperty
        String topic,
        @JsonProperty
        String region
) {
        public static SanitizedPost from(Post post, List<String> paragraphs) {
                return new SanitizedPost(
                        post.siteCode(),
                        post.publicationTime(),
                        post.title(),
                        paragraphs,
                        post.hash(),
                        post.url(),
                        post.imageURL(),
                        post.topic(),
                        post.region()
                );
        }

        @JsonProperty("content")
        public String getContent() {
                return String.join("\n", paragraphs);
        }
}
