package com.leskor.postclassifier;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.leskor.postclassifier.model.TopPost;

@RestController
public class Controller {
	private static final Logger logger = LoggerFactory.getLogger(Controller.class);

	private static final List<RegionWithTopic> REGIONS_AND_TOPICS = List.of(
			new RegionWithTopic("INT", "TECH"),
			new RegionWithTopic("INT", "FINANCE"),
			new RegionWithTopic("INT", "PROGRAMMING"),
			new RegionWithTopic("UA", "TECH"),
			new RegionWithTopic("UA", "FINANCE"),
			new RegionWithTopic("UA", "FOOTBALL"));

	private final RatingService ratingService;

	public Controller(RatingService ratingService) {
		this.ratingService = ratingService;
	}

	@PostMapping(value = "/top", produces = APPLICATION_JSON_VALUE)
	public Response topPosts() {
		List<RegionPosts> regionPosts = new ArrayList<>();

		List<TopPost> totalTopPosts = new ArrayList<>();
		for (RegionWithTopic rt : REGIONS_AND_TOPICS) {
			final String region = rt.region();
			final String topic = rt.topic();
			logger.debug("Rating top posts for region {} and topic {}", region, topic);
			List<TopPost> topPosts = ratingService.ratePosts(topic, region);
			if ((topPosts).isEmpty()) {
				continue;
			}
			regionPosts.stream().filter(r -> r.region.equalsIgnoreCase(region)).findFirst()
					.ifPresentOrElse(
							rp -> rp.topicPosts().add(new TopicPosts(topic, topPosts)),
							() -> {
								List<TopicPosts> newTopicPosts = new ArrayList<>();
								newTopicPosts.add(new TopicPosts(topic, topPosts));
								regionPosts.add(new RegionPosts(region, newTopicPosts));
							});
			totalTopPosts.addAll(topPosts);
		}
		ratingService.saveTopPosts(totalTopPosts);

		return new Response(regionPosts);
	}

	public record Response(List<RegionPosts> regionPosts) {

	}

	public record RegionWithTopic(String region, String topic) {

	}

	public record RegionPosts(String region, List<TopicPosts> topicPosts) {

	}

	public record TopicPosts(String topic, List<TopPost> topPosts) {

	}
}
