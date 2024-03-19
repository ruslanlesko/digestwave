package com.leskor.postclassifier;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.leskor.postclassifier.model.RegionWithTopic;
import com.leskor.postclassifier.model.TopPost;
import com.leskor.postclassifier.service.RatingServiceFactory;
import com.leskor.postclassifier.service.TopPostUpdater;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {
    private static final Logger logger = LoggerFactory.getLogger(Controller.class);

    private final RatingServiceFactory ratingServiceFactory;
    private final TopPostUpdater topPostUpdater;
    private final List<RegionWithTopic> regionsAndTopics;

    public Controller(RatingServiceFactory ratingServiceFactory, TopPostUpdater topPostUpdater,
                      @Qualifier("regionsAndTopics") List<RegionWithTopic> regionsAndTopics) {
        this.ratingServiceFactory = ratingServiceFactory;
        this.topPostUpdater = topPostUpdater;
        this.regionsAndTopics = regionsAndTopics;
    }

    @PostMapping(value = "/top", produces = APPLICATION_JSON_VALUE)
    public Response topPosts() {
        List<RegionPosts> regionPosts = new ArrayList<>();

        List<TopPost> totalTopPosts = new ArrayList<>();
        for (RegionWithTopic rt : regionsAndTopics) {
            final String region = rt.region();
            final String topic = rt.topic();
            logger.debug("Rating top posts for region {} and topic {}", region, topic);
            List<TopPost> topPosts =
                    ratingServiceFactory.getRatingService(region).ratePosts(topic, region);
            if ((topPosts).isEmpty()) {
                continue;
            }
            regionPosts.stream().filter(r -> r.region.equalsIgnoreCase(region)).findFirst()
                    .ifPresentOrElse(rp -> rp.topicPosts().add(new TopicPosts(topic, topPosts)),
                            () -> {
                                List<TopicPosts> newTopicPosts = new ArrayList<>();
                                newTopicPosts.add(new TopicPosts(topic, topPosts));
                                regionPosts.add(new RegionPosts(region, newTopicPosts));
                            });
            totalTopPosts.addAll(topPosts);
        }
        topPostUpdater.updateTopPosts(totalTopPosts);

        return new Response(regionPosts);
    }

    public record Response(List<RegionPosts> regionPosts) {

    }

    public record RegionPosts(String region, List<TopicPosts> topicPosts) {

    }

    public record TopicPosts(String topic, List<TopPost> topPosts) {

    }
}
