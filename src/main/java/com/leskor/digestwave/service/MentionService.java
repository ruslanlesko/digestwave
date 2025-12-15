package com.leskor.digestwave.service;

import static java.util.stream.Collectors.groupingBy;

import com.leskor.digestwave.model.Mention;
import com.leskor.digestwave.model.MentionPayload;
import com.leskor.digestwave.model.Sentiment;
import com.leskor.digestwave.repository.MentionRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class MentionService {
    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final int MAX_RESULTS = 1_000;

    private final MentionRepository mentionRepository;

    MentionService(MentionRepository mentionRepository) {
        this.mentionRepository = mentionRepository;
    }

    public List<MentionPayload> fetchMentions(String keyword, LocalDate startDate, LocalDate endDate) {
        List<Mention> mentions = loadMentions(keyword.toLowerCase(Locale.ROOT), startDate, endDate);

        return mentions.stream()
                .collect(groupingBy(
                        mention -> mention.publishedAt().atZone(ZoneOffset.UTC).toLocalDate(),
                        Collectors.toList()))
                .entrySet().stream()
                .map(entry -> {
                    String formattedDate = entry.getKey().format(DATE_FORMAT);
                    List<MentionPayload.Instance> instances = entry.getValue().stream()
                            .map(mention -> new MentionPayload.Instance(
                                    mention.articleUrl(),
                                    parseSentiment(mention.sentiment())))
                            .toList();
                    return new MentionPayload(formattedDate, instances.size(), instances);
                })
                .sorted(Comparator.comparing(MentionPayload::publishedAt))
                .limit(MAX_RESULTS)
                .toList();
    }

    private List<Mention> loadMentions(String keyword, LocalDate startDate, LocalDate endDate) {
        Instant startInstant = startDate != null
                ? startDate.atStartOfDay().toInstant(ZoneOffset.UTC)
                : null;
        Instant endExclusive = endDate != null
                ? endDate.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC)
                : null;

        if (startInstant != null && endExclusive != null) {
            return mentionRepository.findByKeywordAndPublishedAtGreaterThanEqualAndPublishedAtLessThan(
                    keyword,
                    startInstant,
                    endExclusive);
        }
        if (startInstant != null) {
            return mentionRepository.findByKeywordAndPublishedAtGreaterThanEqual(keyword, startInstant);
        }
        if (endExclusive != null) {
            return mentionRepository.findByKeywordAndPublishedAtLessThan(keyword, endExclusive);
        }
        return mentionRepository.findByKeyword(keyword);
    }

    private static Sentiment parseSentiment(int sentimentValue) {
        return switch (Integer.signum(sentimentValue)) {
            case 1 -> Sentiment.POSITIVE;
            case -1 -> Sentiment.NEGATIVE;
            default -> Sentiment.NEUTRAL;
        };
    }
}
