package com.leskor.sanitizer.prettifiers;

import com.leskor.sanitizer.entities.Paragraph;
import com.leskor.sanitizer.entities.Post;

import java.util.List;

public interface Prettifier {
    List<Paragraph> parseParagraphs(Post post);
}
