package com.leskor.digestwave.cache;

import com.leskor.digestwave.model.LastFetchTime;
import com.leskor.digestwave.repository.LastFetchTimeRepository;
import java.net.URI;
import java.time.Instant;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Bookkeeper is a simple cache that stores the last fetch time for each URI.
 */
@Component
public class Bookkeeper {
    private final LastFetchTimeRepository lastFetchTimeRepository;

    @Autowired
    Bookkeeper(LastFetchTimeRepository lastFetchTimeRepository) {
        this.lastFetchTimeRepository = lastFetchTimeRepository;
    }

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

        lastFetchTimeRepository.save(new LastFetchTime(uri.toString(), fetchTime));
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

        return lastFetchTimeRepository.findByUri(uri.toString()).map(LastFetchTime::fetchTime);
    }
}
