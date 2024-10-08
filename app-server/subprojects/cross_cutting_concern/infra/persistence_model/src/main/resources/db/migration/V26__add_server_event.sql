CREATE TABLE IF NOT EXISTS server_event (
    id VARCHAR(36) NOT NULL,
    type VARCHAR(64) NOT NULL,
    payload TEXT NOT NULL DEFAULT '',
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE INDEX idx_server_event_1 ON server_event(type, created_at);