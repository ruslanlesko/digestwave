package com.leskor.postclassifier;

import io.github.amithkoujalgi.ollama4j.core.OllamaAPI;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

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
}
