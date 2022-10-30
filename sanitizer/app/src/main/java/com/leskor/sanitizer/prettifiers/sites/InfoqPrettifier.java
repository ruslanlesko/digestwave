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

public class InfoqPrettifier implements Prettifier {
    @Override
    public List<Paragraph> parseParagraphs(Post post) {
        return Jsoup.parse(post.html(), Parser.htmlParser())
                .getElementsByTag("p")
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
        if (nextCodeElement == null) return Stream.of(p);

        Element innerCodeElement = nextCodeElement.getElementsByTag("code").first();
        return Stream.of(p, innerCodeElement == null ? nextCodeElement : innerCodeElement);
    }

    private Paragraph createParagraph(Element element) {
        if ("pre".equals(element.tagName()) || "code".equals(element.tagName())) {
            return new Paragraph(element.html().replaceAll("\\n", "<br>"), "code");
        }
        return new Paragraph(Jsoup.clean(element.html(), Safelist.none()).trim(), "");
    }
}
