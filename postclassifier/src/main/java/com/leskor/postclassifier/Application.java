package com.leskor.postclassifier;

import io.github.amithkoujalgi.ollama4j.core.OllamaAPI;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import com.leskor.postclassifier.model.RegionWithTopic;

@SpringBootApplication
public class Application {
	private static final Logger logger = LoggerFactory.getLogger(Application.class);

	private static final String PROMPT_ENV = "PC_PROMPT";

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Bean
	OllamaAPI ollamaAPI(
			@Value("${app.ollama.host}") String ollamaHost,
			@Value("${app.ollama.timeout}") int timeout) {
		OllamaAPI ollamaAPI = new OllamaAPI(ollamaHost);
		ollamaAPI.setRequestTimeoutSeconds(timeout);
		return ollamaAPI;
	}

	@Bean(name = "prompt")
	String prompt() throws IOException {
		if (System.getenv(PROMPT_ENV) != null && !System.getenv(PROMPT_ENV).isEmpty()) {
			String prompt = Files.readString(Path.of(System.getenv(PROMPT_ENV)));
			logger.info("Using a custom prompt:\n{}", prompt);
			return prompt;
		}
		return "";
	}

	@Bean
	ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
			@Value("${app.kafka.host}") String kafkaHost) {
		ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
		Map<String, Object> props = new HashMap<>();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaHost);
		props.put(ConsumerConfig.GROUP_ID_CONFIG, "post-classifier");
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(props));
		return factory;
	}

	@Bean(destroyMethod = "close")
	ExecutorService executorService() {
		return Executors.newFixedThreadPool(8);
	}

	@Bean(name = "regionsAndTopics")
	List<RegionWithTopic> regionsAndTopics() {
		return List.of(
				new RegionWithTopic("INT", "TECH"),
				new RegionWithTopic("INT", "FINANCE"),
				new RegionWithTopic("INT", "PROGRAMMING"),
				new RegionWithTopic("UA", "TECH"),
				new RegionWithTopic("UA", "FINANCE"),
				new RegionWithTopic("UA", "FOOTBALL"));
	}

	@Bean(name = "delay")
	Duration delay() {
		return Duration.ofMinutes(1);
	}
}
