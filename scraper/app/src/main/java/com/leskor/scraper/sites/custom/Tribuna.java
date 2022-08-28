package com.leskor.scraper.sites.custom;

import java.net.URI;
import java.net.http.HttpClient;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import com.leskor.scraper.entities.Post;
import com.leskor.scraper.entities.Region;
import com.leskor.scraper.entities.Topic;
import com.leskor.scraper.sites.Site;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Safelist;

import static java.time.Duration.ofSeconds;

public class Tribuna extends Site {
    private static final URI INDEX_URI = URI.create("https://ua.tribuna.com/uk/football/");

    public Tribuna(URI readabilityUri, HttpClient httpClient) {
        super(
                INDEX_URI,
                readabilityUri,
                "TRI",
                httpClient,
                ofSeconds(10),
                Topic.FOOTBALL,
                Region.UA,
                Set.of("Відео"),
                false
        );
    }

    @Override
    protected List<CompletableFuture<Post>> extractPostsBasedOnPage(String page) {
        Document document = Jsoup.parse(page);
        List<CompletableFuture<Post>> result = new ArrayList<>();
        for (var elem : document.getElementsByClass("NewsItem_news-item__text-wrapper__xiCs4")) {
            Element timeElem = elem.getElementsByClass("NewsItem_news-item__published-time__ifpYI").first();
            Element linkElem = elem.getElementsByTag("a").first();

            if (timeElem == null || linkElem == null) {
                continue;
            }

            Optional<ZonedDateTime> timeOptional = parseTime(timeElem.text());
            if (timeOptional.isEmpty() || !linkElem.hasAttr("href")) {
                continue;
            }

            URI postURI = URI.create("https://ua.tribuna.com" + linkElem.attr("href"));
            String title = Jsoup.clean(linkElem.html(), Safelist.none());
            result.add(extractPost(postURI, title, timeOptional.get()));
        }

        if (result.isEmpty()) {
            logger.error("No posts found for ua.tribuna.com");
        }

        return result;
    }

    private Optional<ZonedDateTime> parseTime(String raw) {
        if (raw == null || raw.length() < 5) {
            return Optional.empty();
        }

        ZoneId zoneId = ZoneId.of("UTC");
        try {
            int hour = Integer.parseInt(raw.substring(0, 2), 10);
            int minute = Integer.parseInt(raw.substring(3, 5), 10);

            ZonedDateTime result = ZonedDateTime.now(zoneId)
                    .withHour(hour)
                    .withMinute(minute);

            return result.isAfter(ZonedDateTime.now(zoneId)) ?
                    Optional.of(result.minusDays(1)) : Optional.of(result);
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    private CompletableFuture<Post> extractPost(URI uri, String title, ZonedDateTime time) {
        return extractArticlePage(uri)
                .thenApply(document -> parsePost(document, title, uri, time));
    }

    private Post parsePost(Document document, String title, URI uri, ZonedDateTime time) {
        Element wrapper = document.getElementsByClass("ContentCard_card__content__TX_NI").first();
        if (wrapper == null) {
            return null;
        }

        String textContent = Jsoup.clean(wrapper.html(), Safelist.none());
        Optional<String> imageURL = extractImageURLFromDocument(document);

        return Post.from(siteCode, topic, region, time, title, textContent, wrapper.html(), uri, imageURL.orElse(""));
    }

    private Optional<String> extractImageURLFromDocument(Document document) {
        return document.getElementsByTag("meta")
                .stream()
                .filter(e -> e.hasAttr("property") && e.hasAttr("content")
                        && "og:image".equals(e.attr("property"))
                )
                .map(e -> e.attr("content"))
                .findFirst();
    }
}
