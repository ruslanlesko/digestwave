package com.leskor.sanitizer.prettifiers.sites;

import com.leskor.sanitizer.entities.Post;
import com.leskor.sanitizer.prettifiers.Prettifier;
import com.leskor.sanitizer.prettifiers.general.ArticlePrefixTrimmingPrettifier;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class LigaNetPrettifier implements Prettifier {
    private final ArticlePrefixTrimmingPrettifier articlePrefixTrimmingPrettifier;

    public LigaNetPrettifier() {
        articlePrefixTrimmingPrettifier = new ArticlePrefixTrimmingPrettifier(Set.of("Ctrl+Enter", "Читайте нас у"));
    }

    @Override
    public List<String> parseParagraphs(Post post) {
        List<String> result = Arrays.stream(post.content().split("\n"))
                .filter(p -> !p.contains("Підписуйтесь на LIGA")
                        && !p.contains("Читайте також:")
                        && !p.contains("Приєднуйтесь до нас у Facebook")
                )
                .toList();

        return articlePrefixTrimmingPrettifier.trimParagraphs(result);
    }
}
