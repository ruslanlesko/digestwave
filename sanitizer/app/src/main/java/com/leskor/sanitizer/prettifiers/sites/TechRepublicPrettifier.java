package com.leskor.sanitizer.prettifiers.sites;

import java.util.List;
import com.leskor.sanitizer.entities.Paragraph;
import com.leskor.sanitizer.entities.Post;
import com.leskor.sanitizer.prettifiers.Prettifier;
import com.leskor.sanitizer.prettifiers.general.ArticlePrefixTrimmingPrettifier;
import com.leskor.sanitizer.prettifiers.general.ArticlePrefixTrimmingPrettifier.Strategy;
import org.jsoup.Jsoup;
import org.jsoup.parser.Parser;
import org.jsoup.safety.Safelist;

public class TechRepublicPrettifier implements Prettifier {
    private final ArticlePrefixTrimmingPrettifier articlePrefixTrimmingPrettifier;

    public TechRepublicPrettifier() {
        this.articlePrefixTrimmingPrettifier =
                new ArticlePrefixTrimmingPrettifier("Subscribe to", Strategy.STARTS_WITH);
    }

    @Override
    public List<Paragraph> parseParagraphs(Post post) {
        List<String> result = Jsoup.parse(post.html(), Parser.htmlParser()).getElementsByTag("p")
                .stream()
                .map(p -> Jsoup.clean(p.html(), Safelist.none()).trim())
                .filter(p -> !(p.startsWith("on ") && p.length() < 34)
                        && !p.startsWith("SEE:")
                        && !p.startsWith("Jump to:"))
                .toList();

        return articlePrefixTrimmingPrettifier.trimParagraphs(result)
                .stream()
                .map(p -> new Paragraph(p, ""))
                .toList();
    }
}
