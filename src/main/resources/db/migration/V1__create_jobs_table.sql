CREATE TABLE jobs (
    id UUID PRIMARY KEY,

    type VARCHAR(40) NOT NULL,
    payload JSONB NOT NULL,

    status VARCHAR(24) NOT NULL DEFAULT 'QUEUED',

    priority INTEGER NOT NULL DEFAULT 0,
    attempt_count INTEGER NOT NULL DEFAULT 0,
    max_attempts INTEGER NOT NULL DEFAULT 3,

    available_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    idempotency_key VARCHAR(255),

    locked_at TIMESTAMPTZ,
    locked_by VARCHAR(100),

    last_error TEXT,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_jobs_status CHECK (
        status IN (
            'QUEUED',
            'PROCESSING',
            'RETRY_WAIT',
            'SUCCEEDED',
            'FAILED',
            'DLQ',
            'CANCELLED'
        )
    ),

    CONSTRAINT chk_jobs_attempt_count
        CHECK (attempt_count >= 0),

    CONSTRAINT chk_jobs_max_attempts
        CHECK (max_attempts > 0)
);

CREATE UNIQUE INDEX uq_jobs_idempotency_key
    ON jobs (idempotency_key)
    WHERE idempotency_key IS NOT NULL;

CREATE INDEX idx_jobs_ready
    ON jobs (priority DESC, available_at, created_at)
    WHERE status IN ('QUEUED', 'RETRY_WAIT');

CREATE INDEX idx_jobs_stale_processing
    ON jobs (locked_at)
    WHERE status = 'PROCESSING';