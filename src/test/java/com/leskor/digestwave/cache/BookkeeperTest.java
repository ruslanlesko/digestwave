package com.leskor.digestwave.cache;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.time.Instant;
import org.junit.jupiter.api.Test;

public class BookkeeperTest {
    @Test
    void saveFetchTime_storesFetchTimeForGivenUri() {
        URI uri = URI.create("https://example.com");
        Instant fetchTime = Instant.parse("2023-01-01T00:00:00Z");
        Bookkeeper bookkeeper = new Bookkeeper();

        bookkeeper.saveFetchTime(uri, fetchTime);

        assertEquals(fetchTime, bookkeeper.lastFetchTime(uri).orElse(null));
    }

    @Test
    void saveFetchTime_overwritesExistingFetchTimeForUri() {
        URI uri = URI.create("https://example.com");
        Instant initialFetchTime = Instant.parse("2023-01-01T00:00:00Z");
        Instant updatedFetchTime = Instant.parse("2023-01-02T00:00:00Z");
        Bookkeeper bookkeeper = new Bookkeeper();

        bookkeeper.saveFetchTime(uri, initialFetchTime);
        bookkeeper.saveFetchTime(uri, updatedFetchTime);

        assertEquals(updatedFetchTime, bookkeeper.lastFetchTime(uri).orElse(null));
    }

    @Test
    void saveFetchTime_handlesNullUri() {
        Instant fetchTime = Instant.parse("2023-01-01T00:00:00Z");
        Bookkeeper bookkeeper = new Bookkeeper();

        assertThrows(IllegalArgumentException.class, () -> bookkeeper.saveFetchTime(null, fetchTime));
    }

    @Test
    void saveFetchTime_handlesNullFetchTime() {
        URI uri = URI.create("https://example.com");
        Bookkeeper bookkeeper = new Bookkeeper();

        assertThrows(IllegalArgumentException.class, () -> bookkeeper.saveFetchTime(uri, null));
    }

    @Test
    void lastFetchTime_returnsEmptyOptionalForUnknownUri() {
        URI uri = URI.create("https://unknown.com");
        Bookkeeper bookkeeper = new Bookkeeper();

        assertTrue(bookkeeper.lastFetchTime(uri).isEmpty());
    }

    @Test
    void lastFetchTime_HandlesNullUri() {
        Bookkeeper bookkeeper = new Bookkeeper();

        assertThrows(IllegalArgumentException.class, () -> bookkeeper.lastFetchTime(null));
    }
}
