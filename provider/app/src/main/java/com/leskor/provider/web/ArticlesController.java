package com.leskor.provider.web;

import com.leskor.provider.entities.Article;
import com.leskor.provider.entities.ArticlePreview;
import com.leskor.provider.exceptions.NotFoundException;
import com.leskor.provider.services.ArticlesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@CrossOrigin(origins = "http://localhost:8091", maxAge = 3600)
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
    public List<Article> fetchArticles(@RequestParam(value = "topic", required = false, defaultValue = "") String topic) {
        return articlesService.fetchArticles(topic);
    }

    @GetMapping(value = "/v1/articles/{articleId}", produces = APPLICATION_JSON_VALUE)
    public Article fetchArticle(@PathVariable String articleId) {
        return articlesService.fetchArticleById(articleId).orElseThrow(NotFoundException::new);
    }

    @GetMapping(value = "/v1/articles/{articleId}/image", produces = MediaType.IMAGE_JPEG_VALUE)
    public @ResponseBody byte[] fetchImage(@PathVariable String articleId) {
        return articlesService.fetchCoverImageByArticleId(articleId).orElseThrow(NotFoundException::new);
    }

    @GetMapping(value = "/v1/preview/articles", produces = APPLICATION_JSON_VALUE)
    public List<ArticlePreview> fetchArticlePreviews(@RequestParam(value = "topic", required = false, defaultValue = "") String topic) {
        return articlesService.fetchArticlePreviews(topic);
    }
}
