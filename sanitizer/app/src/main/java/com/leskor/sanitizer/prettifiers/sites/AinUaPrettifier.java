package com.leskor.sanitizer.prettifiers.sites;

import com.leskor.sanitizer.entities.Paragraph;
import com.leskor.sanitizer.entities.Post;
import com.leskor.sanitizer.prettifiers.Prettifier;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.safety.Safelist;

import java.util.List;
import java.util.stream.Stream;

public class AinUaPrettifier implements Prettifier {
    @Override
    public List<Paragraph> parseParagraphs(Post post) {
        Document document = Jsoup.parse(post.html(), Parser.htmlParser());

        Element wrapper = document.getElementById("main");

        if (wrapper == null) {
            wrapper  = document.getElementById("post-content");
            if (wrapper == null) {
                return List.of();
            }
        }

        return wrapper.children().stream()
                .filter(e -> "p".equals(e.tagName()) || "ul".equals(e.tagName()))
                .flatMap(e -> "ul".equals(e.tagName()) ?
                        e.getElementsByTag("li").stream().map(Element::html)
                        : Stream.of(e.html()))
                .filter(html -> !html.contains(post.title()))
                .map(html -> Jsoup.clean(html, Safelist.none()))
                .map(p -> new Paragraph(p, ""))
                .toList();
    }
}
