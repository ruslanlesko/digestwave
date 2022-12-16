package com.leskor.sanitizer.prettifiers.sites;

import java.util.List;
import com.leskor.sanitizer.entities.Paragraph;
import com.leskor.sanitizer.entities.Post;
import com.leskor.sanitizer.prettifiers.Prettifier;
import com.leskor.sanitizer.prettifiers.general.ArticlePrefixTrimmingPrettifier;
import com.leskor.sanitizer.prettifiers.general.ArticlePrefixTrimmingPrettifier.Strategy;

public class SDTimesPrettifier implements Prettifier {
    private final ArticlePrefixTrimmingPrettifier articlePrefixTrimmingPrettifier;

    public SDTimesPrettifier() {
        this.articlePrefixTrimmingPrettifier =
                new ArticlePrefixTrimmingPrettifier("About", Strategy.STARTS_WITH);
    }
    @Override
    public List<Paragraph> parseParagraphs(Post post) {
        return articlePrefixTrimmingPrettifier.parseParagraphs(post).stream()
                .map(p -> new Paragraph(p.content().trim(), p.style()))
                .filter(p -> !p.content().startsWith("NewsWire")
                        && !"-".equals(p.content())
                        && !p.content().startsWith("To learn more, visit the website"))
                .toList();
    }
}
