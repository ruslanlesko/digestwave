package com.leskor.digestwave.service;

import static java.util.stream.Collectors.groupingBy;

import com.leskor.digestwave.model.Mention;
import com.leskor.digestwave.model.MentionPayload;
import com.leskor.digestwave.model.Sentiment;
import com.leskor.digestwave.repository.MentionRepository;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MentionService {
    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

    private final MentionRepository mentionRepository;
    
    @Autowired
    MentionService(MentionRepository mentionRepository) {
        this.mentionRepository = mentionRepository;
    }
    
    public List<MentionPayload> fetchMentions(String keyword) {
        List<Mention> mentions = mentionRepository.findByKeyword(keyword.toLowerCase(Locale.ROOT));

        return mentions.stream()
                .collect(groupingBy(
                        mention -> mention.publishedAt().atZone(ZoneOffset.UTC).toLocalDate(),
                        Collectors.toList()
                ))
                .entrySet().stream()
                .map(entry -> {
                    String formattedDate = entry.getKey().format(DATE_FORMAT);
                    List<MentionPayload.Instance> instances = entry.getValue().stream()
                            .map(mention -> new MentionPayload.Instance(
                                    mention.articleUrl(),
                                    parseSentiment(mention.sentiment())
                            ))
                            .toList();
                    return new MentionPayload(formattedDate, instances.size(), instances);
                })
                .sorted(Comparator.comparing(MentionPayload::publishedAt))
                .toList();
    }

    private static Sentiment parseSentiment(int sentimentValue) {
        return switch (Integer.signum(sentimentValue)) {
            case 1 -> Sentiment.POSITIVE;
            case -1 -> Sentiment.NEGATIVE;
            default -> Sentiment.NEUTRAL;
        };
    }
}
