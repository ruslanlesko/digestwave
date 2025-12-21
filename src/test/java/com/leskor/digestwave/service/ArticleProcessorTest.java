package com.leskor.digestwave.service;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.leskor.digestwave.cache.ArticleCache;
import com.leskor.digestwave.model.Article;
import com.leskor.digestwave.model.Sentiment;
import com.leskor.digestwave.repository.MentionRepository;
import com.leskor.digestwave.repository.MonthlyKeywordRepository;
import java.net.URI;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ArticleProcessorTest {

    @Mock
    private MetadataExtractor metadataExtractor;

    @Mock
    private MentionRepository mentionRepository;

    @Mock
    private MonthlyKeywordRepository monthlyKeywordRepository;

    @Mock
    private ArticleCache articleCache;

    private ArticleProcessor articleProcessor;

    @BeforeEach
    void setUp() {
        articleProcessor = new ArticleProcessor(metadataExtractor, mentionRepository, monthlyKeywordRepository,
                articleCache);
    }

    @Test
    void processArticle_shouldProcessArticleCorrectly() {
        Instant published = Instant.parse("2024-03-15T10:00:00Z");
        Article article = new Article(
                URI.create("https://example.com/article/1"),
                "Test Article",
                ZonedDateTime.ofInstant(published, ZoneOffset.UTC));

        var metadata = new MetadataExtractor.Metadata(Set.of("AI", "Technology"), Sentiment.POSITIVE);

        when(metadataExtractor.extractMetadata("Test Article")).thenReturn(metadata);

        articleProcessor.processArticle(article);

        verify(mentionRepository).saveAll(anyList());
        verify(articleCache).save(article);
        verify(monthlyKeywordRepository).incrementCount("2024-03", "AI", 1L);
        verify(monthlyKeywordRepository).incrementCount("2024-03", "Technology", 1L);
    }
}
