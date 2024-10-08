CREATE TABLE IF NOT EXISTS place_accessibility_upvote (
    id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    place_accessibility_id VARCHAR(36) NOT NULL,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP(6) WITH TIME ZONE NULL DEFAULT NULL,
    PRIMARY KEY (id)
);

CREATE INDEX idx_place_accessibility_upvote_1 ON place_accessibility_upvote(user_id);

CREATE INDEX idx_place_accessibility_upvote_2 ON place_accessibility_upvote(place_accessibility_id);