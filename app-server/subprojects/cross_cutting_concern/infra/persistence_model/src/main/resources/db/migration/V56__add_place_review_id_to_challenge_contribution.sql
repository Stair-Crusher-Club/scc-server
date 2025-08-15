ALTER TABLE challenge_contribution ADD COLUMN place_review_id VARCHAR(36) NULL;

CREATE INDEX IF NOT EXISTS idx_challenge_contribution_place_accessibility_id
ON challenge_contribution (place_accessibility_id);

CREATE INDEX IF NOT EXISTS idx_challenge_contribution_building_accessibility_id
ON challenge_contribution (building_accessibility_id);

CREATE INDEX IF NOT EXISTS idx_challenge_contribution_place_review_id
ON challenge_contribution (place_review_id);
