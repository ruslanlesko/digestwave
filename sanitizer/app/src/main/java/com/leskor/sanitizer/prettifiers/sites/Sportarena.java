package com.leskor.sanitizer.prettifiers.sites;

import com.leskor.sanitizer.entities.Post;
import com.leskor.sanitizer.prettifiers.Prettifier;

import java.util.Arrays;
import java.util.List;

public class Sportarena implements Prettifier {
    @Override
    public List<String> parseParagraphs(Post post) {
        List<String> result = Arrays.stream(post.content().split("\n")).toList();

        int paragraphContainingEndOfPostIdx = -1;
        for (int i = 0; i < result.size(); i++) {
            if (result.get(i).trim().startsWith("Російський")) {
                paragraphContainingEndOfPostIdx = i;
                break;
            }
        }

        return paragraphContainingEndOfPostIdx != -1 ?
                result.subList(0, paragraphContainingEndOfPostIdx) : result;
    }
}
