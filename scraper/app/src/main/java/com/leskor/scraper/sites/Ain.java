package com.leskor.scraper.sites;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class Ain {
    private static final Logger logger = LoggerFactory.getLogger("Application");

    private static final URI HOME_PAGE_URI = URI.create("https://ain.ua");
    private static final String MOZILLA_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:93.0) Gecko/20100101 Firefox/93.0";

    private final HttpClient httpClient;

    public Ain(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public void process() {
        HttpRequest request = buildRequest();
        try {
            HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() != 200) {
                logger.warn("Cannot process, status {}", response.statusCode());
                return;
            }

            List<URI> postURIs = extractPostURIsfromPage(response.body());
            postURIs.forEach(uri -> logger.debug(uri.toString()));
        } catch (IOException e) {
            logger.error("IO failed", e);
        } catch (InterruptedException e) {
            logger.error("Interrupted Exception", e);
        }
    }

    private HttpRequest buildRequest() {
        return HttpRequest.newBuilder(HOME_PAGE_URI)
                .setHeader("User-Agent", MOZILLA_AGENT)
                .timeout(Duration.ofSeconds(10))
                .build();
    }

    private List<URI> extractPostURIsfromPage(String page) {
        Document document = Jsoup.parse(page);
        var elements = document.getElementsByClass("post-item ordinary-post");
        int limit = 10;
        List<URI> result = new ArrayList<>();
        for (var postElement : elements) {
            Element link = postElement.getElementsByClass("post-link").first();
            if (link == null) continue;

            limit--;

            if (link.getElementsByTag("svg").size() > 0) continue;

            try {
                result.add(URI.create(link.attr("href")));
            } catch (IllegalArgumentException e) {
                logger.warn("Cannot parse URL", e);
            }
            if (limit <= 0) break;
        }
        return result;
    }
}
