package com.leskor.digestwave.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.leskor.digestwave.model.Article;
import com.leskor.digestwave.model.Mention;
import com.leskor.digestwave.model.MentionPayload;
import com.leskor.digestwave.model.Sentiment;
import com.leskor.digestwave.repository.MentionRepository;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.IntStream;
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
                        now.plusDays(1).atZone(ZoneOffset.UTC)), keyword, Sentiment.NEUTRAL));

        when(mentionRepository.findByKeyword(keyword.toLowerCase(Locale.ROOT))).thenReturn(mentions);

        List<MentionPayload> result = mentionService.fetchMentions(keyword, null, null);

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

    @Test
    void fetchMentions_moreThanMaxResults_shouldTrimOutput() {
        String keyword = "Technology";
        LocalDate baseDate = LocalDate.of(2024, 1, 1);
        List<Mention> mentions = IntStream.range(0, 1_200)
                .mapToObj(i -> Mention.of(
                        new Article(
                                URI.create("https://site-" + i + ".com"),
                                "title" + i,
                                baseDate.plusDays(i).atStartOfDay().atZone(ZoneOffset.UTC)),
                        keyword,
                        Sentiment.NEUTRAL))
                .toList();

        when(mentionRepository.findByKeyword(keyword.toLowerCase(Locale.ROOT))).thenReturn(mentions);

        List<MentionPayload> result = mentionService.fetchMentions(keyword, null, null);

        assertEquals(1_000, result.size());
    }

    @Test
    void fetchMentions_withDateRange_shouldUseRangeQuery() {
        String keyword = "finance";
        LocalDate start = LocalDate.of(2023, 9, 1);
        LocalDate end = start.plusDays(1);
        Mention mention = Mention.of(
                new Article(URI.create("https://range-test.com"), "title",
                        start.atStartOfDay().atZone(ZoneOffset.UTC)),
                keyword,
                Sentiment.POSITIVE);

        when(mentionRepository.findByKeywordAndPublishedAtGreaterThanEqualAndPublishedAtLessThan(
                keyword.toLowerCase(Locale.ROOT),
                start.atStartOfDay().toInstant(ZoneOffset.UTC),
                end.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC))).thenReturn(List.of(mention));

        List<MentionPayload> result = mentionService.fetchMentions(keyword, start, end);

        assertEquals(1, result.size());
        verify(mentionRepository).findByKeywordAndPublishedAtGreaterThanEqualAndPublishedAtLessThan(
                keyword.toLowerCase(Locale.ROOT),
                start.atStartOfDay().toInstant(ZoneOffset.UTC),
                end.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC));
    }
}
