CREATE SEQUENCE IF NOT EXISTS outbox_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE IF NOT EXISTS short_urls (
    id VARCHAR(255) PRIMARY KEY,
    original_url VARCHAR(255) NOT NULL,
    expiration TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE IF NOT EXISTS outbox (
    id BIGINT PRIMARY KEY DEFAULT nextval('outbox_seq'),
    aggregatetype VARCHAR(255) NOT NULL,
    aggregateid VARCHAR(255) NOT NULL,
    eventtype VARCHAR(255) NOT NULL,
    payload JSONB NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

ALTER SEQUENCE IF EXISTS outbox_seq OWNED BY outbox.id;

CREATE INDEX IF NOT EXISTS idx_outbox_created_at ON outbox (created_at);
CREATE INDEX IF NOT EXISTS idx_outbox_aggregate ON outbox (aggregatetype, aggregateid);
