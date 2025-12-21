package com.leskor.digestwave;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.leskor.digestwave.cache.ArticleCache;
import com.leskor.digestwave.config.FeedProperties;
import com.leskor.digestwave.model.Article;
import com.leskor.digestwave.service.ArticleProcessor;
import com.leskor.digestwave.service.FeedLoader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NewsProcessorTest {

    @Mock
    private FeedProperties feedProperties;
    @Mock
    private FeedLoader feedLoader;
    @Mock
    private ArticleProcessor articleProcessor;
    @Mock
    private ArticleCache articleCache;

    private NewsProcessor newsProcessor;

    @BeforeEach
    void setUp() {
        newsProcessor = new NewsProcessor(feedProperties, feedLoader, articleProcessor, articleCache);
    }

    @Test
    void process_shouldProcessAllConfiguredUrls() throws Exception {
        String url1 = "https://thenextweb.com/feed";
        String url2 = "https://feed.infoq.com/";
        List<String> urls = List.of(url1, url2);

        URI uri1 = new URI(url1);
        URI uri2 = new URI(url2);

        Article article1 = new Article(uri1, "First Article Title",
                ZonedDateTime.parse("2025-07-30T10:00:00Z"));
        Article article2 = new Article(uri2, "Second Article Title",
                ZonedDateTime.parse("2025-07-30T11:00:00Z"));

        InputStream mockStream1 = new ByteArrayInputStream("firstFeed".getBytes());
        InputStream mockStream2 = new ByteArrayInputStream("secondFeed".getBytes());

        when(feedProperties.urls()).thenReturn(urls);
        when(feedLoader.loadArticles(any(InputStream.class))).thenReturn(List.of(article1))
                .thenReturn(List.of(article2));

        try (MockedStatic<URL> urlMock = mockStatic(URL.class)) {
            URL mockUrl1 = mock(URL.class);
            URL mockUrl2 = mock(URL.class);

            urlMock.when(uri1::toURL).thenReturn(mockUrl1);
            urlMock.when(uri2::toURL).thenReturn(mockUrl2);
            when(mockUrl1.openStream()).thenReturn(mockStream1);
            when(mockUrl2.openStream()).thenReturn(mockStream2);

            newsProcessor.process();
        }

        verify(articleProcessor).processArticle(article1);
        verify(articleProcessor).processArticle(article2);
    }

    @Test
    void process_shouldUseEpochWhenNoLastFetchTime() throws Exception {
        String url = "https://thenextweb.com/feed";
        List<String> urls = List.of(url);
        URI uri = new URI(url);

        Article article = new Article(uri, "Article Title", ZonedDateTime.parse("2025-07-30T10:00:00Z"));

        InputStream mockStream = new ByteArrayInputStream("feed".getBytes());

        when(feedProperties.urls()).thenReturn(urls);
        when(feedLoader.loadArticles(any(InputStream.class))).thenReturn(List.of(article));

        try (MockedStatic<URL> urlMock = mockStatic(URL.class)) {
            URL mockUrl = mock(URL.class);
            urlMock.when(uri::toURL).thenReturn(mockUrl);
            when(mockUrl.openStream()).thenReturn(mockStream);

            newsProcessor.process();
        }

        verify(feedLoader).loadArticles(any(InputStream.class));
    }

    @Test
    void process_shouldNotSaveFetchTimeWhenNoArticles() throws Exception {
        String url = "https://thenextweb.com/feed";
        List<String> urls = List.of(url);
        URI uri = new URI(url);

        InputStream mockStream = new ByteArrayInputStream("feed".getBytes());

        when(feedProperties.urls()).thenReturn(urls);
        when(feedLoader.loadArticles(any(InputStream.class))).thenReturn(List.of());

        try (MockedStatic<URL> urlMock = mockStatic(URL.class)) {
            URL mockUrl = mock(URL.class);
            urlMock.when(uri::toURL).thenReturn(mockUrl);
            when(mockUrl.openStream()).thenReturn(mockStream);

            newsProcessor.process();
        }

        verify(feedLoader).loadArticles(any(InputStream.class));
        verifyNoInteractions(articleProcessor);
    }

    @Test
    void process_shouldSaveLatestPublishTimeWhenMultipleArticles() throws Exception {
        String url = "https://thennextweb.com/feed";
        List<String> urls = List.of(url);
        URI uri = new URI(url);

        Article article1 = new Article(uri, "First Article Title",
                ZonedDateTime.parse("2025-07-30T10:00:00Z"));
        Article article2 = new Article(uri, "Second Article Title",
                ZonedDateTime.parse("2025-07-30T12:00:00Z"));
        Article article3 = new Article(uri, "Third Article Title",
                ZonedDateTime.parse("2025-07-30T11:00:00Z"));

        InputStream mockStream = new ByteArrayInputStream("feed".getBytes());

        when(feedProperties.urls()).thenReturn(urls);
        when(feedLoader.loadArticles(any(InputStream.class))).thenReturn(List.of(article1, article2, article3));

        try (MockedStatic<URL> urlMock = mockStatic(URL.class)) {
            URL mockUrl = mock(URL.class);
            urlMock.when(uri::toURL).thenReturn(mockUrl);
            when(mockUrl.openStream()).thenReturn(mockStream);

            newsProcessor.process();
        }

        verify(articleProcessor).processArticle(article1);
        verify(articleProcessor).processArticle(article2);
        verify(articleProcessor).processArticle(article3);
    }

    @Test
    void process_shouldSkipArticlesAlreadyProcessed() throws Exception {
        String url = "https://thennextweb.com/article";
        URI uri = new URI(url);
        Article article = new Article(uri, "Cached Article", ZonedDateTime.parse("2025-07-30T10:00:00Z"));

        when(feedProperties.urls()).thenReturn(List.of(url));
        when(feedLoader.loadArticles(any(InputStream.class))).thenReturn(List.of(article));
        when(articleCache.exists(article)).thenReturn(true);

        InputStream mockStream = new ByteArrayInputStream("feed".getBytes());
        try (MockedStatic<URL> urlMock = mockStatic(URL.class)) {
            URL mockUrl = mock(URL.class);
            urlMock.when(uri::toURL).thenReturn(mockUrl);
            when(mockUrl.openStream()).thenReturn(mockStream);

            newsProcessor.process();
        }

        verify(articleCache).exists(article);
        verifyNoInteractions(articleProcessor);
    }
}
