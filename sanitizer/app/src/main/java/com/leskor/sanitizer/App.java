package com.leskor.sanitizer;

import com.leskor.sanitizer.entities.Post;
import com.leskor.sanitizer.entities.SanitizedPost;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.streams.serdes.json.KafkaJsonSchemaSerde;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;
import org.jsoup.safety.Whitelist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.System.getenv;

public class App {
    private static final Logger logger = LoggerFactory.getLogger("Application");
    private static final String VERSION = "0.1.0";
    private static final String INPUT_TOPIC = "posts";
    private static final String OUTPUT_TOPIC = "sanitized-posts";

    private final String KAFKA_ADDRESS = getenv("SNT_KAFKA_ADDRESS") == null ?
            "localhost:19092" : getenv("SNT_KAFKA_ADDRESS");
    private final String SCHEMA_REGISTRY_ADDRESS = getenv("SNT_SCHEMA_REGISTRY_ADDRESS") == null ?
            "http://localhost:8081" : getenv("SNT_SCHEMA_REGISTRY_ADDRESS");

    private Properties prepareStreamProperties() {
        Properties props = new Properties();

        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "sanitizer-application");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_ADDRESS);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        return props;
    }

    public void run() {
        Properties props = prepareStreamProperties();

        Map<String, Object> serdeConfig = new HashMap<>();
        serdeConfig.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, SCHEMA_REGISTRY_ADDRESS);
        Serde<Post> postsSerde = new KafkaJsonSchemaSerde<>(Post.class);
        postsSerde.configure(serdeConfig, false);
        Map<String, Object> sanitizedSerdeConfig = new HashMap<>();
        sanitizedSerdeConfig.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, SCHEMA_REGISTRY_ADDRESS);
        Serde<SanitizedPost> sanitizedPostSerde = new KafkaJsonSchemaSerde<>(SanitizedPost.class);
        sanitizedPostSerde.configure(sanitizedSerdeConfig, false);

        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, Post> stream = builder.stream(INPUT_TOPIC, Consumed.with(Serdes.String(), postsSerde));
        stream.map((key, value) -> {
            List<String> paragraphs = parseParagraphsFromPost(value)
                    .stream()
                    .filter(paragraph -> paragraph != null && paragraph.length() > 0)
                    .toList();
            SanitizedPost sanitizedPost = SanitizedPost.from(value, paragraphs);
            return new KeyValue<>(key, sanitizedPost);
        }).to(OUTPUT_TOPIC, Produced.with(Serdes.String(), sanitizedPostSerde));

        final KafkaStreams streams = new KafkaStreams(builder.build(), props);
        final CountDownLatch latch = new CountDownLatch(1);

        // attach shutdown handler to catch control-c
        Runtime.getRuntime().addShutdownHook(new Thread("streams-shutdown-hook") {
            @Override
            public void run() {
                logger.info("Closing streams");
                streams.close(Duration.ofSeconds(5));
                logger.info("Streams were closed");
                latch.countDown();
            }
        });

        try {
            streams.setStateListener((newState, oldState) -> {
                if (newState.isShuttingDown()) {
                    logger.error("Kafka Streams are shutting down");
                    streams.close(Duration.ofSeconds(5));
                    latch.countDown();
                }
            });
            streams.start();
            latch.await();
        } catch (Throwable e) {
            System.exit(1);
        }
        System.exit(0);
    }

    private static List<String> parseParagraphsFromPost(Post post) {
        return switch (post.siteCode()) {
            case "FIN" -> parseFinanceUaParagraphs(post);
            case "MFN" -> parseMinfinUaParagraphs(post);
            default -> Arrays.stream(post.content().split("\n")).toList();
        };
    }

    private static List<String> parseFinanceUaParagraphs(Post post) {
        String[] paragraphs = post.content().split("\n");
        if (paragraphs.length == 0) {
            return List.of();
        }

        List<String> result = new ArrayList<>();

        String firstParagraph = paragraphs[0];
        if (!firstParagraph.contains(post.title())) {
            result.add(firstParagraph);
        }
        result.addAll(Arrays.stream(paragraphs).skip(1).toList());

        int paragraphContainingEditingSuggestionIdx = -1;
        for (int i = 0; i < result.size(); i++) {
            if (result.get(i).contains("Ctrl+Enter")) {
                paragraphContainingEditingSuggestionIdx = i;
                break;
            }
        }

        if (paragraphContainingEditingSuggestionIdx != -1) {
            result = result.subList(0, paragraphContainingEditingSuggestionIdx);
        }

        if (result.get(result.size() - 1).length() < 42) {
            result.remove(result.size() - 1);
        }

        return result;
    }

    private static List<String> parseMinfinUaParagraphs(Post post) {
        String html = post.html();
        if (html == null || html.isBlank()) {
            return List.of();
        }

        Document document = Jsoup.parse(html);
        List<String> result = new ArrayList<>();
        for (var p : document.getElementsByTag("p")) {
            String cleanedText = Jsoup.clean(p.html(), Safelist.none());
            if (cleanedText.length() > 0
                    && !cleanedText.contains("Читайте також")
                    && !cleanedText.contains("Підписуйтесь на")) {
                result.add(cleanedText);
            }
        }

        return result;
    }

    public static void main(String[] args) {
        logger.info("Starting Sanitizer {}", VERSION);
        var app = new App();
        app.run();
    }
}
