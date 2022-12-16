package com.leskor.sanitizer.prettifiers.sites;

import java.util.List;
import com.leskor.sanitizer.entities.Paragraph;
import com.leskor.sanitizer.entities.Post;
import com.leskor.sanitizer.prettifiers.Prettifier;
import com.leskor.sanitizer.prettifiers.general.ArticlePrefixTrimmingPrettifier;
import com.leskor.sanitizer.prettifiers.general.ArticlePrefixTrimmingPrettifier.Strategy;

public class SportarenaPrettifier implements Prettifier {
    private final ArticlePrefixTrimmingPrettifier articlePrefixTrimmingPrettifier;

    public SportarenaPrettifier() {
        articlePrefixTrimmingPrettifier = new ArticlePrefixTrimmingPrettifier("Російський", Strategy.STARTS_WITH);
    }

    @Override
    public List<Paragraph> parseParagraphs(Post post) {
        return articlePrefixTrimmingPrettifier.parseParagraphs(post).stream()
                .map(p -> new Paragraph(p.content().trim(), p.style()))
                .filter(p -> !p.content().startsWith("Фото -") && !p.content().startsWith("Читайте також:"))
                .toList();
    }
}
