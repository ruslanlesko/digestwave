package com.leskor.sanitizer.prettifiers.sites;

import java.util.List;
import com.leskor.sanitizer.entities.Post;
import com.leskor.sanitizer.prettifiers.Prettifier;
import com.leskor.sanitizer.prettifiers.general.ArticlePrefixTrimmingPrettifier;
import com.leskor.sanitizer.prettifiers.general.ArticlePrefixTrimmingPrettifier.Strategy;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Safelist;

public class TechradarPrettifier implements Prettifier {
    private final ArticlePrefixTrimmingPrettifier articlePrefixTrimmingPrettifier;

    public TechradarPrettifier() {
        articlePrefixTrimmingPrettifier = new ArticlePrefixTrimmingPrettifier("Via", Strategy.STARTS_WITH);
    }
    @Override
    public List<String> parseParagraphs(Post post) {
        Element wrapper = Jsoup.parse(post.html()).getElementById("article-body");
        if (wrapper == null) return List.of();

        List<String> result = wrapper.getElementsByTag("p")
                .stream()
                .map(p -> Jsoup.clean(p.html(), Safelist.none()))
                .filter(p -> !p.equalsIgnoreCase("See more"))
                .toList();

        return articlePrefixTrimmingPrettifier.trimParagraphs(result);
    }
}
