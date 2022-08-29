package com.leskor.sanitizer.prettifiers.sites;

import java.util.List;
import java.util.Optional;
import com.leskor.sanitizer.entities.Post;
import com.leskor.sanitizer.prettifiers.Prettifier;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.safety.Safelist;

public class DzonePrettifier implements Prettifier {
    @Override
    public List<String> parseParagraphs(Post post) {
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
                .filter(e -> "p".equals(e.tagName()))
                .map(p -> Jsoup.clean(p.html(), Safelist.none()).trim())
                .toList();
    }
}
