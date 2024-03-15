package com.leskor.postclassifier;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.leskor.postclassifier.model.RegionWithTopic;
import com.leskor.postclassifier.model.TopPost;

@Service
public class KafkaPostConsumer {
	private static final Logger logger = LoggerFactory.getLogger(KafkaPostConsumer.class);

	private final RatingService ratingService;
	private final ExecutorService executorService;
	private final List<RegionWithTopic> regionsAndTopics;
	private final Duration delay;
	private final AtomicBoolean isProcessing;
	private final AtomicLong latestMessageReceived;

	public KafkaPostConsumer(
			RatingService ratingService,
			ExecutorService executorService,
			@Qualifier("regionsAndTopics") List<RegionWithTopic> regionsAndTopics,
			@Qualifier("delay") Duration delay) {
		this.ratingService = ratingService;
		this.executorService = executorService;
		this.regionsAndTopics = regionsAndTopics;
		this.delay = delay;
		this.isProcessing = new AtomicBoolean(false);
		this.latestMessageReceived = new AtomicLong();
	}

	@KafkaListener(topics = "sanitized-posts", groupId = "post-classifier")
	public void process() {
		logger.debug("Processing a new message");
		final long messageTime = System.currentTimeMillis();
		latestMessageReceived.set(messageTime);
		executorService.submit(() -> {
			boolean isCurrentlyProcessing = false;
			try {
				Thread.sleep(delay);
				if (latestMessageReceived.get() != messageTime) {
					return;
				}
				if (!isProcessing.compareAndSet(false, true)) {
					return;
				}
				isCurrentlyProcessing = true;
				for (RegionWithTopic rt : regionsAndTopics) {
					logger.debug("Rating top posts for region {} and topic {}", rt.region(), rt.topic());
					List<TopPost> topPosts = ratingService.ratePosts(rt.topic(), rt.region());
					if ((topPosts).isEmpty()) {
						continue;
					}
					ratingService.saveTopPosts(topPosts);
				}
			} catch (InterruptedException e) {
				logger.error("Interrupted delay after receiving a message.", e);
				Thread.currentThread().interrupt();
			} finally {
				if (isCurrentlyProcessing) {
					isProcessing.set(false);
				}
			}
		});
	}
}
