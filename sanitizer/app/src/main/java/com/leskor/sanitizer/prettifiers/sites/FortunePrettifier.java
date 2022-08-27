package com.leskor.sanitizer.prettifiers.sites;

import java.util.List;
import com.leskor.sanitizer.entities.Post;
import com.leskor.sanitizer.prettifiers.Prettifier;
import com.leskor.sanitizer.prettifiers.general.ArticlePrefixTrimmingPrettifier;
import com.leskor.sanitizer.prettifiers.general.ArticlePrefixTrimmingPrettifier.Strategy;

public class FortunePrettifier implements Prettifier {
    private final ArticlePrefixTrimmingPrettifier articlePrefixTrimmingPrettifier;

    public FortunePrettifier() {
        articlePrefixTrimmingPrettifier = new ArticlePrefixTrimmingPrettifier("Sign up", Strategy.STARTS_WITH);
    }

    @Override
    public List<String> parseParagraphs(Post post) {
        return articlePrefixTrimmingPrettifier.parseParagraphs(post);
    }
}
