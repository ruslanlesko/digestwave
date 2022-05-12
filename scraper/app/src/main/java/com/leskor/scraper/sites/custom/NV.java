package com.leskor.scraper.sites.custom;

import com.leskor.scraper.dto.ReadabilityResponse;
import com.leskor.scraper.entities.Post;
import com.leskor.scraper.entities.Topic;
import com.leskor.scraper.sites.RSSSite;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URI;
import java.net.http.HttpClient;
import java.time.ZonedDateTime;
import java.util.Set;

import static java.time.Duration.ofSeconds;

public class NV extends RSSSite {
    private static final URI INDEX_URI = URI.create("https://nv.ua/ukr/rss/2292.xml");

    public NV(URI readabilityUri, HttpClient httpClient) {
        super(INDEX_URI, readabilityUri, "NV", Topic.FINANCE, httpClient, ofSeconds(10), null, Set.of());
    }

    @Override
    protected Post buildPost(String siteCode, Topic topic, ZonedDateTime publicationTime, ReadabilityResponse readabilityResponse) {
        String content = parseContent(readabilityResponse.content());
        String imageUrl = extractImageUrl(readabilityResponse.content());
        return Post.from(siteCode, topic, publicationTime, readabilityResponse.title(), content, imageUrl);
    }

    private String parseContent(String htmlContent) {
        Document document = Jsoup.parse(htmlContent);
        Elements paragraphs = document.getElementsByTag("p");
        StringBuilder result = new StringBuilder();
        for (var p : paragraphs) {
            if (!p.getElementsByTag("b").isEmpty()) {
                continue;
            }
            var fragment = p.html();
            if (fragment.startsWith("Приєднуйтесь до нас")) {
                return result.toString();
            }
            if (fragment.contains("(Фото:")) {
                continue;
            }
            result.append(fragment);
            result.append('\n');
        }
        return result.toString();
    }

    private String extractImageUrl(String htmlContent) {
        Document document = Jsoup.parse(htmlContent);
        Element image = document.getElementsByTag("img").first();
        if (image == null) {
            return "";
        }
        return image.attr("src");
    }
}