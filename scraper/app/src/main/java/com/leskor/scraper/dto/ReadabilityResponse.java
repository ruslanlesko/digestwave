package com.leskor.scraper.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ReadabilityResponse(String title, String content, String textContent, int length, String excerpt) {
}
