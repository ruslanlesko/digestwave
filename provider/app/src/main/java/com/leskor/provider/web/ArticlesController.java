package com.leskor.provider.web;

import com.leskor.provider.entities.Article;
import com.leskor.provider.entities.ArticlePreview;
import com.leskor.provider.services.ArticlesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
public class ArticlesController {
    private final ArticlesService articlesService;

    @Autowired
    public ArticlesController(ArticlesService articlesService) {
        this.articlesService = articlesService;
    }

    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }

    @GetMapping(value = "/v1/articles", produces = APPLICATION_JSON_VALUE)
    public List<Article> fetchArticles() {
        return articlesService.fetchArticles();
    }

    @GetMapping(value = "/v1/preview/articles", produces = APPLICATION_JSON_VALUE)
    public List<ArticlePreview> fetchArticlePreviews() {
        return articlesService.fetchArticlePreviews();
    }
}
