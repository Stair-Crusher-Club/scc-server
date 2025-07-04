ALTER TABLE place_review
    ADD COLUMN user_id VARCHAR(36) NOT NULL,
    ALTER COLUMN comment DROP NOT NULL;

CREATE INDEX idx_place_review_user_id ON place_review(user_id);

ALTER TABLE toilet_review
    ADD COLUMN user_id VARCHAR(36) NOT NULL;

CREATE INDEX idx_toilet_review_user_id ON toilet_review(user_id);
