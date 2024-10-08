CREATE TABLE IF NOT EXISTS accessibility_rank (
    id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    conquered_count INT NOT NULL,
    rank BIGINT NOT NULL,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE INDEX idx_accessibility_rank_1 ON accessibility_rank(user_id);

CREATE INDEX idx_accessibility_rank_2 ON accessibility_rank(rank);

CREATE INDEX idx_accessibility_rank_3 ON accessibility_rank(conquered_count);