package com.leskor.sanitizer.prettifiers;

import com.leskor.sanitizer.entities.Paragraph;
import com.leskor.sanitizer.entities.Post;

import java.util.Arrays;
import java.util.List;

public class SimplePrettifier implements Prettifier {
    @Override
    public List<Paragraph> parseParagraphs(Post post) {
        return Arrays.stream(post.content().split("\n"))
                .map(p -> new Paragraph(p, ""))
                .toList();
    }
}
