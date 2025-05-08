package com.leskor.digestwave.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.leskor.digestwave.model.Article;
import com.leskor.digestwave.model.Mention;
import com.leskor.digestwave.model.MentionPayload;
import com.leskor.digestwave.model.Sentiment;
import com.leskor.digestwave.repository.MentionRepository;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MentionServiceTest {
    @Mock
    private MentionRepository mentionRepository;

    private MentionService mentionService;

    @BeforeEach
    void setUp() {
        mentionService = new MentionService(mentionRepository);
    }

    @Test
    void fetchMentions_multipleMentionsForDay_shouldReturnAggregated() {
        String keyword = "Automotive";
        LocalDateTime now = LocalDateTime.now();
        List<Mention> mentions = List.of(
                Mention.of(new Article(URI.create("https://some-site.com"), "title1",
                        now.atZone(ZoneOffset.UTC)), keyword, Sentiment.POSITIVE),
                Mention.of(new Article(URI.create("https://some-other-site.com"), "title2",
                        now.atZone(ZoneOffset.UTC)), keyword, Sentiment.NEGATIVE),
                Mention.of(new Article(URI.create("https://different-site.com"), "title3",
                        now.plusDays(1).atZone(ZoneOffset.UTC)), keyword, Sentiment.NEUTRAL)
        );

        when(mentionRepository.findByKeyword(keyword)).thenReturn(mentions);

        List<MentionPayload> result = mentionService.fetchMentions(keyword);

        assertEquals(2, result.size());

        MentionPayload firstDayPayload = result.getFirst();
        assertEquals(now.format(DateTimeFormatter.ISO_LOCAL_DATE), firstDayPayload.publishedAt());
        assertEquals(2, firstDayPayload.count());
        assertEquals(2, firstDayPayload.instances().size());
        assertEquals(Sentiment.POSITIVE, firstDayPayload.instances().get(0).sentiment());
        assertEquals(Sentiment.NEGATIVE, firstDayPayload.instances().get(1).sentiment());

        MentionPayload secondDayPayload = result.get(1);
        assertEquals(now.plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE), secondDayPayload.publishedAt());
        assertEquals(1, secondDayPayload.count());
        assertEquals(1, secondDayPayload.instances().size());
        assertEquals(Sentiment.NEUTRAL, secondDayPayload.instances().get(0).sentiment());
    }
}
