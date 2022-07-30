package com.leskor.provider.entities;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public record Article(
        String id,
        String title,
        List<String> content,
        long publicationTime,
        String site,
        String topic,
        String url,
        boolean hasCoverImage
) {
    public static Article from(Post post, Function<String, String> siteCodeResolverFunction) {
        Objects.requireNonNull(post, post.getHash());
        final String hash = post.getHash();
        String id = hash.startsWith("-") ? "n" + hash.substring(1) : "p" + hash;
        String site = siteCodeResolverFunction.apply(post.getSiteCode());
        boolean hasCoverImage = post.getImageURL() != null && !post.getImageURL().isBlank();
        List<String> content = Arrays.asList(post.getContent().split("\n"));
        return new Article(id, post.getTitle(), content, post.getPublicationTime(), site, post.getTopic(), post.getUrl(), hasCoverImage);
    }
}
