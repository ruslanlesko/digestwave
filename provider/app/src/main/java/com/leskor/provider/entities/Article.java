package com.leskor.provider.entities;

import java.util.Objects;
import java.util.function.Function;

public record Article(
        String id,
        String title,
        String content,
        long publicationTime,
        String site,
        String topic,
        boolean hasCoverImage
) {
    public static Article from(Post post, Function<String, String> siteCodeResolverFunction) {
        Objects.requireNonNull(post, post.getHash());
        final String hash = post.getHash();
        String id = hash.startsWith("-") ? "n" + hash.substring(1) : "p" + hash;
        String site = siteCodeResolverFunction.apply(post.getSiteCode());
        boolean hasCoverImage = post.getImageURL() != null && !post.getImageURL().isBlank();
        return new Article(id, post.getTitle(), post.getContent(), post.getPublicationTime(), site, post.getTopic(), hasCoverImage);
    }
}
