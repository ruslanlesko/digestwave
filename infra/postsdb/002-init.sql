CREATE EXTENSION pg_cron;

ALTER SYSTEM SET cron.use_background_workers = 'on';

CREATE TABLE "public".posts (
    site_code VARCHAR(8),
    hash VARCHAR(16) PRIMARY KEY,
    publication_time BIGINT,
    title TEXT,
    content TEXT
);

SELECT cron.schedule('0 */6 * * *', $$WITH latest AS (
        SELECT ROW_NUMBER() OVER (ORDER BY publication_time DESC), publication_time, hash FROM posts
    ) DELETE FROM posts USING latest WHERE latest.hash = posts.hash AND latest.row_number > 1000$$
);
