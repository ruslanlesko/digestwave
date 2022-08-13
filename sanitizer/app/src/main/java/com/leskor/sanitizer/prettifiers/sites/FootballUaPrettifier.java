package com.leskor.sanitizer.prettifiers.sites;

import com.leskor.sanitizer.entities.Post;
import com.leskor.sanitizer.prettifiers.Prettifier;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.safety.Safelist;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FootballUaPrettifier implements Prettifier {
    @Override
    public List<String> parseParagraphs(Post post) {
        Document document = Jsoup.parse(post.html(), Parser.htmlParser());

        Element wrapper = document.getElementById("ctl00_columnTop");

        if (wrapper == null || wrapper.getElementsByTag("article").isEmpty()) {
            return Arrays.stream(post.content().split("\n")).toList();
        }

        Element article = wrapper.getElementsByTag("article").first();

        List<String> result = new ArrayList<>();
        boolean isHeaderPassed = false;
        for (var element : article.children()) {
            String tagName = element.tagName();
            if (isHeaderPassed) {
                if ("p".equals(tagName)) {
                    result.add(Jsoup.clean(element.html(), Safelist.none()));
                } else if ("div".equals(tagName)) {
                    List<String> newParagraphs = element.getElementsByTag("p").stream()
                            .filter(e -> e.getElementsByTag("span").isEmpty())
                            .map(e -> Jsoup.clean(e.html(), Safelist.none()))
                            .toList();
                    result.addAll(newParagraphs);
                }
            } else {
                if ("h2".equals(tagName)) {
                    isHeaderPassed = true;
                }
            }
        }

        return result;
    }
}
