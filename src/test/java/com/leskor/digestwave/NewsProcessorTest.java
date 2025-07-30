package com.leskor.digestwave;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.leskor.digestwave.cache.Bookkeeper;
import com.leskor.digestwave.config.FeedProperties;
import com.leskor.digestwave.model.Article;
import com.leskor.digestwave.service.ArticleProcessor;
import com.leskor.digestwave.service.FeedLoader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
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
    private Bookkeeper bookkeeper;
    @Mock
    private ArticleProcessor articleProcessor;

    private NewsProcessor newsProcessor;

    @BeforeEach
    void setUp() {
        newsProcessor = new NewsProcessor(feedProperties, feedLoader, bookkeeper, articleProcessor);
    }

    @Test
    void process_shouldProcessAllConfiguredUrls() throws Exception {
        String url1 = "https://thenextweb.com/feed";
        String url2 = "https://feed.infoq.com/";
        List<String> urls = List.of(url1, url2);

        URI uri1 = new URI(url1);
        URI uri2 = new URI(url2);

        Instant lastFetchTime1 = Instant.parse("2025-07-29T10:00:00Z");
        Instant lastFetchTime2 = Instant.parse("2025-07-29T11:00:00Z");

        Article article1 = new Article(uri1, "First Article Title",
                ZonedDateTime.parse("2025-07-30T10:00:00Z"));
        Article article2 = new Article(uri2, "Second Article Title",
                ZonedDateTime.parse("2025-07-30T11:00:00Z"));

        Instant publishTime1 = article1.publishedAt().toInstant();
        Instant publishTime2 = article2.publishedAt().toInstant();

        InputStream mockStream1 = new ByteArrayInputStream("firstFeed".getBytes());
        InputStream mockStream2 = new ByteArrayInputStream("secondFeed".getBytes());

        when(feedProperties.urls()).thenReturn(urls);
        when(bookkeeper.lastFetchTime(uri1)).thenReturn(Optional.of(lastFetchTime1));
        when(bookkeeper.lastFetchTime(uri2)).thenReturn(Optional.of(lastFetchTime2));
        when(feedLoader.loadArticles(any(InputStream.class), eq(lastFetchTime1)))
                .thenReturn(List.of(article1));
        when(feedLoader.loadArticles(any(InputStream.class), eq(lastFetchTime2)))
                .thenReturn(List.of(article2));
        when(articleProcessor.processArticle(article1)).thenReturn(publishTime1);
        when(articleProcessor.processArticle(article2)).thenReturn(publishTime2);

        try (MockedStatic<URL> urlMock = mockStatic(URL.class)) {
            URL mockUrl1 = mock(URL.class);
            URL mockUrl2 = mock(URL.class);

            urlMock.when(uri1::toURL).thenReturn(mockUrl1);
            urlMock.when(uri2::toURL).thenReturn(mockUrl2);
            when(mockUrl1.openStream()).thenReturn(mockStream1);
            when(mockUrl2.openStream()).thenReturn(mockStream2);

            newsProcessor.process();
        }

        verify(bookkeeper).lastFetchTime(uri1);
        verify(bookkeeper).lastFetchTime(uri2);
        verify(articleProcessor).processArticle(article1);
        verify(articleProcessor).processArticle(article2);
        verify(bookkeeper).saveFetchTime(uri1, publishTime1);
        verify(bookkeeper).saveFetchTime(uri2, publishTime2);
    }

    @Test
    void process_shouldUseEpochWhenNoLastFetchTime() throws Exception {
        String url = "https://thenextweb.com/feed";
        List<String> urls = List.of(url);
        URI uri = new URI(url);

        Article article =
                new Article(uri, "Article Title", ZonedDateTime.parse("2025-07-30T10:00:00Z"));
        Instant publishTime = article.publishedAt().toInstant();

        InputStream mockStream = new ByteArrayInputStream("feed".getBytes());

        when(feedProperties.urls()).thenReturn(urls);
        when(bookkeeper.lastFetchTime(uri)).thenReturn(Optional.empty());
        when(feedLoader.loadArticles(any(InputStream.class), eq(Instant.EPOCH)))
                .thenReturn(List.of(article));
        when(articleProcessor.processArticle(article)).thenReturn(publishTime);

        try (MockedStatic<URL> urlMock = mockStatic(URL.class)) {
            URL mockUrl = mock(URL.class);
            urlMock.when(uri::toURL).thenReturn(mockUrl);
            when(mockUrl.openStream()).thenReturn(mockStream);

            newsProcessor.process();
        }

        verify(feedLoader).loadArticles(any(InputStream.class), eq(Instant.EPOCH));
        verify(bookkeeper).saveFetchTime(uri, publishTime);
    }

    @Test
    void process_shouldNotSaveFetchTimeWhenNoArticles() throws Exception {
        String url = "https://thenextweb.com/feed";
        List<String> urls = List.of(url);
        URI uri = new URI(url);

        InputStream mockStream = new ByteArrayInputStream("feed".getBytes());

        when(feedProperties.urls()).thenReturn(urls);
        when(bookkeeper.lastFetchTime(uri)).thenReturn(Optional.of(Instant.EPOCH));
        when(feedLoader.loadArticles(any(InputStream.class), eq(Instant.EPOCH)))
                .thenReturn(List.of());

        try (MockedStatic<URL> urlMock = mockStatic(URL.class)) {
            URL mockUrl = mock(URL.class);
            urlMock.when(uri::toURL).thenReturn(mockUrl);
            when(mockUrl.openStream()).thenReturn(mockStream);

            newsProcessor.process();
        }

        verify(feedLoader).loadArticles(any(InputStream.class), eq(Instant.EPOCH));
        verifyNoInteractions(articleProcessor);
        verify(bookkeeper, times(0)).saveFetchTime(any(URI.class), any(Instant.class));
    }

    @Test
    void process_shouldSaveLatestPublishTimeWhenMultipleArticles() throws Exception {
        String url = "https://thenextweb.com/feed";
        List<String> urls = List.of(url);
        URI uri = new URI(url);

        Article article1 = new Article(uri, "First Article Title",
                ZonedDateTime.parse("2025-07-30T10:00:00Z"));
        Article article2 = new Article(uri, "Second Article Title",
                ZonedDateTime.parse("2025-07-30T12:00:00Z"));
        Article article3 = new Article(uri, "Third Article Title",
                ZonedDateTime.parse("2025-07-30T11:00:00Z"));

        Instant publishTime1 = article1.publishedAt().toInstant();
        Instant publishTime2 = article2.publishedAt().toInstant();
        Instant publishTime3 = article3.publishedAt().toInstant();

        InputStream mockStream = new ByteArrayInputStream("feed".getBytes());

        when(feedProperties.urls()).thenReturn(urls);
        when(bookkeeper.lastFetchTime(uri)).thenReturn(Optional.of(Instant.EPOCH));
        when(feedLoader.loadArticles(any(InputStream.class), eq(Instant.EPOCH)))
                .thenReturn(List.of(article1, article2, article3));
        when(articleProcessor.processArticle(article1)).thenReturn(publishTime1);
        when(articleProcessor.processArticle(article2)).thenReturn(publishTime2);
        when(articleProcessor.processArticle(article3)).thenReturn(publishTime3);

        try (MockedStatic<URL> urlMock = mockStatic(URL.class)) {
            URL mockUrl = mock(URL.class);
            urlMock.when(uri::toURL).thenReturn(mockUrl);
            when(mockUrl.openStream()).thenReturn(mockStream);

            newsProcessor.process();
        }

        verify(articleProcessor).processArticle(article1);
        verify(articleProcessor).processArticle(article2);
        verify(articleProcessor).processArticle(article3);
        verify(bookkeeper).saveFetchTime(uri, publishTime2);
    }
}

