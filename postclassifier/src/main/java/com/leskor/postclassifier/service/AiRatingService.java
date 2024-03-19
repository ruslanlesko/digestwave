package com.leskor.postclassifier.service;

import com.leskor.postclassifier.StringMatcher;
import com.leskor.postclassifier.db.PostRepository;
import com.leskor.postclassifier.db.TopPostRepository;
import com.leskor.postclassifier.exceptions.OllamaUnavailableException;
import com.leskor.postclassifier.model.Post;
import com.leskor.postclassifier.model.TopPost;
import io.github.amithkoujalgi.ollama4j.core.OllamaAPI;
import io.github.amithkoujalgi.ollama4j.core.exceptions.OllamaBaseException;
import io.github.amithkoujalgi.ollama4j.core.models.OllamaResult;
import io.github.amithkoujalgi.ollama4j.core.utils.OptionsBuilder;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AiRatingService implements RatingService {
    private static final Logger logger = LoggerFactory.getLogger(AiRatingService.class);

    private final PostRepository postRepository;
    private final OllamaAPI ollama;
    private final String model;
    private final String promptPrefix;

    public AiRatingService(PostRepository postRepository,
                           OllamaAPI ollama, @Value("${app.model}") String model,
                           @Qualifier("prompt") String prompt) {
        this.postRepository = postRepository;
        this.ollama = ollama;
        this.model = model;
        this.promptPrefix = prompt != null && !prompt.isEmpty() ? prompt
                : "Could you please select top 5 most interesting news titles " +
                "from the list provided below (do not use external resources, just the title, " +
                "use the judgement based on how title looks linguistically)? " +
                "Please avoid titles that imply sales or purely marketing goals. " +
                "Title list is the following:\n";
    }

    @Override
    public List<TopPost> ratePosts(String topic, String region) {
        List<Post> posts =
                postRepository.findTop20ByTopicAndRegionOrderByPublicationTimeDesc(topic, region);

        if (posts.size() < 20) {
            logger.warn("No posts for region {} and topic {}", region, topic);
            return List.of();
        }

        List<TopPost> topPosts = queryTopPosts(posts);
        if (topPosts.size() < 3) {
            return List.of();
        }

        return topPosts;
    }

    private List<TopPost> queryTopPosts(List<Post> posts) {
        try {
            validateOllamaAPI();

            StringBuilder promptBuilder = new StringBuilder(promptPrefix);
            posts.forEach(p -> promptBuilder.append('\n').append(p.title()));

            String prompt = promptBuilder.toString();
            logger.debug("Prompting ollama with the following:\n{}", prompt);

            OllamaResult result = ollama.generate(model, prompt,
                    new OptionsBuilder().setTemperature(0.25f).setSeed(42).build());
            if (result.getHttpStatusCode() != 200) {
                throw new OllamaUnavailableException(
                        "Cannot query ollama, status code " + result.getHttpStatusCode());
            }
            logger.debug("Ollama response is:\n{}", result.getResponse());
            List<Post> top3Posts = extractTop3Posts(posts, result.getResponse());
            if (top3Posts.isEmpty()) {
                logger.error("Not able to extract top posts from Ollama response:\n{}",
                        result.getResponse());
            } else {
                logger.debug("Top 3 posts are:");
                top3Posts.forEach(p -> logger.debug(p.title()));
            }
            return top3Posts.stream()
                    .map(p -> new TopPost(0, p.topic(), p.region(), p.hash(), top3Posts.indexOf(p)))
                    .toList();
        } catch (OllamaBaseException | IOException | URISyntaxException e) {
            throw new OllamaUnavailableException(e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Interrupted");
            return List.of();
        }
    }

    private List<Post> extractTop3Posts(List<Post> posts, String response) {
        Map<Post, Integer> resultPositions = new HashMap<>();
        List<String> responseLines = Arrays.stream(response.split("\n")).toList();
        for (int i = 0; i < responseLines.size(); i++) {
            String responseLine = responseLines.get(i);
            Optional<Post> post = posts.stream()
                    .filter(p -> StringMatcher.matches(responseLine, p.title()))
                    .findAny();
            if (post.isPresent()) {
                resultPositions.put(post.get(), i);
            }
        }

        return resultPositions.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .limit(3)
                .toList();
    }

    private void validateOllamaAPI()
            throws OllamaBaseException, IOException, URISyntaxException, InterruptedException {
        if (!ollama.ping()) {
            throw new OllamaUnavailableException("Ollama API is not reachable");
        }
        if (ollama.listModels().stream()
                .noneMatch(m -> m.getModel().equalsIgnoreCase(model))) {
            logger.info("Pulling model {}", model);
            ollama.pullModel(model);
            logger.info("Model {} is downloaded", model);
            if (ollama.listModels().stream()
                    .noneMatch(m -> m.getModel().equalsIgnoreCase(model))) {
                throw new OllamaUnavailableException("Ollama cannot pull model");
            }
        }
    }
}
