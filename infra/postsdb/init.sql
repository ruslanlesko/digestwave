CREATE TABLE "public".posts (
    site_code VARCHAR(8),
    hash VARCHAR(16) PRIMARY KEY,
    publication_time BIGINT,
    title TEXT,
    content TEXT
);
