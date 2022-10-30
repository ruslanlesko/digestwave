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

public class RootNationUaPrettifier implements Prettifier {
    private final ArticlePrefixTrimmingPrettifier articlePrefixTrimmingPrettifier;

    public RootNationUaPrettifier() {
        articlePrefixTrimmingPrettifier = new ArticlePrefixTrimmingPrettifier("Також цікаво:", Strategy.STARTS_WITH);
    }

    @Override
    public List<Paragraph> parseParagraphs(Post post) {
        Element wrapper = Jsoup.parse(post.html(), Parser.htmlParser())
                .getElementsByAttribute("data-td-block-uid").first();

        if (wrapper == null) {
            return List.of();
        }

        List<Paragraph> result = wrapper.children().stream()
                .filter(e -> "p".equals(e.tagName()) && e.getElementsByTag("strong").isEmpty())
                .map(Element::html)
                .filter(html -> !html.contains(post.title()))
                .map(html -> Jsoup.clean(html, Safelist.none()))
                .filter(p -> !p.startsWith("Читайте також:")
                        && !p.startsWith("Ціни в магазинах")
                        && !p.startsWith("Теж цікаво:")
                )
                .map(p -> new Paragraph(p, ""))
                .toList();

        return articlePrefixTrimmingPrettifier.trimParagraphs(result);
    }
}
