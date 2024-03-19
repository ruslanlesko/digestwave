package com.leskor.postclassifier.service;

import com.leskor.postclassifier.db.TopPostRepository;
import com.leskor.postclassifier.model.TopPost;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TopPostUpdater {
    private static final Logger logger = LoggerFactory.getLogger(TopPostUpdater.class);

    private final TopPostRepository topPostRepository;

    public TopPostUpdater(TopPostRepository topPostRepository) {
        this.topPostRepository = topPostRepository;
    }

    @Transactional
    public void updateTopPosts(List<TopPost> topPosts) {
        logger.info("Saving {} top posts", topPosts.size());
        topPosts.stream()
                .map(TopPost::topic)
                .distinct()
                .forEach(topic -> topPosts.stream()
                        .filter(p -> p.topic().equalsIgnoreCase(topic))
                        .map(TopPost::region)
                        .distinct()
                        .forEach(region -> topPostRepository.deleteAll(
                                topPostRepository.findAllByTopicAndRegion(topic, region))));
        topPostRepository.saveAll(topPosts);
    }
}
