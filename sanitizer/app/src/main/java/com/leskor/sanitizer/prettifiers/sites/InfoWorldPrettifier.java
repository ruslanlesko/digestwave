package com.leskor.sanitizer.prettifiers.sites;

import java.util.List;
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
                .map(p -> Jsoup.clean(p.html(), Safelist.none()).trim())
                .map(p -> new Paragraph(p, ""))
                .toList();
    }
}
