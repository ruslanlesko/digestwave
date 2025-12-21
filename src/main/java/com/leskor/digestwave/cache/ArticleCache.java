package com.leskor.digestwave.cache;

import com.leskor.digestwave.model.Article;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ArticleCache {
    RedisTemplate<String, String> redisTemplate;

    public ArticleCache(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void save(Article article) {
        String uri = article.uri().toString();
        redisTemplate.opsForValue().set(uri, article.publishedAt().format(DateTimeFormatter.ISO_LOCAL_DATE),
                Duration.ofDays(30));
    }

    public boolean exists(Article article) {
        String uri = article.uri().toString();
        return redisTemplate.hasKey(uri);
    }
}
