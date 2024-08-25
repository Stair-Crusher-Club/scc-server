CREATE TABLE IF NOT EXISTS accessibility_image_face_blurring_history (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    place_accessibility_id VARCHAR(36) NULL,
    building_accessibility_id VARCHAR(36) NULL,
    original_image_urls TEXT NOT NULL DEFAULT '[]',
    blurred_image_urls TEXT NOT NULL DEFAULT '[]',
    detected_people_counts TEXT NOT NULL,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_accessibility_image_face_blurring_history_1 ON accessibility_image_face_blurring_history(created_at);

CREATE INDEX idx_accessibility_image_face_blurring_history_2 ON accessibility_image_face_blurring_history(place_accessibility_id);

CREATE INDEX idx_accessibility_image_face_blurring_history_3 ON accessibility_image_face_blurring_history(building_accessibility_id);