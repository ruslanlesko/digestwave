package org.leskor.digestwave.model;

import java.net.URI;
import java.time.ZonedDateTime;

public record Article(
        URI uri,
        String title,
        ZonedDateTime publishedAt) {
}
