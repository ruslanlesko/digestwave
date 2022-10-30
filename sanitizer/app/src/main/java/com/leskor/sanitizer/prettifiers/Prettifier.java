package com.leskor.sanitizer.prettifiers;

import java.util.List;
import com.leskor.sanitizer.entities.Paragraph;
import com.leskor.sanitizer.entities.Post;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Safelist;

public interface Prettifier {
    List<Paragraph> parseParagraphs(Post post);

    default Paragraph createParagraph(Element element) {
        if ("pre".equals(element.tagName()) || "code".equals(element.tagName())) {
            return new Paragraph(element.html().replaceAll("\\n", "<br>"), "code");
        }
        return new Paragraph(Jsoup.clean(element.html(), Safelist.none()).trim(), "");
    }
}
