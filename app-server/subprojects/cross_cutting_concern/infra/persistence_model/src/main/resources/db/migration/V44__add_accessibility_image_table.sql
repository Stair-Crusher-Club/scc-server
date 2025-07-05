CREATE TABLE IF NOT EXISTS accessibility_image (
    id VARCHAR(36) NOT NULL,
    accessibility_id VARCHAR(36) NOT NULL,
    accessibility_type VARCHAR(16) NOT NULL,

    image_url VARCHAR(255) NOT NULL,
    blurred_image_url VARCHAR(255),
    thumbnail_url VARCHAR(255),

    image_type VARCHAR(16),

    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_post_processed_at TIMESTAMP(6) WITH TIME ZONE,
    PRIMARY KEY (id)
);

CREATE INDEX idx_accessibility_image_accessibility_id ON accessibility_image(accessibility_id);
CREATE INDEX idx_accessibility_image_created_at ON accessibility_image(created_at);
CREATE INDEX idx_accessibility_last_post_processed_at ON accessibility_image(last_post_processed_at);
