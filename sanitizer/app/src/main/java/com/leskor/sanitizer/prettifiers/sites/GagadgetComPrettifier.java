package com.leskor.sanitizer.prettifiers.sites;

import com.leskor.sanitizer.entities.Post;
import com.leskor.sanitizer.prettifiers.Prettifier;
import com.leskor.sanitizer.prettifiers.general.ArticlePrefixTrimmingPrettifier;
import com.leskor.sanitizer.prettifiers.general.ArticlePrefixTrimmingPrettifier.Strategy;

import java.util.Arrays;
import java.util.List;

public class GagadgetComPrettifier implements Prettifier {
    private final ArticlePrefixTrimmingPrettifier articlePrefixTrimmingPrettifier;

    public GagadgetComPrettifier() {
        articlePrefixTrimmingPrettifier = new ArticlePrefixTrimmingPrettifier("Джерело:", Strategy.STARTS_WITH);
    }

    @Override
    public List<String> parseParagraphs(Post post) {
        List<String> result = Arrays.stream(post.content().split("\n"))
                .filter(p -> !p.toUpperCase().contains("ЗА ПІДТРИМКИ") && !p.toUpperCase().contains("GG"))
                .toList();

        return articlePrefixTrimmingPrettifier.trimParagraphs(result);
    }
}
