package com.leskor.digestwave.repository;

import com.leskor.digestwave.model.LastFetchTime;
import java.util.Optional;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LastFetchTimeRepository extends CassandraRepository<LastFetchTime, String> {
    Optional<LastFetchTime> findByUri(String uri);
}
