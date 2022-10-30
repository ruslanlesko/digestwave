package com.leskor.sanitizer.prettifiers.general;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import com.leskor.sanitizer.entities.Paragraph;
import com.leskor.sanitizer.entities.Post;
import com.leskor.sanitizer.prettifiers.Prettifier;

import static java.util.stream.Collectors.toSet;

public class ArticlePrefixTrimmingPrettifier implements Prettifier {
    private final Set<String> keywords;
    private final Strategy strategy;

    public ArticlePrefixTrimmingPrettifier(String keyword) {
        this(Set.of(keyword));
    }

    public ArticlePrefixTrimmingPrettifier(String keyword, Strategy strategy) {
        this(Set.of(keyword), strategy);
    }

    public ArticlePrefixTrimmingPrettifier(Set<String> keywords) {
        this(keywords, Strategy.CONTAINS);
    }

    public ArticlePrefixTrimmingPrettifier(Set<String> keywords, Strategy strategy) {
        this.keywords = keywords.stream().map(String::toUpperCase).collect(toSet());
        this.strategy = strategy;
    }

    @Override
    public List<Paragraph> parseParagraphs(Post post) {
        List<Paragraph> paragraphs = Arrays.stream(post.content().split("\n"))
                .map(p -> new Paragraph(p, ""))
                .toList();
        return trimParagraphs(paragraphs);
    }

    public List<Paragraph> trimParagraphs(List<Paragraph> paragraphs) {
        int paragraphEndingPostIdx = -1;
        for (int i = 0; i < paragraphs.size(); i++) {
            String p = paragraphs.get(i).content().trim().toUpperCase();
            Predicate<String> matcher = switch (strategy) {
                case CONTAINS -> p::contains;
                case STARTS_WITH -> p::startsWith;
            };
            if (keywords.stream().anyMatch(matcher)) {
                paragraphEndingPostIdx = i;
                break;
            }
        }

        return paragraphEndingPostIdx != -1 ?
                paragraphs.subList(0, paragraphEndingPostIdx) : paragraphs;
    }

    public enum Strategy {
        CONTAINS, STARTS_WITH
    }
}
