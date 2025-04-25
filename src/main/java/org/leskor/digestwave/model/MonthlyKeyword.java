package org.leskor.digestwave.model;

import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.CassandraType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

@Table
public record MonthlyKeyword(
        @PrimaryKeyColumn(type = PrimaryKeyType.PARTITIONED)
        String monthYear,
        @PrimaryKeyColumn(type = PrimaryKeyType.CLUSTERED)
        String keyword,
        @Column
        @CassandraType(type = CassandraType.Name.COUNTER)
        long count) {
}
