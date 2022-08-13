package com.leskor.sanitizer.prettifiers.sites;

import com.leskor.sanitizer.entities.Post;
import com.leskor.sanitizer.prettifiers.Prettifier;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.safety.Safelist;

import java.util.List;

public class ItcUaPrettifier implements Prettifier {

    @Override
    public List<String> parseParagraphs(Post post) {
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

        int paragraphContainingEditingSuggestionIdx = -1;
        for (int i = 0; i < result.size(); i++) {
            if (result.get(i).trim().startsWith("Джерело:")) {
                paragraphContainingEditingSuggestionIdx = i;
                break;
            }
        }

        return paragraphContainingEditingSuggestionIdx != -1 ?
                result.subList(0, paragraphContainingEditingSuggestionIdx) : result;
    }
}
