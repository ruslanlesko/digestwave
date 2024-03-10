package com.leskor.sanitizer.prettifiers.sites;

import com.leskor.sanitizer.entities.Paragraph;
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
    public List<Paragraph> parseParagraphs(Post post) {
        StringBuilder contentBuilder = new StringBuilder("");
        for (int i = 0; i < post.content().length() - 1; i++) {
            if (post.content().charAt(i) == '.' && post.content().charAt(i + 1) != ' ') {
                contentBuilder.append(".\n");
            } else {
                contentBuilder.append(post.content().charAt(i));
            }
        }
        String content = contentBuilder.toString();
        List<Paragraph> result = Arrays.stream(content.split("\n"))
                .filter(p -> !p.contains("Підписуйтесь на")
                        && !p.contains("Читайте також:")
                        && !p.contains("Читайте нас")
                        && !p.contains("Приєднуйтесь до нас у Facebook")
                        && !p.contains(post.title())
                        && !p.contains("Фото:")
                        && !p.contains("Дивіться також:")
                        && p.length() > 4
                )
                .map(p -> new Paragraph(p, ""))
                .toList();

        return articlePrefixTrimmingPrettifier.trimParagraphs(result);
    }
}
