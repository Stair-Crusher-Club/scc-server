ALTER TABLE place_accessibility
    ADD COLUMN floors TEXT NOT NULL DEFAULT '',
    ADD COLUMN is_stair_only_option BOOLEAN NULL,
    ADD COLUMN stair_height_level VARCHAR(32) NULL,
    ADD COLUMN entrance_door_types TEXT NOT NULL DEFAULT '';

ALTER TABLE building_accessibility
    ADD COLUMN entrance_stair_height_level VARCHAR(32) NULL,
    ADD COLUMN entrance_door_types TEXT NOT NULL DEFAULT '',
    ADD COLUMN elevator_stair_height_level VARCHAR(32) NULL;