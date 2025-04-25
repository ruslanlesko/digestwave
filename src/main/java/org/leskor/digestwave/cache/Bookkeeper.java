package org.leskor.digestwave.cache;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Bookkeeper is a simple in-memory cache that stores the last fetch time for each URI.
 */
@Component
public class Bookkeeper {
    private final Map<URI, Instant> cache = new HashMap<>();

    /**
     * Saves the fetch time for a given URI.
     *
     * @param uri the URI to save the fetch time for (cannot be null)
     * @param fetchTime the fetch time to save (cannot be null)
     *
     * @throws IllegalArgumentException if uri or fetchTime is null
     */
    public void saveFetchTime(URI uri, Instant fetchTime) {
        if (uri == null) {
            throw new IllegalArgumentException("URI cannot be null");
        }
        if (fetchTime == null) {
            throw new IllegalArgumentException("Fetch time cannot be null");
        }

        cache.put(uri, fetchTime);
    }

    /**
     * Retrieves the last fetch time for a given URI.
     *
     * @param uri the URI to retrieve the fetch time for (cannot be null)
     * @return an Optional containing the last fetch time, or an empty Optional if the URI is not found
     *
     * @throws IllegalArgumentException if uri is null
     */
    public Optional<Instant> lastFetchTime(URI uri) {
        if (uri == null) {
            throw new IllegalArgumentException("URI cannot be null");
        }

        return Optional.ofNullable(cache.get(uri));
    }
}
