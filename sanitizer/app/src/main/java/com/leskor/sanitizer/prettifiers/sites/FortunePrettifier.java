package com.leskor.sanitizer.prettifiers.sites;

import java.util.List;
import java.util.Set;
import com.leskor.sanitizer.entities.Paragraph;
import com.leskor.sanitizer.entities.Post;
import com.leskor.sanitizer.prettifiers.Prettifier;
import com.leskor.sanitizer.prettifiers.general.ArticlePrefixTrimmingPrettifier;
import com.leskor.sanitizer.prettifiers.general.ArticlePrefixTrimmingPrettifier.Strategy;

public class FortunePrettifier implements Prettifier {
    private final ArticlePrefixTrimmingPrettifier articlePrefixTrimmingPrettifier;

    public FortunePrettifier() {
        articlePrefixTrimmingPrettifier = new ArticlePrefixTrimmingPrettifier(
                Set.of("Sign up", "Follow Fortune"),
                Strategy.STARTS_WITH
        );
    }

    @Override
    public List<Paragraph> parseParagraphs(Post post) {
        return articlePrefixTrimmingPrettifier.parseParagraphs(post).stream()
                .filter(p -> !p.content().contains("Subscribe here"))
                .toList();
    }
}
