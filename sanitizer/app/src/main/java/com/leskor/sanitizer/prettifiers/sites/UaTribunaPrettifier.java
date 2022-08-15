package com.leskor.sanitizer.prettifiers.sites;

import com.leskor.sanitizer.entities.Post;
import com.leskor.sanitizer.prettifiers.Prettifier;
import org.jsoup.Jsoup;
import org.jsoup.parser.Parser;
import org.jsoup.safety.Safelist;

import java.util.List;

public class UaTribunaPrettifier implements Prettifier {
    @Override
    public List<String> parseParagraphs(Post post) {
        return Jsoup.parse(post.html(), Parser.htmlParser())
                .getElementsByTag("p")
                .stream()
                .map(p -> Jsoup.clean(p.html(), Safelist.none()))
                .toList();
    }
}
