package com.leskor.scraper.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ReadabilityResponse(String title, String content, String textContent, int length, String excerpt) {
    public static ReadabilityResponse fromTitleAndExistingResponse(String title, ReadabilityResponse response) {
        return new ReadabilityResponse(
                title,
                response.content(),
                response.textContent(),
                response.length(),
                response.excerpt()
        );
    }
}
