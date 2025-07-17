package com.leskor.digestwave.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import java.util.List;

@ConfigurationProperties(prefix = "feeds")
public record FeedProperties(List<String> urls) {
}
