package org.leskor.digestwave.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.leskor.digestwave.model.MonthlyKeyword;
import org.leskor.digestwave.repository.MonthlyKeywordRepository;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class KeywordServiceTest {

    @Mock
    private MonthlyKeywordRepository repository;

    private KeywordService keywordService;

    @BeforeEach
    void setUp() {
        keywordService = new KeywordService(repository);
    }

    @Test
    void fetchKeywords_shouldReturnSortedKeywords() {
        String monthYear = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        List<MonthlyKeyword> keywords = List.of(
                MonthlyKeyword.of(monthYear, "keyword1", 10),
                MonthlyKeyword.of(monthYear, "keyword2", 5),
                MonthlyKeyword.of(monthYear, "keyword3", 20)
        );

        when(repository.findByMonthYearIs(monthYear)).thenReturn(keywords);

        List<String> result = keywordService.fetchKeywords();

        assertThat(result).containsExactly("keyword3", "keyword1", "keyword2");
    }
}
