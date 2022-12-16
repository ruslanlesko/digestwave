package com.leskor.sanitizer.prettifiers.sites;

import java.util.List;
import java.util.Set;
import com.leskor.sanitizer.entities.Paragraph;
import com.leskor.sanitizer.entities.Post;
import com.leskor.sanitizer.prettifiers.Prettifier;
import com.leskor.sanitizer.prettifiers.general.ArticlePrefixTrimmingPrettifier;
import com.leskor.sanitizer.prettifiers.general.ArticlePrefixTrimmingPrettifier.Strategy;

public class MoneyMorningPrettifier implements Prettifier {
    private final ArticlePrefixTrimmingPrettifier articlePrefixTrimmingPrettifier;

    public MoneyMorningPrettifier() {
        this.articlePrefixTrimmingPrettifier =
                new ArticlePrefixTrimmingPrettifier(
                        Set.of("Follow Money", "Today's Momentum", "Get the latest trading and investing recommendations"),
                        Strategy.STARTS_WITH
                );
    }

    @Override
    public List<Paragraph> parseParagraphs(Post post) {
        return articlePrefixTrimmingPrettifier.parseParagraphs(post);
    }
}
