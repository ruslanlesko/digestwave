package com.leskor.provider.services;

import com.leskor.provider.entities.Article;
import com.leskor.provider.entities.ArticlePreview;
import com.leskor.provider.entities.Post;
import com.leskor.provider.repositories.PostsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Optional;

@Service
public class ArticlesService {
    private static final int ARTICLES_LIMIT = 10;

    private final PostsRepository postsRepository;
    private final SitesService sitesService;

    @Autowired
    public ArticlesService(PostsRepository postsRepository, SitesService sitesService) {
        this.postsRepository = postsRepository;
        this.sitesService = sitesService;
    }

    public List<Article> fetchArticles(String topic, int page, int size) {
        List<Post> posts = topic == null || topic.isBlank() ?
                postsRepository.findAllByOrderByPublicationTimeDesc()
                : postsRepository.findByTopicOrderByPublicationTimeDesc(topic.toUpperCase());

        return posts.stream()
                .map(p -> Article.from(p, sitesService::siteForCode))
                .skip((long) (page - 1) * size)
                .limit(size)
                .toList();
    }

    public List<ArticlePreview> fetchArticlePreviews(String topic, int page, int size) {
        return fetchArticles(topic, page, size).stream().map(ArticlePreview::from).toList();
    }

    public Optional<Article> fetchArticleById(String articleId) {
        String postId = articleId.substring(1);
        if (articleId.startsWith("n")) {
            postId = "-" + postId;
        }
        return postsRepository.findById(postId).map(p -> Article.from(p, sitesService::siteForCode));
    }

    public Optional<byte[]> fetchCoverImageByArticleId(String articleId) {
        String postId = articleId.substring(1);
        if (articleId.startsWith("n")) {
            postId = "-" + postId;
        }

        String imageURL = postsRepository.findById(postId).map(Post::getImageURL).orElse(null);
        if (imageURL == null) return Optional.empty();
        try (InputStream input = new URL(imageURL).openStream()) {
            return Optional.of(input.readAllBytes());
        } catch (IOException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }
}
