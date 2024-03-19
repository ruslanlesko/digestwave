package com.leskor.postclassifier.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.leskor.postclassifier.db.PostRepository;
import com.leskor.postclassifier.model.Post;
import com.leskor.postclassifier.model.TopPost;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SimpleRatingServiceTest {
    private static final String REGION = "INT";
    private static final String TOPIC = "TECH";

    @Mock
    PostRepository postRepository;

    private SimpleRatingService simpleRatingService;

    @BeforeEach
    void setUp() {
        simpleRatingService = new SimpleRatingService(postRepository);
    }

    @Test
    void testRatePosts() {
        List<Post> posts = IntStream.range(0, 20)
                .mapToObj(i -> {
                    final String content = switch (i) {
                        case 0 -> "First content";
                        case 1 -> "Second content, largest";
                        case 2 -> "Third content, good";
                        default -> "";
                    };
                    return new Post(String.valueOf(i).repeat(4), "", TOPIC, REGION, 0L, "url",
                            content);
                }).toList();

        when(postRepository.findTop20ByTopicAndRegionOrderByPublicationTimeDesc(TOPIC,
                REGION)).thenReturn(posts);

        List<TopPost> ratedPosts = simpleRatingService.ratePosts(TOPIC, REGION);

        assertEquals(3, ratedPosts.size());
        List<TopPost> expectedPosts = List.of(
                new TopPost(0, TOPIC, REGION, "1111", 0),
                new TopPost(0, TOPIC, REGION, "2222", 1),
                new TopPost(0, TOPIC, REGION, "0000", 2));
        assertIterableEquals(expectedPosts, ratedPosts);
        verify(postRepository, Mockito.atMostOnce())
                .findTop20ByTopicAndRegionOrderByPublicationTimeDesc(any(String.class),
                        any(String.class));
    }
}