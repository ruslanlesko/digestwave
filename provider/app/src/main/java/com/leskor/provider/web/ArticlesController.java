package com.leskor.provider.web;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.leskor.provider.entities.Article;
import com.leskor.provider.entities.ArticlePreview;
import com.leskor.provider.exceptions.NotFoundException;
import com.leskor.provider.services.ArticlesService;
import com.leskor.provider.services.TopArticlesService;

@CrossOrigin(origins = { "http://localhost:8090", "https://digestwave.com" }, maxAge = 3600)
@RestController
public class ArticlesController {
    private final ArticlesService articlesService;
    private final TopArticlesService topArticlesService;

    public ArticlesController(ArticlesService articlesService, TopArticlesService topArticlesService) {
        this.articlesService = articlesService;
        this.topArticlesService = topArticlesService;
    }

    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }

    @GetMapping(value = "/v1/articles", produces = APPLICATION_JSON_VALUE)
    public List<Article> fetchArticles(
            @RequestParam(value = "topic", required = false, defaultValue = "") String topic,
            @RequestParam(value = "region", required = false, defaultValue = "") String region,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size) {
        return articlesService.fetchArticles(topic, region, page, size);
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
    public List<ArticlePreview> fetchArticlePreviews(
            @RequestParam(value = "topic", required = false, defaultValue = "") String topic,
            @RequestParam(value = "region", required = false, defaultValue = "") String region,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size) {
        return articlesService.fetchArticlePreviews(topic, region, page, size);
    }

    @GetMapping(value = "/v1/preview/top/{region}", produces = APPLICATION_JSON_VALUE)
    public List<ArticlePreview> fetchTopArticlePreviewsForRegion(@PathVariable String region) {
        return topArticlesService.topArticles(region.toUpperCase());
    }

    @GetMapping(value = "/v1/preview/top/{region}/{topic}", produces = APPLICATION_JSON_VALUE)
    public List<ArticlePreview> fetchTopArticlePreviewsForRegionAndTopic(@PathVariable String region,
            @PathVariable String topic) {
        return topArticlesService.topArticles(region.toUpperCase(), topic.toUpperCase());
    }
}
