CREATE TABLE IF NOT EXISTS club_quest (
    id VARCHAR(36) NOT NULL,
    name VARCHAR(64) NOT NULL,
    quest_center_location_x FLOAT8 NOT NULL,
    quest_center_location_y FLOAT8 NOT NULL,
    target_buildings TEXT NOT NULL,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE INDEX idx_club_quest_1 ON club_quest(created_at);