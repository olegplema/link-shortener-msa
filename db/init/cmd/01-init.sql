CREATE SEQUENCE IF NOT EXISTS outbox_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE IF NOT EXISTS idempotency_records_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE IF NOT EXISTS short_urls (
    id VARCHAR(255) PRIMARY KEY,
    original_url VARCHAR(255) NOT NULL,
    expiration TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMPTZ,
    version BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS outbox (
    id BIGINT PRIMARY KEY DEFAULT nextval('outbox_seq'),
    aggregatetype VARCHAR(255) NOT NULL,
    aggregateid VARCHAR(255) NOT NULL,
    eventtype VARCHAR(255) NOT NULL,
    payload JSONB NOT NULL,
    request_id VARCHAR(128),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS idempotency_records (
    id BIGINT PRIMARY KEY DEFAULT nextval('idempotency_records_seq'),
    idempotency_key VARCHAR(255) NOT NULL,
    operation VARCHAR(64) NOT NULL,
    request_hash VARCHAR(64) NOT NULL,
    response_body JSONB,
    resource_id VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    expires_at TIMESTAMPTZ NOT NULL DEFAULT (NOW() + INTERVAL '24 hours'),
    CONSTRAINT uk_idempotency_operation_key UNIQUE (operation, idempotency_key)
);

ALTER SEQUENCE IF EXISTS outbox_seq OWNED BY outbox.id;
ALTER SEQUENCE IF EXISTS idempotency_records_seq OWNED BY idempotency_records.id;

CREATE INDEX IF NOT EXISTS idx_outbox_created_at ON outbox (created_at);
CREATE INDEX IF NOT EXISTS idx_outbox_aggregate ON outbox (aggregatetype, aggregateid);
CREATE INDEX IF NOT EXISTS idx_idempotency_records_expires_at ON idempotency_records (expires_at);
CREATE INDEX IF NOT EXISTS idx_short_urls_deleted_deleted_at ON short_urls (deleted, deleted_at);

CREATE EXTENSION IF NOT EXISTS pg_cron;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM cron.job
        WHERE jobname = 'cleanup-idempotency-records'
    ) THEN
        PERFORM cron.schedule(
            'cleanup-idempotency-records',
            '0 * * * *',
            $$
            DELETE FROM idempotency_records
            WHERE id IN (
                SELECT id
                FROM idempotency_records
                WHERE expires_at < NOW()
                LIMIT 1000
            )
            $$
        );
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM cron.job
        WHERE jobname = 'cleanup-deleted-short-urls'
    ) THEN
        PERFORM cron.schedule(
            'cleanup-deleted-short-urls',
            '15 * * * *',
            $$
            DELETE FROM short_urls
            WHERE id IN (
                SELECT id
                FROM short_urls
                WHERE deleted = TRUE
                  AND deleted_at < NOW() - INTERVAL '7 days'
                LIMIT 1000
            )
            $$
        );
    END IF;
END $$;
