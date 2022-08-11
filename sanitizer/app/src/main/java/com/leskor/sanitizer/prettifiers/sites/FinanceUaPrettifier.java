package com.leskor.sanitizer.prettifiers.sites;

import com.leskor.sanitizer.entities.Post;
import com.leskor.sanitizer.prettifiers.Prettifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FinanceUaPrettifier implements Prettifier {
    @Override
    public List<String> parseParagraphs(Post post) {
        String[] paragraphs = post.content().split("\n");
        if (paragraphs.length == 0) {
            return List.of();
        }

        List<String> result = new ArrayList<>();

        String firstParagraph = paragraphs[0];
        if (!firstParagraph.contains(post.title())) {
            result.add(firstParagraph);
        }
        result.addAll(Arrays.stream(paragraphs).skip(1).toList());

        int paragraphContainingEditingSuggestionIdx = -1;
        for (int i = 0; i < result.size(); i++) {
            if (result.get(i).contains("Ctrl+Enter")) {
                paragraphContainingEditingSuggestionIdx = i;
                break;
            }
        }

        if (paragraphContainingEditingSuggestionIdx != -1) {
            result = result.subList(0, paragraphContainingEditingSuggestionIdx);
        }

        if (result.get(result.size() - 1).length() < 42) {
            result.remove(result.size() - 1);
        }

        return result;
    }
}
