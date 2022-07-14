package com.leskor.provider.services;

import com.leskor.provider.entities.Article;
import com.leskor.provider.entities.ArticlePreview;
import com.leskor.provider.entities.Post;
import com.leskor.provider.repositories.PostsRepository;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ArticlesService {
    private final PostsRepository postsRepository;
    private final SitesService sitesService;

    @Autowired
    public ArticlesService(PostsRepository postsRepository, SitesService sitesService) {
        this.postsRepository = postsRepository;
        this.sitesService = sitesService;
    }

    public List<Article> fetchArticles(String topic, String region, int page, int size) {
        return extractPosts(topic.toUpperCase(), region.toUpperCase()).stream()
                .map(p -> Article.from(p, sitesService::siteForCode))
                .skip((long) (page - 1) * size)
                .limit(size)
                .toList();
    }

    private List<Post> extractPosts(String topic, String region) {
        if ((topic == null || topic.isBlank()) && (region == null || region.isBlank())) {
            return postsRepository.findAllByOrderByPublicationTimeDesc();
        }
        if ((topic == null || topic.isBlank())) {
            return postsRepository.findByRegionOrderByPublicationTimeDesc(region);
        }
        if ((region == null || region.isBlank())) {
            return postsRepository.findByTopicOrderByPublicationTimeDesc(topic);
        }
        return postsRepository.findByTopicAndRegionOrderByPublicationTimeDesc(topic, region);
    }

    public List<ArticlePreview> fetchArticlePreviews(String topic, String region, int page, int size) {
        return fetchArticles(topic, region, page, size).stream().map(ArticlePreview::from).toList();
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
