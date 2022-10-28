package com.leskor.sanitizer.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record SanitizedPost(
        @JsonProperty("site_code")
        String siteCode,
        @JsonProperty("publication_time")
        long publicationTime,
        @JsonProperty
        String title,
        @JsonIgnore
        List<String> paragraphs,
        @JsonIgnore
        Map<Integer, String> styles,
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
    public static SanitizedPost from(Post post, List<Paragraph> paragraphs) {
        return new SanitizedPost(
                post.siteCode(),
                post.publicationTime(),
                post.title(),
                paragraphs.stream().map(Paragraph::content).toList(),
                generateStyles(paragraphs),
                post.hash(),
                post.url(),
                post.imageURL(),
                post.topic(),
                post.region()
        );
    }

    private static Map<Integer, String> generateStyles(List<Paragraph> paragraphs) {
        Map<Integer, String> result = new HashMap<>();
        for (int i = 0; i < paragraphs.size(); i++) {
            if (!paragraphs.get(i).style().isBlank()) {
                result.put(i, paragraphs.get(i).style());
            }
        }
        return result;
    }

    @JsonProperty("content")
    public String getContent() {
        return String.join("\n", paragraphs);
    }

    @JsonProperty("style")
    public String getStyle() throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(styles);
    }
}
