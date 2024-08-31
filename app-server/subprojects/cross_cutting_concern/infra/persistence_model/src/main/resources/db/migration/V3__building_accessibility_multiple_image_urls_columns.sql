ALTER TABLE building_accessibility DROP COLUMN image_urls;

ALTER TABLE building_accessibility ADD COLUMN entrance_image_urls TEXT NOT NULL DEFAULT '[]';

ALTER TABLE building_accessibility ADD COLUMN elevator_image_urls TEXT NOT NULL DEFAULT '[]';