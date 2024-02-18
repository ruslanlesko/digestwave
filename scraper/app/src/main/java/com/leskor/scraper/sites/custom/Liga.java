package com.leskor.scraper.sites.custom;

import static com.leskor.scraper.dto.ReadabilityResponse.fromTitleAndExistingResponse;
import static java.time.Duration.ofSeconds;

import com.leskor.scraper.entities.Post;
import com.leskor.scraper.entities.Region;
import com.leskor.scraper.entities.Topic;
import com.leskor.scraper.sites.Site;
import java.net.URI;
import java.net.http.HttpClient;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Liga extends Site {
    private static final Logger logger = LoggerFactory.getLogger("Application");

    private static final URI INDEX_URI = URI.create("https://finance.liga.net/ua");
    private static final int POSTS_LIMIT = 10;

    public Liga(URI readabilityUri, HttpClient httpClient) {
        super(INDEX_URI, readabilityUri, "LIG", httpClient, ofSeconds(60), Topic.FINANCE, Region.UA,
                Set.of(), false);
    }

    @Override
    protected List<CompletableFuture<Post>> extractPostsBasedOnPage(String page) {
        Document document = Jsoup.parse(page, Parser.htmlParser());
        Element newsElem = document.getElementsByTag("aside").first();
        if (newsElem == null) {
            logger.warn("Failed to locate news on Liga.net");
            return List.of();
        }

        return newsElem.getElementsByTag("article").stream()
                .filter(parentElem -> {
                    Element e = parentElem.getElementsByTag("a").first();
                    return e != null && e.hasAttr("href") && e.hasText() &&
                            parentElem.getElementsByTag("a").stream()
                                    .noneMatch(a -> "Новини".equals(a.text()));
                })
                .limit(POSTS_LIMIT)
                .map(parentElem -> {
                    Element e = parentElem.getElementsByTag("a").first();
                    return extractPost(
                            e.text(),
                            URI.create(e.attr("href")),
                            parseTime(parentElem.getElementsByTag("time").first())
                    );
                })
                .toList();
    }

    private CompletableFuture<Post> extractPost(String title, URI uri, ZonedDateTime time) {
        if (time == null) {
            return CompletableFuture.completedFuture(null);
        }

        return retrieveReadabilityResponse(uri)
                .thenApply(r -> r == null ? null
                        : Post.from(siteCode, topic, region, time,
                        fromTitleAndExistingResponse(title, r), uri));
    }

    private ZonedDateTime parseTime(Element timeElem) {
        if (timeElem == null || !timeElem.hasText()) {
            return null;
        }

        String raw = timeElem.text().toLowerCase();
        String yesterdayPrefix = "вчора о ";
        boolean isYesterday = false;
        if (raw.startsWith(yesterdayPrefix)) {
            raw = raw.substring(yesterdayPrefix.length());
            isYesterday = true;
        }

        if (raw.length() != 5) {
            return null;
        }

        int hour = Integer.parseInt(raw.substring(0, 2), 10);
        int minute = Integer.parseInt(raw.substring(3, 5), 10);

        ZonedDateTime result = ZonedDateTime.now(ZoneId.of("Europe/Kiev"))
                .withHour(hour)
                .withMinute(minute);

        if (isYesterday) {
            return result.minusDays(1);
        }

        return result;
    }

    @Override
    protected CompletableFuture<Optional<String>> extractImageURI(Post post) {
        return post == null ? CompletableFuture.completedFuture(Optional.empty())
                : extractArticlePage(URI.create(post.url()))
                .thenApply(document -> document == null ? Optional.empty() :
                        extractImageURL(document));
    }

    private Optional<String> extractImageURL(Document document) {
        return document.getElementsByTag("img")
                .stream()
                .filter(e -> e.hasAttr("src")
                        && e.hasAttr("alt")
                        && e.hasAttr("title")
                        && e.hasClass("img-fluid"))
                .map(e -> "https://finance.liga.net" + e.attr("src"))
                .findFirst();
    }
}
