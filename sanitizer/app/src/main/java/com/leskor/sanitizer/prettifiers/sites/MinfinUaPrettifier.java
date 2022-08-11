package com.leskor.sanitizer.prettifiers.sites;

import com.leskor.sanitizer.entities.Post;
import com.leskor.sanitizer.prettifiers.Prettifier;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MinfinUaPrettifier implements Prettifier {
    public static final Set<String> EXCLUDED_PARAGRAPHS_CONTENT =
            Set.of("Читайте також", "Підписуйтесь на", "Читайте:", "«Мінфін");

    @Override
    public List<String> parseParagraphs(Post post) {
        String html = post.html();
        if (html == null || html.isBlank()) {
            return List.of();
        }

        Document document = Jsoup.parse(html);
        List<String> result = new ArrayList<>();
        for (var p : document.getElementsByTag("p")) {
            String cleanedText = Jsoup.clean(p.html(), Safelist.none());
            if (cleanedText.length() > 0 && EXCLUDED_PARAGRAPHS_CONTENT.stream().noneMatch(cleanedText::contains)) {
                result.add(cleanedText);
            }
        }

        return result;
    }
}
