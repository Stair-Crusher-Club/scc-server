CREATE TABLE IF NOT EXISTS club_quest_target_building (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    club_quest_id VARCHAR(36) NOT NULL,
    building_id VARCHAR(36) NOT NULL,
    name VARCHAR(63) NOT NULL,
    location_x FLOAT8 NOT NULL,
    location_y FLOAT8 NOT NULL,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_club_quest_target_building_1 ON club_quest_target_building(club_quest_id);

CREATE INDEX idx_club_quest_target_building_2 ON club_quest_target_building(building_id);

CREATE TABLE IF NOT EXISTS club_quest_target_place (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    club_quest_id VARCHAR(36) NOT NULL,
    target_building_id VARCHAR(36) NOT NULL,
    building_id VARCHAR(36) NOT NULL,
    place_id VARCHAR(36) NOT NULL,
    name VARCHAR(63) NOT NULL,
    location_x FLOAT8 NOT NULL,
    location_y FLOAT8 NOT NULL,
    is_closed BOOLEAN NOT NULL,
    is_not_accessible BOOLEAN NOT NULL,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_club_quest_target_place_1 ON club_quest_target_place(club_quest_id);

CREATE INDEX idx_club_quest_target_place_2 ON club_quest_target_place(target_building_id);

CREATE INDEX idx_club_quest_target_place_3 ON club_quest_target_place(place_id);

CREATE INDEX idx_club_quest_target_place_4 ON club_quest_target_place(building_id);