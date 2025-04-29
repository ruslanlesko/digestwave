package org.leskor.digestwave.web;

import java.util.stream.Collectors;
import org.leskor.digestwave.service.KeywordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/keywords")
public class KeywordController {
    private final KeywordService keywordService;

    @Autowired
    public KeywordController(KeywordService keywordService) {
        this.keywordService = keywordService;
    }

    @GetMapping(produces = "text/html")
    public String getKeywords() {
        return keywordService.fetchKeywords().stream()
                .map(k -> String.format("<li>%s</li>", k))
                .collect(Collectors.joining());
    }
}
