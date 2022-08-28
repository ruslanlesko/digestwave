package com.leskor.scraper.sites.custom;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import com.leskor.scraper.entities.Post;
import com.leskor.scraper.entities.Region;
import com.leskor.scraper.entities.Topic;
import com.leskor.scraper.sites.Site;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.safety.Safelist;

import static java.time.Duration.ofSeconds;
import static java.util.stream.Collectors.joining;

public class EconomicnaPravda extends Site {
    private static final URI INDEX_URI = URI.create("https://www.epravda.com.ua/news/");
    private static final int POSTS_LIMIT = 10;

    public EconomicnaPravda(HttpClient httpClient) {
        super(INDEX_URI, null, "EPR", httpClient, ofSeconds(10), Topic.FINANCE, Region.UA, Set.of(), false);
    }

    @Override
    protected Charset charset() {
        return Charset.forName("Windows-1251");
    }

    @Override
    protected List<CompletableFuture<Post>> extractPostsBasedOnPage(String page) {
        Document document = Jsoup.parse(page, Parser.htmlParser());
        Element newsList = document.getElementsByClass("news_list").first();
        if (newsList == null) {
            logger.error("Failed to fetch news list for epravda");
            return List.of();
        }

        List<CompletableFuture<Post>> result = new ArrayList<>();
        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("d MMMM, uuuu", new Locale("uk", "UA"));
        LocalDate date = LocalDate.now();
        for (var div : newsList.getElementsByTag("div")) {
            if (div.hasClass("news__date")) {
                String raw = div.text();
                date = LocalDate.parse(raw, formatter);
                continue;
            }
            if (!div.hasClass("article")) {
                continue;
            }

            Element timeElement = div.getElementsByClass("article__time").first();
            Element titleElement = div.getElementsByClass("article__title").first();
            if (timeElement == null || titleElement == null) {
                continue;
            }

            Element linkElement = titleElement.getElementsByTag("a").first();
            if (linkElement == null || !linkElement.hasAttr("href")) {
                continue;
            }
            String title = linkElement.text();
            URI postURI = URI.create("https://www.epravda.com.ua" + linkElement.attr("href"));

            LocalTime time = LocalTime.parse(timeElement.text(), DateTimeFormatter.ofPattern("HH:mm"));

            ZonedDateTime publicationTime = ZonedDateTime.of(date, time, ZoneId.of("Europe/Kiev"));

            result.add(fetchPost(postURI, title, publicationTime));
            if (result.size() >= POSTS_LIMIT) {
                break;
            }
        }

        return result;
    }

    private CompletableFuture<Post> fetchPost(URI postURI, String title, ZonedDateTime publicationTime) {
        return fetchHtmlForPost(postURI)
                .thenApply(d -> d == null ? null : createPost(d, title, postURI, publicationTime));
    }

    private CompletableFuture<Document> fetchHtmlForPost(URI postURI) {
        return httpClient.sendAsync(buildPageRequest(postURI), BodyHandlers.ofString(charset()))
                .thenApply(response -> {
                    if (response.statusCode() != 200) {
                        logger.error("Failed to fetch article, status {}", response.statusCode());
                        return null;
                    }
                    String body = response.body();
                    if (body == null || body.isBlank()) {
                        logger.warn("Cannot parse article response, body is blank");
                        return null;
                    }

                    return Jsoup.parse(response.body(), Parser.htmlParser());
                });
    }

    private HttpRequest buildPageRequest(URI postURI) {
        return HttpRequest.newBuilder(postURI)
                .GET()
                .header("Content-Type", "text/html")
                .timeout(DEFAULT_TIMEOUT)
                .build();
    }

    private Post createPost(Document document, String title, URI postURI, ZonedDateTime publicationTime) {
        String html = extractHtml(document);
        String text = extractTextContent(document);
        String imageURI = extractImageUri(document);
        return Post.from(siteCode, topic, region, publicationTime, title, text, html, postURI, imageURI);
    }

    private String extractHtml(Document document) {
        Element postWrapper = document.getElementsByClass("post__text").first();
        if (postWrapper == null) {
            return "";
        }
        return Jsoup.clean(postWrapper.html(), Safelist.basicWithImages());
    }

    private String extractTextContent(Document document) {
        Element postWrapper = document.getElementsByClass("post__text").first();
        if (postWrapper == null) {
            return "";
        }

        return postWrapper.getElementsByTag("p").stream()
                .map(e -> Jsoup.clean(e.html(), Safelist.none()))
                .collect(joining("\n"));
    }

    private String extractImageUri(Document document) {
        Element divElement = document.getElementsByClass("image-box_center").first();
        if (divElement == null) {
            return "";
        }
        Element imageElement = document.getElementsByTag("img").first();
        if (imageElement == null || !imageElement.hasAttr("src")) {
            return "";
        }
        return imageElement.attr("src");
    }
}
