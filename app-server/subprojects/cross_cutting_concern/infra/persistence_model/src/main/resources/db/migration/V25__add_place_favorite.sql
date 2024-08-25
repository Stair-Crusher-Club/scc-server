CREATE TABLE IF NOT EXISTS place_favorite (
    id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    place_id VARCHAR(36) NOT NULL,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP(6) WITH TIME ZONE NULL DEFAULT NULL,
    PRIMARY KEY (id)
);

CREATE INDEX idx_place_favorite_1 ON place_favorite(user_id);

CREATE INDEX idx_place_favorite_2 ON place_favorite(place_id);