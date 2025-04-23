CREATE TABLE IF NOT EXISTS accessibility_report (
    id VARCHAR(36) NOT NULL,
    place_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    reason VARCHAR(255),
    detail TEXT,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE INDEX idx_accessibility_report_place_id ON accessibility_report(place_id);
CREATE INDEX idx_accessibility_report_user_id ON accessibility_report(user_id);