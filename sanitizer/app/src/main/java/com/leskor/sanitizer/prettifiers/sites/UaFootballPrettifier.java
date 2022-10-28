package com.leskor.sanitizer.prettifiers.sites;

import com.leskor.sanitizer.entities.Paragraph;
import com.leskor.sanitizer.entities.Post;
import com.leskor.sanitizer.prettifiers.Prettifier;

import java.util.Arrays;
import java.util.List;

public class UaFootballPrettifier implements Prettifier {
    @Override
    public List<Paragraph> parseParagraphs(Post post) {
        List<Paragraph> result = Arrays.stream(post.content().split("\n"))
                .filter(p -> !p.trim().toUpperCase().startsWith("ФОТО"))
                .map(p -> new Paragraph(p, ""))
                .toList();

        int paragraphContainingEndOfPostIdx = -1;
        int paragraphContainingBeginningOfPostIds = 0;
        for (int i = 0; i < result.size(); i++) {
            String paragraph = result.get(i).content().trim();
            if (paragraph.contains("Ссылка скопирована")) {
                paragraphContainingBeginningOfPostIds = i;
            }
            if (paragraph.startsWith("Читайте також")) {
                paragraphContainingEndOfPostIdx = i;
                break;
            }
        }

        return paragraphContainingEndOfPostIdx != -1 ?
                result.subList(paragraphContainingBeginningOfPostIds, paragraphContainingEndOfPostIdx) :
                result.subList(paragraphContainingBeginningOfPostIds, result.size());
    }
}
