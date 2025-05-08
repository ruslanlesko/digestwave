package com.leskor.digestwave.web;

import static org.assertj.core.api.Assertions.assertThat;

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
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.cassandra.CassandraContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Tag("integration")
public class MentionControllerTest {
    @Container
    private static final CassandraContainer cassandraContainer =
            new CassandraContainer("cassandra:5.0").withInitScript("init.cql");

    private final LocalDateTime now = LocalDateTime.now();

    @DynamicPropertySource
    static void cassandraProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.cassandra.contact-points", cassandraContainer::getHost);
        registry.add("spring.cassandra.port", () -> cassandraContainer.getMappedPort(9042));
        registry.add("spring.cassandra.username", cassandraContainer::getUsername);
        registry.add("spring.cassandra.password", cassandraContainer::getPassword);
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private MentionRepository mentionRepository;

    @BeforeEach
    void setUp() {
        mentionRepository.save(Mention.of(new Article(URI.create("https://some-site.com"), "title1",
                now.atZone(ZoneOffset.UTC)), "World Cup", Sentiment.POSITIVE));
        mentionRepository.save(
                Mention.of(new Article(URI.create("https://some-other-site.com"), "title2",
                        now.plusSeconds(1).atZone(ZoneOffset.UTC)), "World Cup", Sentiment.NEGATIVE));
        mentionRepository.save(
                Mention.of(new Article(URI.create("https://different-site.com"), "title3",
                        now.plusDays(1).atZone(ZoneOffset.UTC)), "World Cup", Sentiment.NEUTRAL));
    }

    @Test
    void getMentions_shouldReturnMentionsSortedByPublishingDate() {
        String keyword = "World Cup";
        ResponseEntity<MentionPayload[]> result =
                restTemplate.getForEntity("/api/mentions/" + keyword, MentionPayload[].class);

        String todayDate = now.atZone(ZoneOffset.UTC).format(DateTimeFormatter.ISO_LOCAL_DATE);
        String tomorrowDate =
                now.plusDays(1).atZone(ZoneOffset.UTC).format(DateTimeFormatter.ISO_LOCAL_DATE);

        assertThat(result.getBody()).isEqualTo(new MentionPayload[] {
                new MentionPayload(todayDate, 2, List.of(
                        new MentionPayload.Instance("https://some-site.com", Sentiment.POSITIVE),
                        new MentionPayload.Instance("https://some-other-site.com",
                                Sentiment.NEGATIVE))),
                new MentionPayload(tomorrowDate, 1, List.of(
                        new MentionPayload.Instance("https://different-site.com",
                                Sentiment.NEUTRAL)))
        });
    }
}
