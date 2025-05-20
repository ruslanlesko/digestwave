CREATE KEYSPACE digestwave WITH replication = {'class' : 'SimpleStrategy', 'replication_factor' : 1};

CREATE TABLE digestwave.mentions
(
    keyword          text,
    published_at     timestamp,
    original_keyword text,
    article_title    text,
    article_url      text,
    sentiment        int,
    PRIMARY KEY (keyword, published_at)
) WITH CLUSTERING ORDER BY (published_at ASC);

CREATE TABLE digestwave.monthly_keywords
(
    month_year text,
    keyword    text,
    count      counter,
    PRIMARY KEY (month_year, keyword)
) WITH CLUSTERING ORDER BY (keyword ASC);

CREATE TABLE digestwave.last_fetch_times
(
    uri        TEXT,
    fetch_time TIMESTAMP,
    PRIMARY KEY (uri)
);
