package com.leskor.sanitizer;

import com.leskor.sanitizer.entities.Paragraph;
import com.leskor.sanitizer.entities.SanitizedPost;

public class GlobalFilter {
    boolean filterParagraph(Paragraph paragraph) {
        return paragraph != null && paragraph.content().length() > 0;
    }

    boolean validatePost(SanitizedPost original) {
        return original != null
                && original.paragraphs() != null
                && original.paragraphs().size() > 2;
    }
}
