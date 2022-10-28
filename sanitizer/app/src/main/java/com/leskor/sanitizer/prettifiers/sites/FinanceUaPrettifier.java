package com.leskor.sanitizer.prettifiers.sites;

import com.leskor.sanitizer.entities.Paragraph;
import com.leskor.sanitizer.entities.Post;
import com.leskor.sanitizer.prettifiers.Prettifier;
import com.leskor.sanitizer.prettifiers.general.ArticlePrefixTrimmingPrettifier;

import java.util.List;

public class FinanceUaPrettifier implements Prettifier {
    private final ArticlePrefixTrimmingPrettifier articlePrefixTrimmingPrettifier;

    public FinanceUaPrettifier() {
        articlePrefixTrimmingPrettifier = new ArticlePrefixTrimmingPrettifier("Ctrl+Enter");
    }

    @Override
    public List<Paragraph> parseParagraphs(Post post) {
        List<Paragraph> result = articlePrefixTrimmingPrettifier.parseParagraphs(post);
        if (result.isEmpty()) {
            return result;
        }

        String firstParagraph = result.get(0).content();
        if (firstParagraph.contains(post.title())) {
            result = result.stream().skip(1).toList();
        }

        if (result.get(result.size() - 1).content().length() < 42) {
            return result.stream().limit(result.size() - 2).toList();
        }

        return result;
    }
}
