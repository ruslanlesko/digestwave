package com.leskor.sanitizer.prettifiers.sites;

import java.util.List;
import com.leskor.sanitizer.entities.Paragraph;
import com.leskor.sanitizer.entities.Post;
import com.leskor.sanitizer.prettifiers.Prettifier;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.safety.Safelist;

public class TheStreetPrettifier implements Prettifier {
    @Override
    public List<Paragraph> parseParagraphs(Post post) {
        Element outerWrapper = Jsoup.parse(post.html(), Parser.htmlParser()).getElementsByTag("div").first();

        if (outerWrapper == null) {
            return List.of();
        }

        return outerWrapper.children().stream()
                .flatMap(div -> div.children().stream())
                .filter(e -> "p".equals(e.tagName()))
                .map(p -> Jsoup.clean(p.html(), Safelist.none()).replaceAll("&nbsp;", " ").trim())
                .map(p -> new Paragraph(p, ""))
                .toList();
    }
}
