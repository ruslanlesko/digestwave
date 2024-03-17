package com.leskor.provider.services;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.groupingBy;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.leskor.provider.entities.Article;
import com.leskor.provider.entities.ArticlePreview;
import com.leskor.provider.entities.TopPost;
import com.leskor.provider.repositories.TopPostsRepository;

@Service
public class TopArticlesService {
	private final TopPostsRepository topPostsRepository;
	private final SitesService sitesService;

	public TopArticlesService(TopPostsRepository topPostsRepository, SitesService sitesService) {
		this.topPostsRepository = topPostsRepository;
		this.sitesService = sitesService;
	}

	public List<ArticlePreview> topArticles(String region) {
		return topPostsRepository.findByRegion(region)
				.stream()
				.filter(p -> p.getPost() != null)
				.collect(groupingBy(TopPost::getTopic))
				.entrySet()
				.stream()
				.sorted((a, b) -> calculateTopicPriority(a.getKey()) - calculateTopicPriority(b.getKey()))
				.map(p -> p.getValue().stream().sorted(comparing(TopPost::getRating)).findFirst().orElse(null))
				.filter(Objects::nonNull)
				.map(p -> Article.from(p.getPost(), sitesService::siteForCode))
				.map(ArticlePreview::from)
				.limit(3)
				.toList();
	}

	public List<ArticlePreview> topArticles(String region, String topic) {
		return topPostsRepository.findByRegionAndTopic(region, topic)
				.stream()
				.sorted(comparing(TopPost::getRating))
				.map(TopPost::getPost)
				.filter(Objects::nonNull)
				.map(p -> Article.from(p, sitesService::siteForCode))
				.map(ArticlePreview::from)
				.limit(3)
				.toList();
	}

	private int calculateTopicPriority(String topic) {
		return switch (topic) {
		case "TECH" -> 0;
		case "PROGRAMMING" -> 1;
		case "SPORT" -> 2;
		default -> 100;
		};
	}
}
