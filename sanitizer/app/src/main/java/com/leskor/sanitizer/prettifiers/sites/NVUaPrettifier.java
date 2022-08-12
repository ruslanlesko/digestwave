package com.leskor.sanitizer.prettifiers.sites;

import com.leskor.sanitizer.entities.Post;
import com.leskor.sanitizer.prettifiers.Prettifier;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import java.util.Arrays;
import java.util.List;

public class NVUaPrettifier implements Prettifier {
    @Override
    public List<String> parseParagraphs(Post post) {
        return Arrays.stream(post.content().split("\n"))
                .map(p -> Jsoup.clean(p, Safelist.none()).replaceAll("&nbsp;", " "))
                .toList();
    }
}
