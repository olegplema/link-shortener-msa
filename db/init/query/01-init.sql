CREATE TABLE IF NOT EXISTS short_url_entity (
    id VARCHAR(255) PRIMARY KEY,
    original_url VARCHAR(255) NOT NULL,
    expiration TIMESTAMPTZ NOT NULL,
    click_count INTEGER NOT NULL,
    last_accessed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_short_url_entity_original_url ON short_url_entity (original_url);
