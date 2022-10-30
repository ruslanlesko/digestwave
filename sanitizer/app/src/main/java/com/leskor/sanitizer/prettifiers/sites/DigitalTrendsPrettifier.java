package com.leskor.sanitizer.prettifiers.sites;

import java.util.List;
import com.leskor.sanitizer.entities.Paragraph;
import com.leskor.sanitizer.entities.Post;
import com.leskor.sanitizer.prettifiers.Prettifier;
import com.leskor.sanitizer.prettifiers.general.ArticlePrefixTrimmingPrettifier;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Safelist;

public class DigitalTrendsPrettifier implements Prettifier {
    private final ArticlePrefixTrimmingPrettifier articlePrefixTrimmingPrettifier;

    public DigitalTrendsPrettifier() {
        articlePrefixTrimmingPrettifier = new ArticlePrefixTrimmingPrettifier("Editors' Recommendations");
    }

    @Override
    public List<Paragraph> parseParagraphs(Post post) {
        Element wrapper = Jsoup.parse(post.html()).getElementsByTag("article").first();
        if (wrapper == null) return List.of();

        List<Paragraph> result = wrapper.getElementsByTag("p")
                .stream()
                .map(p -> Jsoup.clean(p.html(), Safelist.none()))
                .filter(p -> !p.equalsIgnoreCase("Contents") && !p.equalsIgnoreCase("Jump to details"))
                .map(p -> new Paragraph(p, ""))
                .toList();

        return articlePrefixTrimmingPrettifier.trimParagraphs(result);
    }
}
