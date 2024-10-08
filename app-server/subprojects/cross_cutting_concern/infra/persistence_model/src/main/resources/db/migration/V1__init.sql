CREATE TABLE IF NOT EXISTS building (
    id VARCHAR(36) NOT NULL,
    name VARCHAR(32) NULL,
    location_x FLOAT8 NOT NULL,
    location_y FLOAT8 NOT NULL,
    address_si_do VARCHAR(32) NOT NULL,
    address_si_gun_gu VARCHAR(32) NOT NULL,
    address_eup_myeon_dong VARCHAR(32) NOT NULL,
    address_li VARCHAR(32) NOT NULL,
    address_road_name VARCHAR(32) NOT NULL,
    address_main_building_number VARCHAR(32) NOT NULL,
    address_sub_building_number VARCHAR(32) NOT NULL,
    si_gun_gu_id VARCHAR(36) NOT NULL,
    eup_myeon_dong_id VARCHAR(36) NOT NULL,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS place (
    id VARCHAR(36) NOT NULL,
    name VARCHAR(32) NOT NULL,
    location_x FLOAT8 NOT NULL,
    location_y FLOAT8 NOT NULL,
    building_id VARCHAR(36) NULL,
    si_gun_gu_id VARCHAR(36) NULL,
    eup_myeon_dong_id VARCHAR(36) NULL,
    category VARCHAR(32) NULL,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS building_accessibility (
    id VARCHAR(36) NOT NULL,
    building_id VARCHAR(36) NOT NULL,
    entrance_stair_info VARCHAR(32) NOT NULL DEFAULT 'NONE',
    has_slope BOOLEAN NOT NULL DEFAULT FALSE,
    has_elevator BOOLEAN NOT NULL,
    elevator_stair_info VARCHAR(32) NOT NULL DEFAULT 'NONE',
    user_id VARCHAR(36) DEFAULT NULL,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE INDEX idx_building_accessibility_1 ON building_accessibility(building_id);

CREATE INDEX idx_building_accessibility_2 ON building_accessibility(user_id);

CREATE TABLE IF NOT EXISTS building_accessibility_comment (
    id VARCHAR(36) NOT NULL,
    building_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) DEFAULT NULL,
    comment TEXT NOT NULL,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE INDEX idx_building_accessibility_comment_1 ON building_accessibility_comment(building_id);

CREATE INDEX idx_building_accessibility_comment_2 ON building_accessibility_comment(user_id);

CREATE TABLE IF NOT EXISTS building_accessibility_upvote (
    id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    building_accessibility_id VARCHAR(36) NOT NULL,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP(6) WITH TIME ZONE NULL DEFAULT NULL,
    PRIMARY KEY (id)
);

CREATE INDEX idx_building_accessibility_upvote_1 ON building_accessibility_upvote(user_id);

CREATE INDEX idx_building_accessibility_upvote_2 ON building_accessibility_upvote(building_accessibility_id);

CREATE TABLE IF NOT EXISTS place_accessibility (
    id VARCHAR(36) NOT NULL,
    place_id VARCHAR(36) NOT NULL,
    is_first_floor BOOLEAN NOT NULL,
    stair_info VARCHAR(32) NOT NULL DEFAULT 'NONE',
    has_slope BOOLEAN NOT NULL DEFAULT FALSE,
    user_id VARCHAR(36) DEFAULT NULL,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE INDEX idx_place_accessibility_1 ON place_accessibility(place_id);

CREATE INDEX idx_place_accessibility_2 ON place_accessibility(user_id);

CREATE TABLE IF NOT EXISTS place_accessibility_comment (
    id VARCHAR(36) NOT NULL,
    place_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) DEFAULT NULL,
    comment TEXT NOT NULL,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE INDEX idx_place_accessibility_comment_1 ON place_accessibility_comment(place_id);

CREATE INDEX idx_place_accessibility_comment_2 ON place_accessibility_comment(user_id);

CREATE TABLE IF NOT EXISTS scc_user (
    id VARCHAR(36) NOT NULL,
    nickname VARCHAR(32) NOT NULL,
    encrypted_password VARCHAR(64) NOT NULL,
    instagram_id VARCHAR(32) DEFAULT NULL,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE UNIQUE INDEX idx_user_table_1 ON scc_user(nickname);