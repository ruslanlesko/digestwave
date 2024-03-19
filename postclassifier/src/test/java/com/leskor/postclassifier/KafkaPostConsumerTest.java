package com.leskor.postclassifier;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.leskor.postclassifier.service.AiRatingService;
import com.leskor.postclassifier.service.RatingService;
import com.leskor.postclassifier.service.RatingServiceFactory;
import com.leskor.postclassifier.service.TopPostUpdater;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.leskor.postclassifier.model.RegionWithTopic;
import com.leskor.postclassifier.model.TopPost;

@ExtendWith(MockitoExtension.class)
class KafkaPostConsumerTest {
    private static final List<TopPost> TOP_POSTS = List.of(new TopPost(0, "TECH", "INT", "123", 0));

    @Mock
    RatingService ratingService;

    @Mock
    RatingServiceFactory ratingServiceFactory;

    @Mock
    TopPostUpdater topPostUpdater;

    ExecutorService executorService;

    KafkaPostConsumer kafkaPostConsumer;

    @BeforeEach
    void setup() {
        executorService = Executors.newSingleThreadExecutor();
        kafkaPostConsumer = new KafkaPostConsumer(ratingServiceFactory, topPostUpdater, executorService,
                List.of(new RegionWithTopic("INT", "TECH")), Duration.ofSeconds(1));
    }

    @Test
    void processWithMultipleInvocationProcessOnlyLatest() throws InterruptedException {
        when(ratingServiceFactory.getRatingService("INT")).thenReturn(ratingService);
        when(ratingService.ratePosts("TECH", "INT")).thenReturn(TOP_POSTS);

        kafkaPostConsumer.process();
        Thread.sleep(250);
        kafkaPostConsumer.process();
        Thread.sleep(250);
        kafkaPostConsumer.process();

        Thread.sleep(3500);

        verify(ratingService, times(1)).ratePosts("TECH", "INT");
        verify(topPostUpdater, times(1)).updateTopPosts(TOP_POSTS);
    }
}