CREATE EXTENSION pg_cron;

ALTER SYSTEM SET cron.use_background_workers = 'on';

CREATE TABLE "public".posts (
    site_code VARCHAR(8),
    topic VARCHAR(16) NOT NULL,
    region VARCHAR(3) NOT NULL,
    hash VARCHAR(16) PRIMARY KEY,
    publication_time BIGINT,
    title TEXT,
    content TEXT,
    style TEXT,
    url TEXT,
    image_url TEXT
);

CREATE TABLE "public".top_posts (
    id SERIAL PRIMARY KEY,
    topic VARCHAR(16) NOT NULL,
    region VARCHAR(3) NOT NULL,
    hash VARCHAR(16) NOT NULL,
    rating INT NOT NULL,
    FOREIGN KEY (hash) REFERENCES posts(hash) ON DELETE CASCADE
);

SELECT cron.schedule('0 */6 * * *', $$WITH latest AS (
        SELECT ROW_NUMBER() OVER (ORDER BY publication_time DESC), publication_time, hash FROM posts
    ) DELETE FROM posts USING latest WHERE latest.hash = posts.hash AND latest.row_number > 1000$$
);

SELECT cron.schedule('0 4 * * *', 'VACUUM FULL posts');
SELECT cron.schedule('0 5 * * *', 'VACUUM FULL top_posts');
