package com.leskor.digestwave.model;

import java.util.List;

public record MentionPayload(
        String publishedAt,
        int count,
        List<Instance> instances
) {
    public record Instance(String articleUrl, Sentiment sentiment) {}
}
