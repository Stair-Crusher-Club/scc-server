CREATE TABLE IF NOT EXISTS challenge_rank (
    id VARCHAR(36) NOT NULL,
    challenge_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    contribution_count BIGINT NOT NULL,
    rank BIGINT NOT NULL,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE INDEX idx_challenge_rank_1 ON challenge_rank(challenge_id, user_id);

CREATE INDEX idx_challenge_rank_2 ON challenge_rank(challenge_id, rank);

CREATE INDEX idx_challenge_rank_3 ON challenge_rank(challenge_id, contribution_count);