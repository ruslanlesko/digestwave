package com.leskor.digestwave.repository;

import com.leskor.digestwave.model.Mention;
import java.time.Instant;
import java.util.List;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MentionRepository extends CassandraRepository<Mention, Mention.Key> {
    List<Mention> findByKeyword(String keyword);

    List<Mention> findByKeywordAndPublishedAtGreaterThanEqual(String keyword, Instant publishedAt);

    List<Mention> findByKeywordAndPublishedAtLessThan(String keyword, Instant publishedAt);

    List<Mention> findByKeywordAndPublishedAtGreaterThanEqualAndPublishedAtLessThan(
            String keyword,
            Instant startInclusive,
            Instant endExclusive);
}
