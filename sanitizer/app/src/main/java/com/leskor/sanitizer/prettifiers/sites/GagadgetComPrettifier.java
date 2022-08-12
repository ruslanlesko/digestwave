package com.leskor.sanitizer.prettifiers.sites;

import com.leskor.sanitizer.entities.Post;
import com.leskor.sanitizer.prettifiers.Prettifier;

import java.util.Arrays;
import java.util.List;

public class GagadgetComPrettifier implements Prettifier {
    @Override
    public List<String> parseParagraphs(Post post) {
        List<String> result = Arrays.stream(post.content().split("\n"))
                .filter(p -> !p.toUpperCase().contains("ЗА ПІДТРИМКИ") && !p.toUpperCase().contains("GG"))
                .toList();

        int paragraphContainingEditingSuggestionIdx = -1;
        for (int i = 0; i < result.size(); i++) {
            if (result.get(i).contains("Джерело:")) {
                paragraphContainingEditingSuggestionIdx = i;
                break;
            }
        }

        return paragraphContainingEditingSuggestionIdx != -1 ?
                result.subList(0, paragraphContainingEditingSuggestionIdx) : result;
    }
}
