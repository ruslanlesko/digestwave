package com.leskor.provider.services;

import com.leskor.provider.entities.Article;
import com.leskor.provider.entities.ArticlePreview;
import com.leskor.provider.repositories.PostsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ArticlesService {
    private final PostsRepository postsRepository;
    private final SitesService sitesService;

    @Autowired
    public ArticlesService(PostsRepository postsRepository, SitesService sitesService) {
        this.postsRepository = postsRepository;
        this.sitesService = sitesService;
    }

    public List<Article> fetchArticles() {
        return postsRepository.findAllByOrderByPublicationTimeDesc()
                .stream()
                .map(p -> Article.from(p, sitesService::siteForCode))
                .limit(10)
                .toList();
    }

    public List<ArticlePreview> fetchArticlePreviews() {
        return fetchArticles().stream().map(ArticlePreview::from).toList();
    }
}
