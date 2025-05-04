package com.leskor.digestwave.model;

import java.io.Serializable;
import java.time.ZonedDateTime;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.CassandraType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

@Table("mentions")
public record Mention(
        @PrimaryKeyColumn(type = PrimaryKeyType.PARTITIONED)
        String keyword,
        @PrimaryKeyColumn(name = "published_at", type = PrimaryKeyType.CLUSTERED)
        @CassandraType(type = CassandraType.Name.TIMESTAMP)
        ZonedDateTime publishedAt,
        @Column("article_url")
        String articleUrl,
        @Column("article_title")
        String articleTitle,
        @Column
        int sentiment
) {

    public Mention(Key key, String articleUrl, String articleTitle, int sentiment) {
        this(key.keyword, key.publishedAt, articleUrl, articleTitle, sentiment);
    }

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

    @PrimaryKeyClass
    public record Key(
            @PrimaryKeyColumn(type = PrimaryKeyType.PARTITIONED)
            String keyword,
            @PrimaryKeyColumn(name = "published_at", type = PrimaryKeyType.CLUSTERED)
            @CassandraType(type = CassandraType.Name.TIMESTAMP)
            ZonedDateTime publishedAt
    ) implements Serializable {}
}
