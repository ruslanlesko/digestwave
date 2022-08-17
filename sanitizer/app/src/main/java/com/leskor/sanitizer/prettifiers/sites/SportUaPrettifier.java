package com.leskor.sanitizer.prettifiers.sites;

import com.leskor.sanitizer.entities.Post;
import com.leskor.sanitizer.prettifiers.Prettifier;
import com.leskor.sanitizer.prettifiers.general.ArticlePrefixTrimmingPrettifier;
import com.leskor.sanitizer.prettifiers.general.ArticlePrefixTrimmingPrettifier.Strategy;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import java.util.List;

public class SportUaPrettifier implements Prettifier {
    private final ArticlePrefixTrimmingPrettifier articlePrefixTrimmingPrettifier;

    public SportUaPrettifier() {
        articlePrefixTrimmingPrettifier = new ArticlePrefixTrimmingPrettifier("Читайте нас", Strategy.STARTS_WITH);
    }

    @Override
    public List<String> parseParagraphs(Post post) {
        List<String> result = Jsoup.parse(post.html()).getElementsByTag("p").stream()
                .map(p -> Jsoup.clean(p.html(), Safelist.none()))
                .filter(p -> !p.toUpperCase().contains("ТЕКСТОВА ТРАНСЛЯЦІЯ МАТЧУ"))
                .toList();

        return articlePrefixTrimmingPrettifier.trimParagraphs(result);
    }
}
