package com.leskor.sanitizer.prettifiers.sites;

import java.util.List;
import com.leskor.sanitizer.entities.Post;
import com.leskor.sanitizer.prettifiers.Prettifier;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Safelist;

public class TomsguidePrettifier implements Prettifier {
    @Override
    public List<String> parseParagraphs(Post post) {
        Element wrapper = Jsoup.parse(post.html()).getElementById("article-body");
        if (wrapper == null) return List.of();

        return wrapper.getElementsByTag("p")
                .stream()
                .map(p -> Jsoup.clean(p.html(), Safelist.none()).trim())
                .filter(p -> !p.equalsIgnoreCase("SEE MORE"))
                .toList();
    }
}
