package com.leskor.sanitizer.prettifiers.sites;

import java.util.List;
import com.leskor.sanitizer.entities.Paragraph;
import com.leskor.sanitizer.entities.Post;
import com.leskor.sanitizer.prettifiers.Prettifier;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Safelist;

public class CnetPrettifier implements Prettifier {
    @Override
    public List<Paragraph> parseParagraphs(Post post) {
        Element wrapper = Jsoup.parse(post.html()).getElementsByAttribute("data-component").first();
        if (wrapper == null) return List.of();

        return wrapper.getElementsByTag("p")
                .stream()
                .map(p -> Jsoup.clean(p.html(), Safelist.none()).trim())
                .filter(p -> !p.toUpperCase().startsWith("NOW PLAYING") && p.length() > 5)
                .map(p -> new Paragraph(p, ""))
                .toList();
    }
}
