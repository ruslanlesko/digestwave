package org.leskor.digestwave.repository;

import org.leskor.digestwave.model.MonthlyKeyword;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MonthlyKeywordRepository extends CassandraRepository<MonthlyKeyword, String> {

    @Query("UPDATE monthlykeyword SET count = count + ?2 WHERE monthyear = ?0 AND keyword = ?1")
    void incrementCount(String monthYear, String keyword, long count);
}
