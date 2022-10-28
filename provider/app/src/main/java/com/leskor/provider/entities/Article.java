package com.leskor.provider.entities;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public record Article(
        String id,
        String title,
        List<String> content,
        Map<Integer, String> styles,
        long publicationTime,
        String site,
        String topic,
        String url,
        boolean hasCoverImage
) {
    public static Article from(Post post, UnaryOperator<String> siteCodeResolverFunction) {
        Objects.requireNonNull(post, post.getHash());
        final String hash = post.getHash();
        String id = hash.startsWith("-") ? "n" + hash.substring(1) : "p" + hash;
        String site = siteCodeResolverFunction.apply(post.getSiteCode());
        boolean hasCoverImage = post.getImageURL() != null && !post.getImageURL().isBlank();
        List<String> content = Arrays.stream(post.getContent().split("\n"))
                .map(p -> p.replaceAll("<:<newline>:>", "\n"))
                .toList();
        Map<String, String> styles = null;
        try {
            styles = new ObjectMapper().readValue(post.getStyle(), Map.class);
        } catch (JsonProcessingException e) {
            System.out.println("Failed to parse styles: " + post.getStyle());
        }

        return new Article(
                id,
                post.getTitle(),
                content,
                convertStyles(styles),
                post.getPublicationTime(),
                site,
                post.getTopic(),
                post.getUrl(),
                hasCoverImage
        );
    }

    private static Map<Integer, String> convertStyles(Map<String, String> styles) {
        if (styles == null) return Map.of();
        Map<Integer, String> result = new HashMap<>();
        for (var entry : styles.entrySet()) {
            result.put(Integer.valueOf(entry.getKey()), entry.getValue());
        }
        return result;
    }
}
