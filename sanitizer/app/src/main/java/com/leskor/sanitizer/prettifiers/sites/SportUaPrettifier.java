package com.leskor.sanitizer.prettifiers.sites;

import com.leskor.sanitizer.entities.Post;
import com.leskor.sanitizer.prettifiers.Prettifier;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import java.util.List;

public class SportUaPrettifier implements Prettifier {
    @Override
    public List<String> parseParagraphs(Post post) {
        List<String> result = Jsoup.parse(post.html()).getElementsByTag("p").stream()
                .map(p -> Jsoup.clean(p.html(), Safelist.none()))
                .filter(p -> !p.toUpperCase().contains("ТЕКСТОВА ТРАНСЛЯЦІЯ МАТЧУ"))
                .toList();

        int paragraphContainingEndOfPostIdx = -1;
        for (int i = 0; i < result.size(); i++) {
            if (result.get(i).trim().startsWith("Читайте нас")) {
                paragraphContainingEndOfPostIdx = i;
                break;
            }
        }

        return paragraphContainingEndOfPostIdx != -1 ?
                result.subList(0, paragraphContainingEndOfPostIdx) : result;
    }
}
