package com.leskor.sanitizer.prettifiers.sites;

import com.leskor.sanitizer.entities.Paragraph;
import com.leskor.sanitizer.entities.Post;
import com.leskor.sanitizer.prettifiers.Prettifier;
import com.leskor.sanitizer.prettifiers.general.ArticlePrefixTrimmingPrettifier;
import com.leskor.sanitizer.prettifiers.general.ArticlePrefixTrimmingPrettifier.Strategy;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.safety.Safelist;

import java.util.List;
import java.util.Set;

public class ItcUaPrettifier implements Prettifier {
    private final ArticlePrefixTrimmingPrettifier articlePrefixTrimmingPrettifier;

    public ItcUaPrettifier() {
        articlePrefixTrimmingPrettifier = new ArticlePrefixTrimmingPrettifier(Set.of("Джерело:", "Джерела:"), Strategy.STARTS_WITH);
    }

    @Override
    public List<Paragraph> parseParagraphs(Post post) {
        Element wrapper = Jsoup.parse(post.html(), Parser.htmlParser())
                .getElementsByAttribute("data-io-article-url").first();

        if (wrapper == null) {
            return List.of();
        }

        List<String> result = wrapper.children().stream()
                .filter(e -> "p".equals(e.tagName()))
                .map(Element::html)
                .filter(html -> !html.contains(post.title()))
                .map(html -> Jsoup.clean(html, Safelist.none()))
                .toList();

        return articlePrefixTrimmingPrettifier.trimParagraphs(result)
                .stream()
                .map(p -> new Paragraph(p, ""))
                .toList();
    }
}
