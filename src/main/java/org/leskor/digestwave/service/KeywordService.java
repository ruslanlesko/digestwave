package org.leskor.digestwave.service;

import static java.util.Comparator.comparing;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import org.leskor.digestwave.model.MonthlyKeyword;
import org.leskor.digestwave.repository.MonthlyKeywordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class KeywordService {
    private final MonthlyKeywordRepository monthlyKeywordRepository;

    @Autowired
    public KeywordService(MonthlyKeywordRepository monthlyKeywordRepository) {
        this.monthlyKeywordRepository = monthlyKeywordRepository;
    }

    public List<String> fetchKeywords() {
        String currentMonthYear = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        List<MonthlyKeyword> keywords = monthlyKeywordRepository.findByMonthYearIs(currentMonthYear);
        return keywords.stream()
                .sorted(comparing(MonthlyKeyword::getCount).reversed())
                .limit(64)
                .map(MonthlyKeyword::getKeyword)
                .collect(Collectors.toList());
    }
}
