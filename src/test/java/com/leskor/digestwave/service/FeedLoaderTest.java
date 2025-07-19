package com.leskor.digestwave.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.leskor.digestwave.model.Article;

class FeedLoaderTest {
    private FeedLoader feedLoader;

    @BeforeEach
    void setUp() {
        feedLoader = new FeedLoader();
    }

    @Test
    void loadArticles_returnsArticlesAfterGivenInstant() throws IOException {
        String rss = """
                <?xml version="1.0" encoding="UTF-8"?><rss version="2.0"
                	xmlns:content="http://purl.org/rss/1.0/modules/content/"
                	xmlns:wfw="http://wellformedweb.org/CommentAPI/"
                	xmlns:dc="http://purl.org/dc/elements/1.1/"
                	xmlns:atom="http://www.w3.org/2005/Atom"
                	xmlns:sy="http://purl.org/rss/1.0/modules/syndication/"
                	xmlns:slash="http://purl.org/rss/1.0/modules/slash/"
                	>
                
                <channel>
                	<title>TechCrunch</title>
                	<atom:link href="https://techcrunch.com/feed/" rel="self" type="application/rss+xml" />
                	<link>https://techcrunch.com/</link>
                	<description>Startup and Technology News</description>
                	<lastBuildDate>Mon, 07 Apr 2025 19:33:27 +0000</lastBuildDate>
                	<language>en-US</language>
                	<item>
                		<title>Meta exec denies the company artificially boosted Llama 4&#8217;s benchmark scores</title>
                		<link>https://techcrunch.com/2025/04/07/meta-exec-denies-the-company-artificially-boosted-llama-4s-benchmark-scores/</link>
                		<dc:creator><![CDATA[Kyle Wiggers]]></dc:creator>
                		<pubDate>Mon, 07 Apr 2025 18:45:07 +0000</pubDate>
                		<category><![CDATA[Meta]]></category>
                		<guid isPermaLink="false">https://techcrunch.com/?p=2990400</guid>
                	</item>
                    <item>
                        <title>Former Tesla exec Drew Baglino’s new startup is rethinking the electrical transformer</title>
                        <link>https://techcrunch.com/2025/04/07/former-tesla-exec-drew-baglinos-new-startup-is-rethinking-the-electrical-transformer/</link>
                        <dc:creator><![CDATA[Tim De Chant]]></dc:creator>
                        <pubDate>Sat, 05 Apr 2025 17:54:48 +0000</pubDate>
                        <category><![CDATA[Tesla]]></category>
                        <guid isPermaLink="false">https://techcrunch.com/?p=2990345</guid>
                        <description><![CDATA[Heron Power is raising between $30 million to $50 million for a Series A, according to a report.]]></description>
                    </item>
                	</channel>
                </rss>
                """;
        InputStream is = new ByteArrayInputStream(rss.getBytes(StandardCharsets.UTF_8));

        Instant from = Instant.parse("2025-04-06T00:00:00Z");

        List<Article> articles = feedLoader.loadArticles(is, from);

        assertEquals(1, articles.size());
        assertIterableEquals(List.of(new Article(URI.create(
                "https://techcrunch.com/2025/04/07/meta-exec-denies-the-company-artificially-boosted-llama-4s-benchmark-scores/"),
                "Meta exec denies the company artificially boosted Llama 4’s benchmark scores",
                ZonedDateTime.parse("2025-04-07T18:45:07Z"))), articles);
    }

    @Test
    void loadArticles_filtersOutArticlesWithoutPublishedTime() throws IOException {
        String rss = """
                <?xml version="1.0" encoding="UTF-8"?><rss version="2.0"
                	xmlns:content="http://purl.org/rss/1.0/modules/content/"
                	xmlns:wfw="http://wellformedweb.org/CommentAPI/"
                	xmlns:dc="http://purl.org/dc/elements/1.1/"
                	xmlns:atom="http://www.w3.org/2005/Atom"
                	xmlns:sy="http://purl.org/rss/1.0/modules/syndication/"
                	xmlns:slash="http://purl.org/rss/1.0/modules/slash/"
                	>
                
                <channel>
                	<title>TechCrunch</title>
                	<atom:link href="https://techcrunch.com/feed/" rel="self" type="application/rss+xml" />
                	<link>https://techcrunch.com/</link>
                	<description>Startup and Technology News</description>
                	<lastBuildDate>Mon, 07 Apr 2025 19:33:27 +0000</lastBuildDate>
                	<language>en-US</language>
                	<item>
                		<title>Meta exec denies the company artificially boosted Llama 4&#8217;s benchmark scores</title>
                		<link>https://techcrunch.com/2025/04/07/meta-exec-denies-the-company-artificially-boosted-llama-4s-benchmark-scores/</link>
                		<dc:creator><![CDATA[Kyle Wiggers]]></dc:creator>
                		<category><![CDATA[Meta]]></category>
                		<guid isPermaLink="false">https://techcrunch.com/?p=2990400</guid>
                	</item>
                    <item>
                        <title>Former Tesla exec Drew Baglino’s new startup is rethinking the electrical transformer</title>
                        <link>https://techcrunch.com/2025/04/07/former-tesla-exec-drew-baglinos-new-startup-is-rethinking-the-electrical-transformer/</link>
                        <dc:creator><![CDATA[Tim De Chant]]></dc:creator>
                        <pubDate>Sat, 05 Apr 2025 17:54:48 +0000</pubDate>
                        <category><![CDATA[Tesla]]></category>
                        <guid isPermaLink="false">https://techcrunch.com/?p=2990345</guid>
                        <description><![CDATA[Heron Power is raising between $30 million to $50 million for a Series A, according to a report.]]></description>
                    </item>
                	</channel>
                </rss>
                """;
        InputStream is = new ByteArrayInputStream(rss.getBytes(StandardCharsets.UTF_8));

        List<Article> articles = feedLoader.loadArticles(is, Instant.EPOCH);

        assertEquals(1, articles.size());
        assertIterableEquals(List.of(new Article(URI.create(
                "https://techcrunch.com/2025/04/07/former-tesla-exec-drew-baglinos-new-startup-is-rethinking-the-electrical-transformer/"),
                "Former Tesla exec Drew Baglino’s new startup is rethinking the electrical transformer",
                ZonedDateTime.parse("2025-04-05T17:54:48Z"))), articles);
    }

    @Test
    void loadArticles_throwsIOExceptionOnFeedException() {
        String unknownFeedFormat = "<unknown>";
        InputStream is = new ByteArrayInputStream(unknownFeedFormat.getBytes(StandardCharsets.UTF_8));

        assertThrows(IOException.class, () -> feedLoader.loadArticles(is, Instant.EPOCH));
    }
}
