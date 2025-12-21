package com.leskor.digestwave.service;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

import com.leskor.digestwave.cache.ArticleCache;
import com.leskor.digestwave.model.Article;
import com.leskor.digestwave.model.Mention;
import com.leskor.digestwave.repository.MentionRepository;
import com.leskor.digestwave.repository.MonthlyKeywordRepository;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ArticleProcessor {
    private final MetadataExtractor metadataExtractor;
    private final MentionRepository mentionRepository;
    private final MonthlyKeywordRepository monthlyKeywordRepository;
    private final ArticleCache articleCache;

    public ArticleProcessor(
            MetadataExtractor metadataExtractor,
            MentionRepository mentionRepository,
            MonthlyKeywordRepository monthlyKeywordRepository,
            ArticleCache articleCache) {
        this.metadataExtractor = metadataExtractor;
        this.mentionRepository = mentionRepository;
        this.monthlyKeywordRepository = monthlyKeywordRepository;
        this.articleCache = articleCache;
    }

    public void processArticle(Article article) {
        var metadata = metadataExtractor.extractMetadata(article.title());

        List<Mention> mentions = metadata.keywords().stream()
                .map(keyword -> Mention.of(article, keyword, metadata.sentiment()))
                .toList();

        String monthYear = LocalDate.ofInstant(
                article.publishedAt().toInstant(),
                ZoneOffset.UTC)
                .format(DateTimeFormatter.ofPattern("yyyy-MM"));

        metadata.keywords().stream()
                .collect(groupingBy(identity(), counting()))
                .forEach((keyword, count) -> monthlyKeywordRepository.incrementCount(monthYear, keyword,
                        count));

        mentionRepository.saveAll(mentions);

        articleCache.save(article);
    }
}
