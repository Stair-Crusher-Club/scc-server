CREATE TABLE IF NOT EXISTS accessibility_allowed_region (
    id VARCHAR(36) NOT NULL,
    name VARCHAR(64) NOT NULL,
    boundary_vertices TEXT NOT NULL,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE INDEX idx_accessibility_allowed_region_1 ON accessibility_allowed_region(created_at);