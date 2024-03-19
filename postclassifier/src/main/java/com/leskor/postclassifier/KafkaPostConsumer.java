package com.leskor.postclassifier;

import com.leskor.postclassifier.model.RegionWithTopic;
import com.leskor.postclassifier.model.TopPost;
import com.leskor.postclassifier.service.RatingServiceFactory;
import com.leskor.postclassifier.service.TopPostUpdater;
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

@Service
public class KafkaPostConsumer {
    private static final Logger logger = LoggerFactory.getLogger(KafkaPostConsumer.class);

    private final RatingServiceFactory ratingServiceFactory;
    private final TopPostUpdater topPostUpdater;
    private final ExecutorService executorService;
    private final List<RegionWithTopic> regionsAndTopics;
    private final Duration delay;
    private final AtomicBoolean isProcessing;
    private final AtomicLong latestMessageReceived;

    public KafkaPostConsumer(
            RatingServiceFactory ratingServiceFactory,
            TopPostUpdater topPostUpdater,
            ExecutorService executorService,
            @Qualifier("regionsAndTopics") List<RegionWithTopic> regionsAndTopics,
            @Qualifier("delay") Duration delay) {
        this.ratingServiceFactory = ratingServiceFactory;
        this.topPostUpdater = topPostUpdater;
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
                    logger.debug("Rating top posts for region {} and topic {}", rt.region(),
                            rt.topic());
                    List<TopPost> topPosts = ratingServiceFactory.getRatingService(rt.region())
                            .ratePosts(rt.topic(), rt.region());
                    if ((topPosts).isEmpty()) {
                        continue;
                    }
                    topPostUpdater.updateTopPosts(topPosts);
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
