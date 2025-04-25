package org.leskor.digestwave.model;

import java.time.ZonedDateTime;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.CassandraType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

@Table
public record Mention(
        @PrimaryKeyColumn(type = PrimaryKeyType.PARTITIONED)
        String keyword,
        @PrimaryKeyColumn(type = PrimaryKeyType.CLUSTERED)
        @CassandraType(type = CassandraType.Name.TIMESTAMP)
        ZonedDateTime publishedAt,
        @Column
        String articleUrl,
        @Column
        String articleTitle,
        @Column
        int sentiment
) {
    public static Mention of(Article article, String keyword, Sentiment sentiment) {
        return new Mention(
                keyword,
                article.publishedAt(),
                article.uri().toString(),
                article.title(),
                switch (sentiment) {
                    case POSITIVE -> 1;
                    case NEGATIVE -> -1;
                    case NEUTRAL -> 0;
                }
        );
    }
}
