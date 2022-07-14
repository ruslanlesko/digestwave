package com.leskor.scraper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leskor.scraper.entities.Region;
import com.leskor.scraper.entities.Topic;
import com.leskor.scraper.sites.RSSSite;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.Set;

public class RSSFactory {
    private final ObjectMapper objectMapper;
    private final String configFilePath;
    private final HttpClient httpClient;
    private final URI readabilityUri;

    public RSSFactory(ObjectMapper objectMapper, String configFilePath, HttpClient httpClient, URI readabilityUri) {
        this.objectMapper = objectMapper;
        this.configFilePath = configFilePath;
        this.httpClient = httpClient;
        this.readabilityUri = readabilityUri;
    }

    public List<RSSSite> createSites() throws IOException {
        InputStream in = loadFileFromResources(configFilePath);
        List<SiteConfig> configs = objectMapper.readerForListOf(SiteConfig.class).readValue(in);
        in.close();
        return configs.stream().map(this::configToRSSSite).toList();
    }

    private InputStream loadFileFromResources(String path) throws IOException {
        InputStream in = this.getClass().getClassLoader().getResourceAsStream(path);
        if (in == null) throw new IOException("File not found");
        return in;
    }

    private RSSSite configToRSSSite(SiteConfig siteConfig) {
        var uri = URI.create(siteConfig.uri());
        Duration timeout = siteConfig.timeout() <= 0 ? Duration.ofSeconds(10) : Duration.ofSeconds(siteConfig.timeout());
        return new RSSSite(
                uri,
                readabilityUri,
                siteConfig.code(),
                siteConfig.topic(),
                siteConfig.region(),
                httpClient,
                timeout,
                siteConfig.titleSuffixToTrim(),
                siteConfig.excludedCategories(),
                siteConfig.excludeIfTitleContains()
        );
    }

    private record SiteConfig(
            String uri,
            String code,
            Topic topic,
            Region region,
            long timeout,
            String titleSuffixToTrim,
            Set<String> excludedCategories,
            Set<String> excludeIfTitleContains
    ) {
    }
}
