package com.leskor.scraper.sites.custom;

import com.leskor.scraper.entities.Post;
import com.leskor.scraper.entities.Region;
import com.leskor.scraper.entities.Topic;
import com.leskor.scraper.sites.Site;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.charset.Charset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import static java.time.Duration.ofSeconds;
import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;

public class EconomicnaPravda extends Site {
    private static final URI INDEX_URI = URI.create("https://www.epravda.com.ua/rss/");
    private static final int POSTS_LIMIT = 10;

    public EconomicnaPravda(HttpClient httpClient) {
        super(INDEX_URI, null, "EPR", httpClient, ofSeconds(10), Topic.FINANCE, Region.UA, Set.of());
    }

    @Override
    protected Charset charset() {
        return Charset.forName("Windows-1251");
    }

    @Override
    protected List<CompletableFuture<Post>> extractPostsBasedOnPage(String page) {
        Document document = Jsoup.parse(page, Parser.xmlParser());
        var postElements = document.getElementsByTag("item");
        int limit = POSTS_LIMIT;
        List<CompletableFuture<Post>> result = new ArrayList<>();
        for (var elem : postElements) {
            if (limit <= 0) break;

            Element titleElement = elem.getElementsByTag("title").first();
            Element linkElement = elem.getElementsByTag("link").first();
            Element rawTextElement = elem.getElementsByTag("fulltext").first();
            var dateElement = elem.getElementsByTag("pubDate").first();
            if (titleElement == null || linkElement == null || rawTextElement == null || dateElement == null) continue;

            limit--;

            var dateString = dateElement.text();
            var title = titleElement.text();
            var link = linkElement.text();

            String imageUrl = null;
            Element enclosure = elem.getElementsByTag("enclosure").first();
            if (enclosure != null) {
                if (enclosure.hasAttr("url") && enclosure.hasAttr("type")) {
                    if (enclosure.attr("type").startsWith("image")) {
                        imageUrl = enclosure.attr("url");
                    }
                }
            }

            try {
                ZonedDateTime publicationTime = ZonedDateTime.parse(dateString, RFC_1123_DATE_TIME);
                String text = parseRawText(rawTextElement);
                String html = parseHtml(rawTextElement);
                var newPost = Post.from(siteCode, topic, region, publicationTime, title, text, html, URI.create(link), imageUrl);

                result.add(CompletableFuture.completedFuture(newPost));
            } catch (IllegalArgumentException e) {
                logger.warn("Cannot parse URL or post is too short", e);
            } catch (DateTimeParseException e) {
                logger.warn("Cannot parse publication time", e);
            }
        }
        return result;
    }

    private String parseRawText(Element rawTextElement) {
        if (rawTextElement.childNodes().isEmpty()) {
            return "";
        }
        if (!rawTextElement.childNodes().get(0).attributes().hasKeyIgnoreCase("#cdata")) {
            return "";
        }
        String raw = rawTextElement.childNodes().get(0).attributes().get("#cdata");
        Document document = Jsoup.parse(raw);
        Elements paragraphs = document.getElementsByTag("p");
        StringBuilder result = new StringBuilder();
        for (var p : paragraphs) {
            if (!p.children().isEmpty() && p.child(0).html().startsWith("Про це")) {
                continue;
            }
            StringBuilder text = new StringBuilder();
            for (var child : p.children()) {
                var fragment = child.html();
                if (fragment.startsWith("Нагадаємо")
                        || fragment.startsWith("Нагадуємо")
                        || fragment.startsWith("Читайте також")) {
                    return result.toString();
                }
                text.append(fragment);
            }
            result.append(text);
            result.append('\n');
        }
        return result.toString();
    }

    private String parseHtml(Element rawTextElement) {
        if (rawTextElement.childNodes().isEmpty()) {
            return "";
        }
        if (!rawTextElement.childNodes().get(0).attributes().hasKeyIgnoreCase("#cdata")) {
            return "";
        }

        return rawTextElement.childNodes().get(0).attributes().get("#cdata");
    }
}
