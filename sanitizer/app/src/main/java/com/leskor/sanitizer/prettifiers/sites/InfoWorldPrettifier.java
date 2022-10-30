package com.leskor.sanitizer.prettifiers.sites;

import java.util.List;
import java.util.stream.Stream;
import com.leskor.sanitizer.entities.Paragraph;
import com.leskor.sanitizer.entities.Post;
import com.leskor.sanitizer.prettifiers.Prettifier;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.safety.Safelist;

public class InfoWorldPrettifier implements Prettifier {
    @Override
    public List<Paragraph> parseParagraphs(Post post) {
        Element wrapper = Jsoup.parse(post.html(), Parser.htmlParser()).getElementById("drr-container");
        if (wrapper == null) {
            return List.of();
        }
        return wrapper.getElementsByTag("p")
                .stream()
                .flatMap(this::extractParagraphWithCodeElements)
                .map(this::createParagraph)
                .toList();
    }

    private Stream<Element> extractParagraphWithCodeElements(Element p) {
        Element nextSibling = p.nextElementSibling();
        Element nextCodeElement = null;
        if (nextSibling != null && !"p".equals(nextSibling.tagName())) {
            nextCodeElement = nextSibling.getElementsByTag("pre").first();
        }
        return nextCodeElement == null ? Stream.of() : Stream.of(p, nextCodeElement);
    }

    private Paragraph createParagraph(Element element) {
        if ("pre".equals(element.tagName())) {
            return new Paragraph(element.html(), "code");
        }
        return new Paragraph(Jsoup.clean(element.html(), Safelist.none()).trim(), "");
    }
}
