ALTER TABLE place_accessibility ADD COLUMN images TEXT NOT NULL DEFAULT '';

ALTER TABLE building_accessibility ADD COLUMN entrance_images TEXT  NOT NULL DEFAULT '';

ALTER TABLE building_accessibility ADD COLUMN elevator_images TEXT  NOT NULL DEFAULT '';