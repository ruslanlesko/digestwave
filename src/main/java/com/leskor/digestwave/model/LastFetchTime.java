package com.leskor.digestwave.model;

import java.time.Instant;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.CassandraType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

@Table("last_fetch_times")
public record LastFetchTime(
        @PrimaryKeyColumn(type = PrimaryKeyType.PARTITIONED)
        String uri,
        @Column("fetch_time")
        @CassandraType(type = CassandraType.Name.TIMESTAMP)
        Instant fetchTime
) {
}
