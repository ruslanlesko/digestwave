package com.leskor.scraper;

import com.leskor.scraper.entities.Post;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.apache.kafka.clients.producer.ProducerConfig.*;

public class KafkaPostsProducer {
    private static final Logger logger = LoggerFactory.getLogger("Application");

    private static final String TOPIC = "posts";

    private final Producer<String, String> producer;

    public KafkaPostsProducer() {
        Properties props = new Properties();
        props.put(BOOTSTRAP_SERVERS_CONFIG, Config.getKafkaAddress());
        props.put(ACKS_CONFIG, "all");
        props.put(KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        this.producer = new KafkaProducer<>(props);
    }

    public void sendPosts(List<Post> posts) {
        final var countDownLatch = new CountDownLatch(posts.size());

        try {
            for (Post post : posts) {
                final ProducerRecord<String, String> record =
                        new ProducerRecord<>(TOPIC, post.title());
                producer.send(record, (metadata, exception) -> {
                    if (metadata != null) {
                        logger.debug("sent record(key={} value={}) meta(partition={}, offset={})",
                                record.key(), record.value(), metadata.partition(), metadata.offset());
                    } else {
                        logger.error("Failed to send post", exception);
                    }
                    countDownLatch.countDown();
                });
            }
            if (!countDownLatch.await(25, TimeUnit.SECONDS)) {
                logger.error("Sending posts timed out");
            }
        } catch (InterruptedException e) {
            logger.error("Interrupted", e);
        } finally {
            producer.flush();
        }
    }

    public void close() {
        producer.close();
    }
}
