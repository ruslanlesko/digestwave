package com.leskor.provider.services;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import com.leskor.provider.entities.Article;
import com.leskor.provider.entities.ArticlePreview;
import com.leskor.provider.entities.Post;
import com.leskor.provider.repositories.PostsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
        return extractPosts(topic.toUpperCase(), region.toUpperCase(), page, size).stream()
                .map(p -> Article.from(p, sitesService::siteForCode))
                .toList();
    }

    private List<Post> extractPosts(String topic, String region, int page, int size) {
        var pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "publicationTime"));
        if ((topic == null || topic.isBlank()) && (region == null || region.isBlank())) {
            return postsRepository.findAll(pageRequest).toList();
        }
        if ((topic == null || topic.isBlank())) {
            return postsRepository.findByRegion(region, pageRequest);
        }
        if ((region == null || region.isBlank())) {
            return postsRepository.findByTopic(topic, pageRequest);
        }
        return postsRepository.findByTopicAndRegion(topic, region, pageRequest);
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
