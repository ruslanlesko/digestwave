package com.leskor.sanitizer.prettifiers.sites;

import java.util.List;
import com.leskor.sanitizer.entities.Paragraph;
import com.leskor.sanitizer.entities.Post;
import com.leskor.sanitizer.prettifiers.Prettifier;
import com.leskor.sanitizer.prettifiers.general.ArticlePrefixTrimmingPrettifier;

public class KeddrComPrettifier implements Prettifier {
    private final ArticlePrefixTrimmingPrettifier articlePrefixTrimmingPrettifier;

    public KeddrComPrettifier() {
        articlePrefixTrimmingPrettifier = new ArticlePrefixTrimmingPrettifier("Ctrl+Enter");
    }

    @Override
    public List<Paragraph> parseParagraphs(Post post) {
        return articlePrefixTrimmingPrettifier.parseParagraphs(post);
    }
}
