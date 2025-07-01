CREATE TABLE IF NOT EXISTS search_place_preset(
    id VARCHAR(36) NOT NULL,
    type VARCHAR(36) NOT NULL,
    description TEXT NOT NULL,
    search_text VARCHAR(255) NOT NULL,
    filter JSONB NULL,
    sort VARCHAR(128) NULL,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE INDEX idx_search_place_preset_created_at ON search_place_preset(created_at);
