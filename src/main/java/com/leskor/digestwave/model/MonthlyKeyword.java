package com.leskor.digestwave.model;

import java.io.Serializable;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.CassandraType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

@Table("monthly_keywords")
public record MonthlyKeyword(
        @PrimaryKeyColumn(name = "month_year", type = PrimaryKeyType.PARTITIONED)
        String monthYear,
        @PrimaryKeyColumn(name = "keyword", type = PrimaryKeyType.CLUSTERED)
        String keyword,
        @Column("count") @CassandraType(type = CassandraType.Name.COUNTER)
        long count) {

    public MonthlyKeyword(Key key, long count) {
        this(key.monthYear, key.keyword, count);
    }

    public static MonthlyKeyword of(String monthYear, String keyword, long count) {
        return new MonthlyKeyword(new Key(monthYear, keyword), count);
    }

    @PrimaryKeyClass
    public record Key(
            @PrimaryKeyColumn(name = "month_year", type = PrimaryKeyType.PARTITIONED)
            String monthYear,
            @PrimaryKeyColumn(name = "keyword", type = PrimaryKeyType.CLUSTERED)
            String keyword
    ) implements Serializable {}
}
