package com.leskor.sanitizer.prettifiers.sites;

import com.leskor.sanitizer.entities.Paragraph;
import com.leskor.sanitizer.entities.Post;
import com.leskor.sanitizer.prettifiers.Prettifier;

import java.util.Arrays;
import java.util.List;

public class EpravdaUaPrettifier implements Prettifier {
    @Override
    public List<Paragraph> parseParagraphs(Post post) {
        List<Paragraph> result = Arrays.stream(post.content().split("\n"))
                .map(p -> new Paragraph(p, ""))
                .toList();

        int paragraphMarkingEndOfThePostIdx = -1;
        for (int i = 0; i < result.size(); i++) {
            String[] words = result.get(i).content().split(" ");
            if (words.length == 2 && "правда".equalsIgnoreCase(words[1])) {
                paragraphMarkingEndOfThePostIdx = i;
                break;
            }
            if (words.length == 1 && (words[0].contains("Нагадаємо") || words[0].contains("Нагадуємо"))) {
                paragraphMarkingEndOfThePostIdx = i;
                break;
            }
        }

        return paragraphMarkingEndOfThePostIdx != -1 ?
                result.subList(0, paragraphMarkingEndOfThePostIdx) : result;
    }
}
