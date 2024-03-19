package com.leskor.postclassifier.service;

import com.leskor.postclassifier.db.PostRepository;
import com.leskor.postclassifier.model.Post;
import com.leskor.postclassifier.model.TopPost;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SimpleRatingService implements RatingService {
    private static final Logger logger = LoggerFactory.getLogger(SimpleRatingService.class);

    private final PostRepository postRepository;

    public SimpleRatingService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @Override
    public List<TopPost> ratePosts(String topic, String region) {
        List<Post> posts =
                postRepository.findTop20ByTopicAndRegionOrderByPublicationTimeDesc(topic, region);

        if (posts.size() < 20) {
            logger.warn("No posts for region {} and topic {}", region, topic);
            return List.of();
        }

        List<Post> longestPosts = posts.stream()
                .filter(p -> p.imageUrl() != null && !p.imageUrl().isEmpty() && p.content() != null)
                .sorted(Comparator.<Post>comparingInt(o -> o.content().length()).reversed())
                .limit(3)
                .toList();
        List<TopPost> topPosts = new ArrayList<>();
        for (int i = 0; i < longestPosts.size(); i++) {
            topPosts.add(new TopPost(0, topic, region, longestPosts.get(i).hash(), i));
        }
        if (topPosts.size() < 3) {
            return List.of();
        }

        return topPosts;
    }
}
