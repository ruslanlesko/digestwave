package com.leskor.digestwave.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import com.leskor.digestwave.model.Sentiment;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MetadataExtractor {
    private static final String SYSTEM_PROMPT = """
        You are a helpful assistant that processes news titles. Your task is to extract keywords and determine the sentiment of the article.
        
        Respond with a JSON object containing the following fields: keywords (array of strings) and sentiment (string: "positive", "negative", "neutral").
        For example:
        
        {
            "keywords": ["Google", "AI"],
            "sentiment": "positive"
        }
        
        {
            "keywords": ["Layoffs", "Meta"],
            "sentiment": "negative"
        }
        
        {
            "keywords": ["GenAI", "Law"],
            "sentiment": "neutral"
        }
        """;

    private final OllamaChatModel ollamaChatModel;
    private final ObjectMapper objectMapper;

    @Autowired
    public MetadataExtractor(OllamaChatModel ollamaChatModel, ObjectMapper objectMapper) {
        this.ollamaChatModel = ollamaChatModel;
        this.objectMapper = objectMapper;
    }

    public Metadata extractMetadata(String title) {
        Prompt prompt = new Prompt(List.of(new SystemMessage(SYSTEM_PROMPT), new UserMessage(title)));

        String rawText = ollamaChatModel.call(prompt).getResult().getOutput().getText();
        try {
            return objectMapper.readValue(rawText, Metadata.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to process JSON response", e);
        }
    }

    public record Metadata(Set<String> keywords, Sentiment sentiment) {
        public static Metadata of(Set<String> keywords, Sentiment sentiment) {
            return new Metadata(keywords, sentiment);
        }
    }
}
