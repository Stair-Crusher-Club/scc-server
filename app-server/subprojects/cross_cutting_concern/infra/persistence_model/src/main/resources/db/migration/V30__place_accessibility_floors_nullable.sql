ALTER TABLE place_accessibility
    ALTER COLUMN floors DROP NOT NULL,
    ALTER COLUMN floors SET DEFAULT NULL,
    ALTER COLUMN entrance_door_types DROP NOT NULL,
    ALTER COLUMN entrance_door_types SET DEFAULT NULL;

ALTER TABLE building_accessibility
    ALTER COLUMN entrance_door_types DROP NOT NULL,
    ALTER COLUMN entrance_door_types SET DEFAULT NULL;