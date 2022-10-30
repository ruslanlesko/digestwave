package com.leskor.sanitizer.prettifiers.sites;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import com.leskor.sanitizer.entities.Paragraph;
import com.leskor.sanitizer.entities.Post;
import com.leskor.sanitizer.prettifiers.Prettifier;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.safety.Safelist;

public class DzonePrettifier implements Prettifier {
    @Override
    public List<Paragraph> parseParagraphs(Post post) {
        Element outerWrapper = Jsoup.parse(post.html(), Parser.htmlParser()).getElementsByTag("div").first();

        if (outerWrapper == null) {
            return List.of();
        }

        Optional<Element> innerWrapper = outerWrapper.children().stream()
                .filter(e -> "div".equals(e.tagName()))
                .findFirst();

        if (innerWrapper.isEmpty()) {
            return List.of();
        }

        return innerWrapper.get().children()
                .stream()
                .filter(e -> "p".equals(e.tagName()) || e.hasAttr("data-lang"))
                .map(this::createParagraph)
                .filter(Objects::nonNull)
                .toList();
    }

    private Paragraph createParagraph(Element element) {
        if (element.hasAttr("data-lang")) {
            Element codeElement = element.getElementsByTag("code").first();
            return codeElement == null ? null : new Paragraph(encodeCode(codeElement.html()), "code");
        }
        return new Paragraph(Jsoup.clean(element.html(), Safelist.none()).trim(), "");
    }

    private String encodeCode(String raw) {
        return raw.replaceAll("\\n", "<:<newline>:>");
    }
}
