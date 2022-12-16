package com.leskor.sanitizer.prettifiers.sites;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import com.leskor.sanitizer.entities.Paragraph;
import com.leskor.sanitizer.entities.Post;
import com.leskor.sanitizer.prettifiers.Prettifier;
import com.leskor.sanitizer.prettifiers.general.ArticlePrefixTrimmingPrettifier;
import com.leskor.sanitizer.prettifiers.general.ArticlePrefixTrimmingPrettifier.Strategy;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;

public class TechRepublicPrettifier implements Prettifier {
    private final ArticlePrefixTrimmingPrettifier articlePrefixTrimmingPrettifier;

    public TechRepublicPrettifier() {
        this.articlePrefixTrimmingPrettifier =
                new ArticlePrefixTrimmingPrettifier(
                        Set.of("Subscribe to", "Prices and availability are subject to change"),
                        Strategy.STARTS_WITH
                );
    }

    @Override
    public List<Paragraph> parseParagraphs(Post post) {
        if (post.title().startsWith("Job description:") || post.title().startsWith("Hiring kit:")) {
            return List.of();
        }

        List<Paragraph> result = Jsoup.parse(post.html(), Parser.htmlParser()).getElementsByTag("p")
                .stream()
                .flatMap(this::extractParagraphWithCodeElements)
                .map(this::createParagraph)
                .filter(p -> !(p.content().startsWith("on ") && p.content().length() < 34)
                        && !p.content().startsWith("SEE:")
                        && !p.content().startsWith("Jump to:"))
                .toList();

        if (!result.isEmpty() && result.get(0).content().startsWith("on")) {
            result = result.stream().skip(1).toList();
        }

        return articlePrefixTrimmingPrettifier.trimParagraphs(result);
    }

    private Stream<Element> extractParagraphWithCodeElements(Element p) {
        Element innerCodeElement = p.getElementsByTag("code").first();
        Element nextSibling = p.nextElementSibling();
        Element nextCodeElement = null;
        if (nextSibling != null && !"p".equals(nextSibling.tagName())) {
            nextCodeElement = nextSibling.getElementsByTag("code").first();
        }
        if (innerCodeElement == null && nextCodeElement == null) {
            return Stream.of(p);
        }

        List<Element> elements = new ArrayList<>();
        if (innerCodeElement != null) {
            innerCodeElement.remove();
            elements.add(innerCodeElement);
        }
        elements.add(p);
        if (nextCodeElement != null) {
            elements.add(nextCodeElement);
        }
        return elements.stream();
    }
}
