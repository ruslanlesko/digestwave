package com.leskor.digestwave.web;

import static org.assertj.core.api.Assertions.assertThat;

import com.leskor.digestwave.repository.MonthlyKeywordRepository;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
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
public class KeywordControllerTest {

    @Container
    private static final CassandraContainer cassandraContainer =
            new CassandraContainer("cassandra:5.0").withInitScript("init.cql");

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
    private MonthlyKeywordRepository monthlyKeywordRepository;

    private final String currentMonthYear = LocalDate.now()
            .format(DateTimeFormatter.ofPattern("yyyy-MM"));

    @BeforeEach
    void setUp() {
        monthlyKeywordRepository.incrementCount(currentMonthYear, "keyword1", 10);
        monthlyKeywordRepository.incrementCount(currentMonthYear, "keyword2", 5);
        monthlyKeywordRepository.incrementCount(currentMonthYear, "keyword3", 20);
    }

    @Test
    void getKeywords_shouldReturnKeywordsSortedByCountDesc() {
        String result = restTemplate.getForObject("/api/keywords", String.class);
        assertThat(result).isEqualTo("<li>keyword3</li><li>keyword1</li><li>keyword2</li>");
    }
}
