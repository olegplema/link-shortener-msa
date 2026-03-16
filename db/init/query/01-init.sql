CREATE TABLE IF NOT EXISTS short_urls (
    id VARCHAR(255) PRIMARY KEY,
    original_url VARCHAR(255),
    expiration TIMESTAMPTZ,
    click_count INTEGER NOT NULL DEFAULT 0,
    last_accessed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ,
    aggregate_version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_short_urls_original_url ON short_urls (original_url);
