CREATE TABLE IF NOT EXISTS anonymous_user (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    scc_user_id VARCHAR(36) NULL,
    converted_at TIMESTAMP(6) WITH TIME ZONE NULL,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_anonymous_user_1 ON anonymous_user(scc_user_id);
CREATE INDEX idx_anonymous_user_2 ON anonymous_user(converted_at);
