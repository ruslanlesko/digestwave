package com.leskor.digestwave.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.leskor.digestwave.model.Sentiment;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;

class MetadataExtractorTest {
    private OllamaChatModel ollamaChatModel;
    private MetadataExtractor metadataExtractor;

    @BeforeEach
    void setup() {
        ollamaChatModel = mock(OllamaChatModel.class);
        metadataExtractor = new MetadataExtractor(ollamaChatModel, new ObjectMapper());
    }

    @Test
    void extractMetadata_extractsMetadataSuccessfully() {
        String title = "Google launches new AI tool";
        when(ollamaChatModel.call(any(Prompt.class))).thenReturn(mockResponse("""
            {
                "keywords": ["Google", "AI"],
                "sentiment": "positive"
            }
        """));

        MetadataExtractor.Metadata result = metadataExtractor.extractMetadata(title);

        assertEquals(new MetadataExtractor.Metadata(Set.of("Google", "AI"), Sentiment.POSITIVE), result);
        verify(ollamaChatModel).call(argThat((Prompt p) -> p.getContents().contains(title)));
    }

    @Test
    void extractMetadata_throwsExceptionForCorruptedJsonResponse() {
        when(ollamaChatModel.call(any(Prompt.class))).thenReturn(mockResponse("""
        Corrupted JSON response
        """));

        assertThrows(RuntimeException.class, () -> metadataExtractor.extractMetadata(""));
    }

    @Test
    void extractMetadata_handlesEmptyTitleGracefully() {
        when(ollamaChatModel.call(any(Prompt.class))).thenReturn(mockResponse("""
            {
                "keywords": [],
                "sentiment": "neutral"
            }
        """));

        MetadataExtractor.Metadata result = metadataExtractor.extractMetadata("");

        assertEquals(new MetadataExtractor.Metadata(Set.of(), Sentiment.NEUTRAL), result);
    }

    private ChatResponse mockResponse(String rawText) {
        return new ChatResponse(List.of(new Generation(new AssistantMessage(rawText))));
    }
}
