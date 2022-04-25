package com.leskor.provider.entities;

public record ArticlePreview(
        String id,
        String title,
        String site,
        boolean hasCoverImage
) {
    public static ArticlePreview from(Article article) {
        return new ArticlePreview(article.id(), article.title(), article.site(), article.hasCoverImage());
    }
}
