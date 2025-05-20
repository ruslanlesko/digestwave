package com.leskor.digestwave.cache;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.leskor.digestwave.model.LastFetchTime;
import com.leskor.digestwave.repository.LastFetchTimeRepository;
import java.net.URI;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class BookkeeperTest {

    @Mock
    private LastFetchTimeRepository lastFetchTimeRepository;

    private Bookkeeper bookkeeper;

    @BeforeEach
    void setUp() {
        bookkeeper = new Bookkeeper(lastFetchTimeRepository);
    }

    @Test
    void saveFetchTime_storesFetchTimeForGivenUri() {
        URI uri = URI.create("https://example.com");
        Instant fetchTime = Instant.parse("2023-01-01T00:00:00Z");

        bookkeeper.saveFetchTime(uri, fetchTime);

        verify(lastFetchTimeRepository).save(new LastFetchTime("https://example.com", fetchTime));
    }

    @Test
    void saveFetchTime_handlesNullUri() {
        Instant fetchTime = Instant.parse("2023-01-01T00:00:00Z");

        assertThrows(IllegalArgumentException.class, () -> bookkeeper.saveFetchTime(null, fetchTime));
    }

    @Test
    void saveFetchTime_handlesNullFetchTime() {
        URI uri = URI.create("https://example.com");

        assertThrows(IllegalArgumentException.class, () -> bookkeeper.saveFetchTime(uri, null));
    }

    @Test
    void lastFetchTime_returnsEmptyOptionalForUnknownUri() {
        when(lastFetchTimeRepository.findByUri(any())).thenReturn(Optional.empty());

        URI uri = URI.create("https://unknown.com");

        assertTrue(bookkeeper.lastFetchTime(uri).isEmpty());
    }

    @Test
    void lastFetchTime_HandlesNullUri() {
        assertThrows(IllegalArgumentException.class, () -> bookkeeper.lastFetchTime(null));
    }
}
