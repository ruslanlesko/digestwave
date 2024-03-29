package com.leskor.postclassifier;

import static java.util.stream.Collectors.joining;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.leskor.postclassifier.db.PostRepository;
import com.leskor.postclassifier.model.Post;
import com.leskor.postclassifier.model.TopPost;
import com.leskor.postclassifier.service.AiRatingService;
import io.github.amithkoujalgi.ollama4j.core.OllamaAPI;
import io.github.amithkoujalgi.ollama4j.core.models.Model;
import io.github.amithkoujalgi.ollama4j.core.models.OllamaResult;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class AiRatingServiceTest {
    private static final String MODEL = "model";
    private static final String REGION = "INT";
    private static final String TOPIC = "TECH";

    private PostRepository postRepository = mock(PostRepository.class);
    private OllamaAPI ollamaAPI = mock(OllamaAPI.class);

    private AiRatingService aiRatingService;

    @BeforeEach
    void setUp() throws Exception {
        aiRatingService = new AiRatingService(postRepository, ollamaAPI, MODEL, "");

        when(ollamaAPI.ping()).thenReturn(true);
        Model model = new Model();
        model.setModel(MODEL);
        when(ollamaAPI.listModels()).thenReturn(List.of(model));
    }

    @Test
    void testRatePosts() throws Exception {
        List<Post> posts = IntStream.range(0, 20).mapToObj(i -> {
            final String title = switch (i) {
                case 0 -> "First article, mediocre title";
                case 1 -> "Second article, stunning title";
                case 2 -> "Third article, good title";
                default -> i + "th article, average material";
            };
            return new Post(String.valueOf(i).repeat(4), title, TOPIC, REGION, 0L, "url", "");
        }).toList();

        OllamaResult ollamaResult = new OllamaResult("""
                This is my top 3 articles:
                1. Second article with stunning title
                2. Third article with good title
                3. First article with mediocre title
                """, 1, 200);

        when(postRepository.findTop20ByTopicAndRegionOrderByPublicationTimeDesc(TOPIC,
                REGION)).thenReturn(posts);
        when(ollamaAPI.generate(eq(MODEL),
                argThat(p -> p.endsWith(posts.stream().map(Post::title).collect(joining("\n")))),
                any())).thenReturn(ollamaResult);

        List<TopPost> ratedPosts = aiRatingService.ratePosts(TOPIC, REGION);

        assertEquals(3, ratedPosts.size());
        List<TopPost> expectedPosts = List.of(new TopPost(0, TOPIC, REGION, "1111", 0),
                new TopPost(0, TOPIC, REGION, "2222", 1), new TopPost(0, TOPIC, REGION, "0000", 2));
        assertIterableEquals(expectedPosts, ratedPosts);
        verify(postRepository,
                Mockito.atMostOnce()).findTop20ByTopicAndRegionOrderByPublicationTimeDesc(
                any(String.class), any(String.class));
    }
}
