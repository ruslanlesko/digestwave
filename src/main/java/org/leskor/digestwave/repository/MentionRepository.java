package org.leskor.digestwave.repository;

import org.leskor.digestwave.model.Mention;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MentionRepository extends CassandraRepository<Mention, String> {
}
