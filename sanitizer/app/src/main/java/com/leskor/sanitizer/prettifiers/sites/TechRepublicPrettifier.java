package com.leskor.sanitizer.prettifiers.sites;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import com.leskor.sanitizer.entities.Paragraph;
import com.leskor.sanitizer.entities.Post;
import com.leskor.sanitizer.prettifiers.Prettifier;
import com.leskor.sanitizer.prettifiers.general.ArticlePrefixTrimmingPrettifier;
import com.leskor.sanitizer.prettifiers.general.ArticlePrefixTrimmingPrettifier.Strategy;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
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
        List<Paragraph> result = Jsoup.parse(post.html(), Parser.htmlParser()).getElementsByTag("p")
                .stream()
                .flatMap(this::extractParagraphWithCodeElements)
                .map(this::createParagraph)
                .filter(p -> !(p.content().startsWith("on ") && p.content().length() < 34)
                        && !p.content().startsWith("SEE:")
                        && !p.content().startsWith("Jump to:"))
                .toList();

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

    private Paragraph createParagraph(Element element) {
        if ("code".equals(element.tagName())) {
            return new Paragraph(encodeCode(element.html()), "code");
        }
        return new Paragraph(Jsoup.clean(element.html(), Safelist.none()).trim(), "");
    }

    private String encodeCode(String raw) {
        return raw.replaceAll("\\n", "<:<newline>:>");
    }
}
