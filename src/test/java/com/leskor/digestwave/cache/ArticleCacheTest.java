package com.leskor.digestwave.cache;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.leskor.digestwave.model.Article;
import java.net.URI;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class ArticleCacheTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private ArticleCache cache;

    @BeforeEach
    void setUp() {
        cache = new ArticleCache(redisTemplate);
    }

    @Test
    void saveStoresPublishedDateForThirtyDays() {
        Article article = mock(Article.class);
        URI uri = URI.create("https://example.com/a");
        ZonedDateTime publishedAt = ZonedDateTime.of(2024, 1, 10, 0, 0, 0, 0, ZonedDateTime.now().getZone());

        when(article.uri()).thenReturn(uri);
        when(article.publishedAt()).thenReturn(publishedAt);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        cache.save(article);

        verify(valueOperations).set(
                eq(uri.toString()),
                eq(publishedAt.format(DateTimeFormatter.ISO_LOCAL_DATE)),
                eq(Duration.ofDays(30)));
    }

    @Test
    void existsDelegatesToRedisTemplate() {
        Article article = mock(Article.class);
        String uri = "https://example.com/b";
        when(article.uri()).thenReturn(URI.create(uri));
        when(redisTemplate.hasKey(uri)).thenReturn(true);

        assertTrue(cache.exists(article));
        verify(redisTemplate).hasKey(uri);
    }
}
