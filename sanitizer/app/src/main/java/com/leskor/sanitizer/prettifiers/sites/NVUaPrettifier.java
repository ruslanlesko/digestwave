package com.leskor.sanitizer.prettifiers.sites;

import com.leskor.sanitizer.entities.Paragraph;
import com.leskor.sanitizer.entities.Post;
import com.leskor.sanitizer.prettifiers.Prettifier;
import com.leskor.sanitizer.prettifiers.general.ArticlePrefixTrimmingPrettifier;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class NVUaPrettifier implements Prettifier {
    private final ArticlePrefixTrimmingPrettifier articlePrefixTrimmingPrettifier;

    public NVUaPrettifier() {
        this.articlePrefixTrimmingPrettifier = new ArticlePrefixTrimmingPrettifier(
                Set.of("Ctrl + Enter", "Дайджест головних новин", "Безкоштовна email-розсилка", "Розсилка відправляється")
        );
    }
    @Override
    public List<Paragraph> parseParagraphs(Post post) {
        List<Paragraph> paragraphs = Arrays.stream(post.content().split("\n"))
                .map(p -> Jsoup.clean(p, Safelist.none()).replaceAll("&nbsp;", " "))
                .map(p -> new Paragraph(p, ""))
                .toList();

        return articlePrefixTrimmingPrettifier.trimParagraphs(paragraphs);
    }
}
