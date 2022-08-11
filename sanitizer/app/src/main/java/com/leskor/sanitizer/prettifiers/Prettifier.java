package com.leskor.sanitizer.prettifiers;

import com.leskor.sanitizer.entities.Post;

import java.util.List;

public interface Prettifier {
    List<String> parseParagraphs(Post post);
}
