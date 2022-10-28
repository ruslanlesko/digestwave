package com.leskor.sanitizer.prettifiers.sites;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import com.leskor.sanitizer.entities.Paragraph;
import com.leskor.sanitizer.entities.Post;
import com.leskor.sanitizer.prettifiers.Prettifier;
import com.leskor.sanitizer.prettifiers.general.ArticlePrefixTrimmingPrettifier;
import com.leskor.sanitizer.prettifiers.general.ArticlePrefixTrimmingPrettifier.Strategy;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.safety.Safelist;

public class EconomistPrettifier implements Prettifier {
    private final ArticlePrefixTrimmingPrettifier articlePrefixTrimmingPrettifier;

    public EconomistPrettifier() {
        this.articlePrefixTrimmingPrettifier =
                new ArticlePrefixTrimmingPrettifier(Set.of("For more", "Read more"), Strategy.STARTS_WITH);
    }

    @Override
    public List<Paragraph> parseParagraphs(Post post) {
        Element outerWrapper = Jsoup.parse(post.html(), Parser.htmlParser()).getElementsByTag("div").first();

        if (outerWrapper == null) {
            return List.of();
        }

        Optional<Element> middleWrapper = outerWrapper.children().stream()
                .filter(e -> "div".equals(e.tagName()))
                .findFirst();

        if (middleWrapper.isEmpty()) {
            return List.of();
        }

        if (middleWrapper.get().children().stream().filter(e -> "p".equals(e.tagName())).count() < 3) {
            middleWrapper = middleWrapper.get().children().stream()
                    .filter(e -> "div".equals(e.tagName()))
                    .findFirst();
        }

        if (middleWrapper.isEmpty()) {
            return List.of();
        }

        List<String> result = middleWrapper.get().children().stream()
                .filter(e -> "p".equals(e.tagName()))
                .map(p -> Jsoup.clean(p.html(), Safelist.none()).replaceAll("■", "").trim())
                .toList();

        return articlePrefixTrimmingPrettifier.trimParagraphs(result)
                .stream()
                .map(p -> new Paragraph(p, ""))
                .toList();
    }
}
